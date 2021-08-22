package com.github.microwww.bitcoin.provider;

import com.github.microwww.bitcoin.chain.ChainBlock;
import com.github.microwww.bitcoin.math.Uint256;
import com.github.microwww.bitcoin.math.Uint64;
import com.github.microwww.bitcoin.net.Peer;
import com.github.microwww.bitcoin.net.protocol.*;
import com.github.microwww.bitcoin.store.FileChainBlock;
import com.github.microwww.bitcoin.store.FileTransaction;
import com.github.microwww.bitcoin.store.HeightBlock;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import static com.github.microwww.bitcoin.net.protocol.GetHeaders.MAX_LOCATOR_SZ;

/**
 * `net_processing.cpp`
 */
@Component
public class PeerChannelProtocol {
    private static final Logger logger = LoggerFactory.getLogger(PeerChannelProtocol.class);
    private static final AttributeKey<List<Uint256>> LOADING_BLOCKS = AttributeKey.newInstance("loading-blocks");

    @Autowired
    LocalBlockChain chain;
    private LoadingHeaderManager loadingHeaderManager = new LoadingHeaderManager();

    public void doAction(ChannelHandlerContext ctx, AbstractProtocol ver) throws UnsupportedOperationException {
        try {
            Method service = PeerChannelProtocol.class.getDeclaredMethod("service", ChannelHandlerContext.class, ver.getClass());
            service.invoke(this, ctx, ver);
        } catch (IllegalAccessException | InvocationTargetException e) {
            logger.warn("Request error !", e);
            throw new UnsupportedOperationException(e);
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

        loadingHeaderManager.sendGetHeader(peer, ctx);

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
                if (list.size() >= MAX_LOCATOR_SZ) {
                    break;
                }
                if (list.size() > GetHeaders.MAX_UNCONNECTING_HEADERS) {
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
        if (list.size() > GetHeaders.MAX_UNCONNECTING_HEADERS) {
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
        Attribute<List<Uint256>> loading = ctx.channel().attr(LOADING_BLOCKS);
        if (loading.get() != null && !loading.get().isEmpty()) {
            return;
        }
        Map<Uint256, ChainBlock> readyBlocks = new LinkedHashMap<>();
        ChainBlock[] cb = request.getChainBlocks();
        for (ChainBlock k : cb) {
            Uint256 hash = k.hash();
            Assert.isTrue(k.header.getTxCount().intValueExact() == 0, "Headers tx.length == 0");
            if (logger.isDebugEnabled())
                logger.debug("Headers new block : {}, tx: {}", hash.toHexReverse256(), k.header.getTxCount());
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
        if (readyBlocks.isEmpty()) {
            return;
        }
        Peer peer = ctx.channel().attr(Peer.PEER).get();
        logger.info("Get head : {}, from {}:{}, will loading it !", readyBlocks.size(), peer.getHost(), peer.getPort());
        loading.set(new LinkedList<>(readyBlocks.keySet()));
        loadChainBlock(ctx);
    }

    public void loadChainBlock(ChannelHandlerContext ctx) {
        Peer peer = ctx.channel().attr(Peer.PEER).get();
        ctx.executor().execute(() -> {
            Attribute<List<Uint256>> loading = ctx.channel().attr(LOADING_BLOCKS);
            if (loading.get() == null || loading.get().isEmpty()) {
                return;
            }
            List<GetData.Message> ms = new ArrayList<>(10);
            Iterator<Uint256> iterator = loading.get().iterator();
            while (iterator.hasNext()) {
                if (ms.size() < 10) {
                    Uint256 hash = iterator.next();
                    iterator.remove();
                    GetData.Message msg = new GetData.Message()
                            .setHashIn(hash)
                            .setTypeIn(GetDataType.WITNESS_BLOCK);
                    ms.add(msg);
                } else break;
            }
            GetData data = new GetData(peer).setMessages(ms.toArray(new GetData.Message[]{}));
            ctx.writeAndFlush(data);
        });
    }

    public void sendLoadOneChainBlock(ChannelHandlerContext ctx) {
        Peer peer = ctx.channel().attr(Peer.PEER).get();
        Attribute<List<Uint256>> loading = ctx.channel().attr(LOADING_BLOCKS);
        if (loading.get() == null || loading.get().isEmpty()) {
            loadingHeaderManager.stopAndNext(peer);
            return;
        }
        Uint256 one = loading.get().remove(0);
        GetData data = new GetData(peer).setMessages(new GetData.Message[]{
                new GetData.Message()
                        .setHashIn(one)
                        .setTypeIn(GetDataType.WITNESS_BLOCK)
        });
        ctx.writeAndFlush(data);
    }

    private long current = System.currentTimeMillis();

    public void service(ChannelHandlerContext ctx, Block request) {
        ChainBlock cb = request.getChainBlock();
        if (!cb.verifyMerkleTree()) {
            logger.error("RawTransaction MerkleRoot do not match : {}, Now is test so skip", cb.hash());
        }
        Optional<HeightBlock> hc = chain.getDiskBlock().writeBlock(cb, true);
        if (logger.isInfoEnabled()) {
            long next = System.currentTimeMillis();
            if (next - current > 5000) {
                current = next;
                logger.info("Get one blocks, height: {}, {}", hc.map(HeightBlock::getHeight).orElse(-1), cb.hash());
            }
        }
        if (hc.isPresent()) {
            FileChainBlock fc = hc.get().getFileChainBlock();
            if (!fc.isCache()) {
                FileTransaction[] ft = fc.getFileTransactions();
                chain.getTransactionStore().serializationTransaction(ft);
            } else {
                logger.warn("WHY ! load one exist ?");
            }
        } else {
            logger.warn("STOP ! Not find pre-block");
            Attribute<List<Uint256>> loading = ctx.channel().attr(LOADING_BLOCKS);
            loading.set(null);
        }
        sendLoadOneChainBlock(ctx);
    }

    public void service(ChannelHandlerContext ctx, Tx request) {
        logger.debug("Get new tx: {}, add to pool", request.getTransaction().hash());
        chain.getTransactionStore().add(request.getTransaction());
    }

    public void service(ChannelHandlerContext ctx, Inv request) {
        if (true) {// TODO: 根据高度计算
            logger.info("Skip [Inv] request");
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

    //TODO::作用未知
    public void service(ChannelHandlerContext ctx, SendAddrV2 request) {
        ctx.writeAndFlush(request);
    }

    //TODO::作用未知
    public void service(ChannelHandlerContext ctx, SendCmpct request) {
        SendCmpct cmpct = new SendCmpct(request.getPeer()).setVal(request.getVal());
        ctx.writeAndFlush(cmpct);
    }

    // TODO :: 需要坚持是否连通
    public void service(ChannelHandlerContext ctx, Ping request) {
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

    public void service(ChannelHandlerContext ctx, Reject request) {
        Peer peer = request.getPeer();
        logger.warn("Peer-Reject {}:{}, request : {}, reason: {}", peer.getHost(), peer.getPort(), request.getMessage(), request.getReason());
    }

    public void channelClose(Peer peer) {
        loadingHeaderManager.peerClose(peer);
    }

    class LoadingHeaderManager {
        private Map<String, ChannelHandlerContext> peers = new ConcurrentHashMap();// 注意 channel 可以被回收
        private Peer current;

        public void sendGetHeader(Peer peer, ChannelHandlerContext ctx) {
            ChannelHandlerContext channel = peers.putIfAbsent(peer.getURI(), ctx);
            if (channel == null)
                logger.info("Add peer : {}, Peers {}", peer.getURI(), peers.size());

            stopAndNext(peer);
        }

        public synchronized void stopAndNext(Peer peer) {
            if (current == null || current == peer) {
                logger.debug("CHANGE peer to GET header: {}:{}", peer.getHost(), peer.getPort());
                current = null;
                int h = chain.getDiskBlock().getLatestHeight();
                ChannelHandlerContext[] can = peers.values().stream().filter(e -> {
                    Channel ch = e.channel();
                    Peer next = ch.attr(Peer.PEER).get();
                    return next.getBlockHeight() > h && next.isMeReady() && next.isRemoteReady() && ch.isWritable();
                }).toArray(ChannelHandlerContext[]::new);

                int len = can.length;
                if (len == 0) {
                    logger.warn("Not have peers to GET header");
                } else {
                    int r = (int) (Math.random() * len);
                    current = can[r].channel().attr(Peer.PEER).get();
                    PeerChannelProtocol.this.sendGetHeader(can[r]);
                    logger.info("CHANGE ! GET header by peer {} in {} : {}", r, len, current.getURI());
                }
            } else logger.debug("U [{}:{}] can not stop it", peer.getHost(), peer.getPort());
        }

        public void peerClose(Peer peer) {
            ChannelHandlerContext remove = peers.remove(peer.getURI());
            if (remove != null) {
                logger.info("Remove peer : {}, Peers {}", peer.getURI(), peers.size());
                stopAndNext(peer);
            }
        }

    }
}
