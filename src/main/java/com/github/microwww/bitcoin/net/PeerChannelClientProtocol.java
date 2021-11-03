package com.github.microwww.bitcoin.net;

import com.github.microwww.bitcoin.chain.BlockHeader;
import com.github.microwww.bitcoin.chain.ChainBlock;
import com.github.microwww.bitcoin.event.*;
import com.github.microwww.bitcoin.math.Uint256;
import com.github.microwww.bitcoin.math.Uint64;
import com.github.microwww.bitcoin.net.protocol.*;
import com.github.microwww.bitcoin.provider.LocalBlockChain;
import com.github.microwww.bitcoin.provider.Peer;
import com.github.microwww.bitcoin.provider.TimeoutTaskManager;
import com.github.microwww.bitcoin.store.DiskBlock;
import com.github.microwww.bitcoin.store.Height;
import com.github.microwww.bitcoin.util.Tools;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * `net_processing.cpp`
 */
@Component
public class PeerChannelClientProtocol implements Closeable {
    private static final Logger logger = LoggerFactory.getLogger(PeerChannelClientProtocol.class);
    private static final Logger log5seconds = Tools.timeLogger(logger, 5);
    public static final Logger verify = LoggerFactory.getLogger("mode.test");
    public static final String CACHE_HEADERS = "cache_headers";

    @Autowired
    LocalBlockChain chain;
    @Autowired
    ApplicationEventPublisher publisher;
    private final TimeoutTaskManager<ChannelHandlerContext> taskManager;
    // private LoadingHeaderManager loadingHeaderManager = new LoadingHeaderManager();

    public PeerChannelClientProtocol() {
        this.taskManager = new TimeoutTaskManager<>(e -> {
            this.sendGetHeader(e);
        }, 10, TimeUnit.SECONDS);
        init();
    }

    private void init() {
        taskManager.putCache(CACHE_HEADERS, new ConcurrentLinkedDeque<>());
        taskManager.addChangeListeners((t, u) -> {
            taskManager.getCache(CACHE_HEADERS, Queue.class).clear();
        });
    }

    public void doAction(ChannelHandlerContext ctx, AbstractProtocol request) throws UnsupportedOperationException {
        try {
            Method service = this.getClass().getMethod("service", ChannelHandlerContext.class, request.getClass());
            service.invoke(this, ctx, request);
        } catch (IllegalAccessException | InvocationTargetException e) {
            logger.warn("Server executor error !", e);
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            logger.warn("Unsupported handler in {} : {}", this.getClass().getSimpleName(), e.getMessage());
            throw new UnsupportedOperationException("" + this.getClass().getSimpleName() + " Unsupported handler", e);
        }
    }

    public void service(ChannelHandlerContext ctx, AbstractProtocol protocol) {
        throw new UnsupportedOperationException("Unsupported protocol : " + protocol.support().cmd());
    }

    public void service(ChannelHandlerContext ctx, Version version) {
        Peer peer = ctx.channel().attr(Peer.PEER).get();
        peer.setVersion(version);
        peer.setMeReady(true);

        ctx.executor().execute(() -> {
            ctx.write(new WtxidRelay(peer));
            ctx.write(new SendAddrV2(peer));
            // VerACK
            ctx.write(new VerACK(peer));
            ctx.write(new GetAddr(peer));
        });
    }

    public void service(ChannelHandlerContext ctx, VerACK ack) {
        Peer peer = ctx.channel().attr(Peer.PEER).get();
        peer.setRemoteReady(true);
        logger.info("New outbound peer connected: version: {}, blocks={}", peer.getVersion().getProtocolVersion(), peer.getBlockHeight());
        ctx.executor().execute(() -> {
            ctx.write(new SendHeaders(peer));
            if (peer.getVersion().getProtocolVersion() >= 70014) {
                //ctx.write(new SendCmpct(peer).setVersion(2));
                ctx.write(new SendCmpct(peer));
            }
            ctx.write(new Ping(peer));
            taskManager.addProvider(ctx);// getheaders
            ctx.writeAndFlush(new FeeFilter(peer));
        });
    }

    public void sendGetHeader(ChannelHandlerContext ctx) {
        ctx.executor().execute(() -> {
            // TASKMANAGER: .1. send header
            taskManager.assertIsMe(ctx).touchTenFold(ctx, "Waiting send GetHeaders");
            sendGetHeaderNow(ctx);
            ctx.flush();
        });
    }

    public void sendGetHeaderNow(ChannelHandlerContext ctx) {
        Peer peer = ctx.channel().attr(Peer.PEER).get();
        int height = chain.getDiskBlock().getLatestHeight();
        int step = 1;
        List<Uint256> list = new ArrayList<>();
        for (int i = height - 1; i >= 0; i -= step) {
            if (list.size() >= GetHeaders.MAX_LOCATOR_SZ) {
                break;
            }
            if (list.size() > GetHeaders.MAX_UN_CONNECTING_HEADERS) {
                step *= 2;
            }
            Optional<Uint256> hash = chain.getDiskBlock().getHash(i);
            if (hash.isPresent()) {
                list.add(hash.get());
            } else {
                logger.error("Not hear !!!");
                return;
            }
        }
        list.add(chain.getChainParams().env.G.hash());
        GetHeaders hd = new GetHeaders(peer).setStarting(list);
        ctx.write(hd);
    }

    // if (msg_type == NetMsgType::GETHEADERS) {
    public void service(ChannelHandlerContext ctx, GetHeaders request) {
        logger.debug("TODO:: GetHeaders request ! server to do");
    }

    // PeerManager::ProcessHeadersMessage
    public void service(ChannelHandlerContext ctx, Headers request) {
        Peer peer = ctx.channel().attr(Peer.PEER).get();
        if (!taskManager.can(ctx)) {
            logger.info("Ignore timeout Headers request : {}, Peer: {}", request.getChainBlocks().length, peer.getURI());
            return;
        }
        // TASKMANAGER: .2. Headers , from send-header-request
        taskManager.assertIsMe(ctx).touchTenFold(ctx, "Waiting parse HEADERS");
        Map<Uint256, BlockHeader> readyBlocks = new LinkedHashMap<>(); // key 按照 set 顺序排序
        BlockHeader[] cb = request.getChainBlocks();
        for (BlockHeader header : cb) {
            Uint256 hash = header.hash();
            Assert.isTrue(header.getTxCount().intValueExact() == 0, "Headers tx.length == 0");
            if (logger.isDebugEnabled())
                logger.debug("Headers new block : {}, tx: {}", hash, header.getTxCount());
            Uint256 preHash = header.getPreHash();
            int height = chain.getDiskBlock().getHeight(preHash);
            if (height >= 0) {
                Optional<ChainBlock> hc = chain.getDiskBlock().readBlock(hash);
                if (!hc.isPresent()) {
                    readyBlocks.putIfAbsent(hash, header);
                }
            } else {
                BlockHeader ready = readyBlocks.get(preHash);
                if (ready == null) {
                    logger.warn("Not find pre-block Hash : {} -> {}", preHash, hash);
                } else {
                    readyBlocks.putIfAbsent(hash, header);
                }
            }
        }
        if (readyBlocks.isEmpty()) {// no more HEADER
            taskManager.remove(ctx);
            ChainBlock last = chain.getDiskBlock().getLastBlock();
            Date time = last.header.getDateTime();
            logger.info("NO new Block BY GetHeader, LAST {}, [{}], IGNORE peer: {}", last.getHeight(), time, peer.getURI());
            return;
        }
        logger.info("Get head : {}, from {}, will loading it !", readyBlocks.size(), peer.getURI());
        // 1. headers
        taskManager.getCache(CACHE_HEADERS, Queue.class).addAll(readyBlocks.keySet());
        loadChainBlock(ctx);
        publisher.publishEvent(new HeadersEvent(request));
        // FOR next header !
        taskManager.addProvider(ctx);
    }

    public void service(ChannelHandlerContext ctx, Block request) {
        try {
            this.tryBlock(ctx, request);
        } catch (RuntimeException ex) {
            ChainBlock ch = request.getChainBlock();
            logger.error("Error hash: {}, pre-hash: {}", ch.hash(), ch.header.getPreHash());
            throw ex;
        }
    }

    public void tryBlock(ChannelHandlerContext ctx, Block request) {
        ChainBlock cb = request.getChainBlock();
        Peer peer = ctx.channel().attr(Peer.PEER).get();
        taskManager.ifMe(ctx, () -> {
            // TASKMANAGER: .3. Block, get block-info from GetData request
            taskManager.assertIsMe(ctx).touch(ctx, "Waiting parse BLOCK");
        });
        Optional<ChainBlock> prehash = chain.getDiskBlock().readBlock(cb.header.getPreHash());
        if (!prehash.isPresent()) {
            logger.warn("LOCATION not find preHash: {}", cb.header.getPreHash());
            if (taskManager.isNoProvider()) {
                taskManager.addProvider(ctx);
            }
            return;
        }
        cb.header.setHeight(prehash.get().getHeight() + 1);
        Assert.isTrue(cb.verifyMerkleTree(), "RawTransaction MerkleRoot do not match : " + cb.hash() + ", Now is test so skip");
        if (verify.isDebugEnabled()) { // 校验数据是否正确
            Assert.isTrue(Arrays.equals(request.getPayload(), request.getChainBlock().serialization()), "BLOCK format serialization error !");
        }

        DiskBlock disk = chain.getDiskBlock();
        disk.verifyNBits(cb);
        cb.header.assertDifficulty();

        chain.getTransactionStore().verifyTransactions(cb);

        Optional<Height> hc = disk.writeBlock(cb, true);
        if (log5seconds.isInfoEnabled()) {
            log5seconds.info("Get blocks {}, height: {}, {}", request.getPeer().getURI(), hc.map(Height::getHeight).orElse(-1), cb.hash());
        }
        if (hc.isPresent()) {
            this.sendLoadOneChainBlock(ctx);
        } else {
            logger.warn("STOP ! Peer {}, Not find pre-block: {}", peer.getURI(), cb.header.getPreHash());
            taskManager.remove(ctx);
        }
        publisher.publishEvent(new AddBlockEvent(request));
    }

    public void service(ChannelHandlerContext ctx, Tx request) {
        logger.debug("Get new tx: {}, add to pool", request.getTransaction().hash());
        chain.getTransactionStore().add(request.getTransaction());
        publisher.publishEvent(new AddTxEvent(request));
    }

    public boolean service(ChannelHandlerContext ctx, Inv request) {
        if (!taskManager.isNoProvider()) {
            logger.debug("Loading headers, so skip [Inv] request : {}, {}, {}", request.getPeer().getURI(), request.getData().length, request.getData()[0].select().get().name());
            return false;
        }
        logger.debug("Inv request : {}, len: {}", request.getPeer().getURI(), request.getData().length);
        publisher.publishEvent(new InvEvent(request));
        ctx.executor().execute(() -> {
            request.validity();
            GetData.Message[] data = request.getData();
            List<GetData.Message> list = new ArrayList<>();
            for (GetData.Message msg : data) {
                Optional<GetDataType> select = msg.select();
                if (select.isPresent()) {
                    list.add(msg);
                } else {
                    logger.warn("Unsupported Data-type : {}", msg.getTypeIn().toString());
                }
            }
            GetData dt = new GetData(request.getPeer()).setMessages(list.toArray(new GetData.Message[]{}));
            ctx.writeAndFlush(dt);
        });
        return true;
    }

    //TODO::作用未知
    public void service(ChannelHandlerContext ctx, WtxidRelay request) {
        logger.debug("TODO:: WtxidRelay request ! server to do");
    }

    public void service(ChannelHandlerContext ctx, SendAddrV2 request) {
        logger.debug("TODO:: SendAddrV2 request ! server to do");
    }

    public void service(ChannelHandlerContext ctx, AddrV2 request) {
        logger.warn("AddrV2 ignore: {}", (Object) request.getNodes());
    }

    //TODO::作用未知
    public void service(ChannelHandlerContext ctx, SendCmpct request) {
        logger.debug("TODO:: SendCmpct request ! server to do");
    }

    public void service(ChannelHandlerContext ctx, Ping request) {
        logger.debug("Get ping from : {}", request.getPeer().getURI());
        long l = ThreadLocalRandom.current().nextLong(Long.MIN_VALUE, Long.MAX_VALUE);
        Pong pong = new Pong(request.getPeer()).setNonce(new Uint64(l));
        ctx.writeAndFlush(pong);
        logger.debug("Send PONG, {}", request.getPeer().getURI());
    }

    public void service(ChannelHandlerContext ctx, Pong request) {
        logger.debug("Pong request ! server to do");
    }

    public void service(ChannelHandlerContext ctx, FeeFilter request) {
        FeeFilter fee = new FeeFilter(request.getPeer()).setFee(1_000);
        ctx.writeAndFlush(fee);
    }

    public void service(ChannelHandlerContext ctx, SendHeaders request) {
        logger.debug("TODO:: SendHeaders request ! server to do");
    }

    public void service(ChannelHandlerContext ctx, Addr request) {
        PeerNode[] nodes = request.getNodes();
        logger.info("Get peer address from peer, count: {}", nodes.length);
        for (PeerNode node : nodes) {
            try {// not block
                publisher.publishEvent(new BitcoinAddPeerEvent(Peer.uri(node.getInetAddress().getHostAddress(), node.getPort())));
            } catch (UnknownHostException e) {
                logger.warn("Addr protocol get a IP address is wrong, IGNORE", e);
            }
        }
    }

    public void service(ChannelHandlerContext ctx, Reject request) {
        Peer peer = request.getPeer();
        logger.warn("Peer-Reject {}:{}, request : {}, reason: {}", peer.getHost(), peer.getPort(), request.getMessage(), request.getReason());
    }

    public void loadChainBlock(ChannelHandlerContext ctx) {
        Peer peer = ctx.channel().attr(Peer.PEER).get();
        ctx.executor().execute(() -> {
            Queue<Uint256> headers = taskManager.getCache(CACHE_HEADERS, Queue.class);
            if (headers.isEmpty()) {
                return;
            }
            int max = GetHeaders.MAX_GET_BLOCK_SZ;
            List<GetData.Message> ms = new ArrayList<>(max);
            for (int i = 0; i < max; i++) {
                Uint256 hash = headers.poll();
                if (hash == null) {
                    break;
                }
                GetData.Message msg = new GetData.Message()
                        .setHashIn(hash)
                        .setTypeIn(GetDataType.WITNESS_BLOCK);
                ms.add(msg);
                logger.debug("Add Batch GET-BLOCK request: {}", hash);
            }
            // TASKMANAGER: .3. GetData , send get-data for getting block-info , after get headers-response
            GetData data = new GetData(peer).setMessages(ms.toArray(new GetData.Message[]{}));
            ctx.writeAndFlush(data);
        });
    }

    public void sendLoadOneChainBlock(ChannelHandlerContext ctx) {
        Peer peer = ctx.channel().attr(Peer.PEER).get();
        taskManager.assertIsMe(ctx).touch(ctx, "Send get-data BLOCK");
        Queue<Uint256> headers = taskManager.getCache(CACHE_HEADERS, Queue.class);
        Uint256 one = headers.poll();
        if (one != null) {
            GetData data = new GetData(peer).setMessages(new GetData.Message[]{
                    new GetData.Message()
                            .setHashIn(one)
                            .setTypeIn(GetDataType.WITNESS_BLOCK)
            });
            logger.debug("Add one request: {}", one);
            ctx.writeAndFlush(data);
        }
    }

    public void channelClose(Peer peer) {
        List<ChannelHandlerContext> tasks = taskManager.getProviders();
        Optional<ChannelHandlerContext> any = tasks.stream().filter(e -> {//
            return e.channel().attr(Peer.PEER).get() == peer;
        }).findAny();

        if (any.isPresent()) {
            taskManager.remove(any.get());
        } else {
            taskManager.getCurrent().ifPresent(e -> {
                if (e.channel().attr(Peer.PEER).get() == peer) {
                    taskManager.remove(e);
                }
            });
        }
    }

    @Override
    public void close() throws IOException {
        taskManager.close();
    }
}
