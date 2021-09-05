package com.github.microwww.bitcoin.net;

import com.github.microwww.bitcoin.chain.ChainBlock;
import com.github.microwww.bitcoin.event.BitcoinAddPeerEvent;
import com.github.microwww.bitcoin.math.Uint256;
import com.github.microwww.bitcoin.math.Uint64;
import com.github.microwww.bitcoin.net.protocol.*;
import com.github.microwww.bitcoin.provider.LocalBlockChain;
import com.github.microwww.bitcoin.provider.Peer;
import com.github.microwww.bitcoin.store.DiskBlock;
import com.github.microwww.bitcoin.store.FileChainBlock;
import com.github.microwww.bitcoin.store.FileTransaction;
import com.github.microwww.bitcoin.store.HeightBlock;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * `net_processing.cpp`
 */
@Component
public class PeerChannelProtocol {
    private static final Logger logger = LoggerFactory.getLogger(PeerChannelProtocol.class);
    public static final Logger verify = LoggerFactory.getLogger("mode.test");

    @Autowired
    LocalBlockChain chain;
    @Autowired
    ApplicationEventPublisher publisher;
    private LoadingHeaderManager loadingHeaderManager = new LoadingHeaderManager();

    public void doAction(ChannelHandlerContext ctx, AbstractProtocol request) throws UnsupportedOperationException {
        try {
            Method service = PeerChannelProtocol.class.getDeclaredMethod("service", ChannelHandlerContext.class, request.getClass());
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
    }

    public void service(ChannelHandlerContext ctx, VerACK ack) {
        Peer peer = ctx.channel().attr(Peer.PEER).get();
        peer.setRemoteReady(true);
        ctx.executor().execute(() -> {
            ctx.write(new VerACK(peer));
        });

        ctx.executor().execute(() -> {
            ctx.write(new GetAddr(peer));
        });
        ctx.executor().execute(() -> {
            ctx.write(new SendHeaders(peer));
        });
        ctx.executor().execute(() -> {
            ctx.write(new SendCmpct(peer));
        });
        ctx.executor().execute(() -> {
            ctx.write(new Ping(peer));
        });

        loadingHeaderManager.sendGetHeader(ctx);

        ctx.executor().execute(() -> {
            ctx.write(new FeeFilter(peer));
        });
    }

    public void sendGetHeader(ChannelHandlerContext ctx) {
        Peer peer = ctx.channel().attr(Peer.PEER).get();
        ctx.executor().execute(() -> {
            int height = chain.getDiskBlock().getLatestHeight();
            int step = 1;
            // TODO:: 这个规则需要确认
            List<Uint256> list = new ArrayList<>();
            for (int i = height; i >= 0; i -= step) {
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
            GetHeaders hd = new GetHeaders(peer).setStarting(list);
            ctx.write(hd);
        });
    }

    // if (msg_type == NetMsgType::GETHEADERS) {
    public void service(ChannelHandlerContext ctx, GetHeaders request) {
        List<Uint256> list = request.getStarting();
        if (list.size() > GetHeaders.MAX_UN_CONNECTING_HEADERS) {
            return;
        }
        int from = -1;
        for (Uint256 uint256 : list) {
            from = chain.getDiskBlock().getHeight(uint256);
            if (from > 0) {
                break;
            }
        }
        Uint256 stopping = request.getStopping();
        if (from >= 0) {
            for (int i = 0, j = 0; i < GetHeaders.MAX_HEADERS_RESULTS; i++) {
                Headers headers = new Headers(request.getPeer());
                List<ChainBlock> bs = new ArrayList<>();
                if (j < 0xFF) { // 最大一个字节
                    j++;
                    Optional<Uint256> hash = chain.getDiskBlock().getHash(from + i);
                    if (hash.isPresent()) {
                        Optional<HeightBlock> cb = chain.getDiskBlock().readBlock(hash.get());
                        Assert.isTrue(cb.isPresent(), "This hash in height , but not in local file");
                        ChainBlock fd = cb.get().getBlock();
                        bs.add(fd);
                        if (fd.hash().equals(stopping)) {
                            ctx.writeAndFlush(headers);
                            break;
                        }
                    } else break;
                } else {
                    j = 0;
                    ctx.writeAndFlush(headers);
                }
                headers.setChainBlocks(bs);
            }
        }
    }

    // PeerManager::ProcessHeadersMessage
    public void service(ChannelHandlerContext ctx, Headers request) {
        if (!loadingHeaderManager.isEmpty()) {
            return;
        }
        Map<Uint256, ChainBlock> readyBlocks = new LinkedHashMap<>();
        ChainBlock[] cb = request.getChainBlocks();
        for (ChainBlock k : cb) {
            Uint256 hash = k.hash();
            Assert.isTrue(k.header.getTxCount().intValueExact() == 0, "Headers tx.length == 0");
            if (logger.isDebugEnabled())
                logger.debug("Headers new block : {}, tx: {}", hash, k.header.getTxCount());
            Uint256 preHash = k.header.getPreHash();
            int height = chain.getDiskBlock().getHeight(preHash);
            if (height >= 0) {
                Optional<HeightBlock> hc = chain.getDiskBlock().readBlock(hash);
                if (!hc.isPresent()) {
                    readyBlocks.putIfAbsent(hash, k);
                }
            } else {
                ChainBlock ready = readyBlocks.get(preHash);
                if (ready == null) {
                    logger.warn("Not find pre-block Hash : {} -> {}", preHash, hash);
                } else {
                    readyBlocks.putIfAbsent(hash, k);
                }
            }
        }
        Peer peer = ctx.channel().attr(Peer.PEER).get();
        if (readyBlocks.isEmpty()) {// no more HEADER
            loadingHeaderManager.removePeer(peer);
            logger.info("No have new Block by Get-Header delete it : {}", peer.getURI());
            return;
        }
        logger.info("Get head : {}, from {}:{}, will loading it !", readyBlocks.size(), peer.getHost(), peer.getPort());
        // 1. headers
        loadingHeaderManager.addAllHeaders(readyBlocks.keySet());
        loadingHeaderManager.loadChainBlock(ctx);
    }

    private long current = System.currentTimeMillis();

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
        loadingHeaderManager.loading.decrementAndGet();
        if (!cb.verifyMerkleTree()) {
            logger.error("RawTransaction MerkleRoot do not match : {}, Now is test so skip", cb.hash());
        }
        if (verify.isDebugEnabled()) { // 校验数据是否正确
            Assert.isTrue(Arrays.equals(request.getPayload(), request.getChainBlock().serialization()), "BLOCK format serialization error !");
        }

        DiskBlock disk = chain.getDiskBlock();
        disk.verifyNBits(cb);
        cb.header.assertDifficulty();

        chain.getTransactionStore().verifyTransactions(cb);

        Optional<HeightBlock> hc = disk.writeBlock(cb, true);
        if (logger.isInfoEnabled()) {
            long next = System.currentTimeMillis();
            if (logger.isDebugEnabled() || next - current > 5000) {
                current = next;
                logger.info("Get blocks {}, height: {}, {}", request.getPeer().getURI(), hc.map(HeightBlock::getHeight).orElse(-1), cb.hash());
            }
        }
        if (hc.isPresent()) {
            FileChainBlock fc = hc.get().getFileChainBlock();
            if (!fc.isCache()) {
                FileTransaction[] ft = fc.getFileTransactions();
                chain.getTransactionStore().serializationTransaction(ft);
            } else {
                logger.warn("WHY ! load one exist : {}", fc.loadBlock().getBlock().hash());
            }
        } else {
            logger.warn("STOP ! Not find pre-block: {}, loss: {}", cb.header.getPreHash(), loadingHeaderManager.loading.intValue());
            loadingHeaderManager.headers.clear();
        }
        loadingHeaderManager.sendLoadOneChainBlock(ctx);
    }

    public void service(ChannelHandlerContext ctx, Tx request) {
        logger.debug("Get new tx: {}, add to pool", request.getTransaction().hash());
        chain.getTransactionStore().add(request.getTransaction());
    }

    public void service(ChannelHandlerContext ctx, Inv request) {
        if (true) {// TODO: 根据高度计算
            logger.info("Skip [Inv] request : {}", request.getPeer().getURI());
            return;
        }
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
    }

    //TODO::作用未知
    public void service(ChannelHandlerContext ctx, WtxidRelay request) {
        ctx.writeAndFlush(request);
    }

    public void service(ChannelHandlerContext ctx, SendAddrV2 request) {
        ctx.writeAndFlush(request);
    }

    public void service(ChannelHandlerContext ctx, AddrV2 request) {
        logger.warn("AddrV2 ignore: {}", request.getNodes());
    }

    //TODO::作用未知
    public void service(ChannelHandlerContext ctx, SendCmpct request) {
        SendCmpct cmpct = new SendCmpct(request.getPeer()).setVal(request.getVal());
        ctx.writeAndFlush(cmpct);
    }

    // TODO :: 需要坚持是否连通
    public void service(ChannelHandlerContext ctx, Ping request) {
        logger.info("Peer send ping, {}", request.getPeer().getURI());
        long l = ThreadLocalRandom.current().nextLong(Long.MIN_VALUE, Long.MAX_VALUE);
        Pong pong = new Pong(request.getPeer()).setNonce(new Uint64(l));
        ctx.writeAndFlush(pong);
    }

    // TODO :: 需要坚持是否连通
    public void service(ChannelHandlerContext ctx, Pong request) {
        logger.info("Get pong !");
    }

    public void service(ChannelHandlerContext ctx, FeeFilter request) {
        FeeFilter fee = new FeeFilter(request.getPeer()).setFee(1_000);
        ctx.writeAndFlush(fee);
    }

    public void service(ChannelHandlerContext ctx, SendHeaders request) {
        logger.info("Get SendHeaders !");
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

    public void channelClose(Peer peer) {
        loadingHeaderManager.peerClose(peer);
    }

    // TODO 该功能设计太麻烦 !!
    class LoadingHeaderManager {
        private Queue<ChannelHandlerContext> queue = new ConcurrentLinkedDeque();// 注意 channel 可以被回收
        private ChannelHandlerContext current;
        private Queue<Uint256> headers = new LinkedList();
        private AtomicInteger loading = new AtomicInteger();

        public void sendGetHeader(ChannelHandlerContext ctx) {
            queue.add(ctx);
            stopAndNext(ctx);
        }

        public synchronized void clear() {
            this.headers.clear();
            this.loading.set(0);
        }

        public synchronized boolean isEmpty() {
            return this.headers.isEmpty() && this.loading.get() == 0;
        }

        private synchronized void stopAndNext(ChannelHandlerContext ctx) {
            if (current != null) {
                if (current == ctx) {
                    Peer peer = current.channel().attr(Peer.PEER).get();
                    logger.info("CHANGE peer to GET header: {} , in: {}", peer.getURI(), queue.size());
                    current = null;
                    this.queue.add(ctx);
                }
            }
            if (current == null) {
                int h = chain.getDiskBlock().getLatestHeight();
                while (true) {
                    ChannelHandlerContext c = queue.poll();
                    if (c == null) {
                        logger.debug("No peer to connection ... , try to restart !");
                        break;
                    }
                    Peer next = c.channel().attr(Peer.PEER).get();
                    if (next.isMeReady() && next.isRemoteReady()) {
                        if (next.getBlockHeight() > h && c.channel().isWritable()) {
                            current = c;
                            PeerChannelProtocol.this.sendGetHeader(current);
                            logger.info("Change, get header by peer {}, length: {}", next.getURI(), queue.size());
                            break;
                        }
                    } else {
                        queue.add(c);
                        continue;
                    }
                }

                if (current == null) {
                    logger.warn("Not have peers to GET header, Maybe is Max-Height");
                }
            } else {
                Peer peer = ctx.channel().attr(Peer.PEER).get();
                logger.info("U [{}:{}] can not stop it", peer.getHost(), peer.getPort());
            }
        }

        public void peerClose(Peer peer) {
            this.removePeer(peer);
        }

        public synchronized void removePeer(Peer peer) {
            Peer c = current.channel().attr(Peer.PEER).get();
            if (peer == c) {
                logger.info("Remove peer : {}, Peers {}", peer.getURI(), queue.size());
                stopAndNext(current);
            } else {
                Optional<ChannelHandlerContext> any = queue.stream().filter(e -> {
                    return e.channel().attr(Peer.PEER).get() == peer;
                }).findAny();

                if (any.isPresent()) {
                    queue.remove(any.get());
                }
            }
        }

        public void loadChainBlock(ChannelHandlerContext ctx) {
            Peer peer = ctx.channel().attr(Peer.PEER).get();
            ctx.executor().execute(() -> {
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
                loading.addAndGet(ms.size());
                GetData data = new GetData(peer).setMessages(ms.toArray(new GetData.Message[]{}));
                ctx.writeAndFlush(data);
            });
        }

        public void sendLoadOneChainBlock(ChannelHandlerContext ctx) {
            Peer peer = ctx.channel().attr(Peer.PEER).get();
            if (headers.isEmpty()) {
                int i = loading.get();
                if (i <= 0) {
                    Assert.isTrue(i == 0, "Error loading < 0");
                    this.stopAndNext(ctx);
                }
                return;
            }
            Uint256 one = headers.poll();
            GetData data = new GetData(peer).setMessages(new GetData.Message[]{
                    new GetData.Message()
                            .setHashIn(one)
                            .setTypeIn(GetDataType.WITNESS_BLOCK)
            });
            logger.debug("Add one request: {}", one);
            loading.incrementAndGet();
            ctx.writeAndFlush(data);
        }

        public boolean addAllHeaders(Collection<? extends Uint256> c) {
            logger.info("Add all {} + {}", headers.size(), c.size());
            return headers.addAll(c);
        }
    }
}
