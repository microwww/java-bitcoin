package com.github.microwww.bitcoin.provider;

import com.github.microwww.bitcoin.chain.ChainBlock;
import com.github.microwww.bitcoin.conf.Settings;
import com.github.microwww.bitcoin.math.Uint256;
import com.github.microwww.bitcoin.math.Uint64;
import com.github.microwww.bitcoin.net.Peer;
import com.github.microwww.bitcoin.net.protocol.*;
import com.github.microwww.bitcoin.store.FileTransaction;
import com.github.microwww.bitcoin.store.HeightBlock;
import io.netty.channel.ChannelHandlerContext;
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

    @Autowired
    Settings config;
    @Autowired
    LocalBlockChain chain;
    private Map<Uint256, LoadingBlock> readyBlocks = new ConcurrentHashMap<>(100);

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
        } catch (UnsupportedOperationException e) {
            throw e;
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

        sendGetHeader(ctx);

        ctx.executor().execute(() -> {
            ctx.write(new FeeFilter(peer));
        });
    }

    private void sendGetHeader(ChannelHandlerContext ctx) {
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
                list.add(chain.getDiskBlock().getHash(i).get());
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
        ChainBlock[] cb = request.getChainBlocks();
        for (ChainBlock k : cb) {
            Uint256 hash = k.hash();
            Assert.isTrue(k.header.getTxCount().intValueExact() == 0, "Headers tx.length == 0");
            logger.debug("Headers new block : {}, tx: {}", hash.toHexReverse256(), k.header.getTxCount());
            Uint256 preHash = k.header.getPreHash();
            int height = chain.getDiskBlock().getHeight(preHash);
            if (height >= 0) {
                Optional<HeightBlock> hc = chain.getDiskBlock().readBlock(hash);
                if (!hc.isPresent()) {
                    readyBlocks.putIfAbsent(hash, new LoadingBlock(k.hash()));
                }
            } else {
                LoadingBlock ready = readyBlocks.get(preHash);
                if (ready == null) {
                    logger.warn("Not find pre-block Hash : {} -> {}", preHash, hash);
                } else {
                    readyBlocks.putIfAbsent(hash, new LoadingBlock(k.hash()));
                }
                /*
                Optional<HeightBlock> hc = chain.getDiskBlock().readBlock(preHash);
                if (hc.isPresent()) {
                    height = hc.get().getHeight();
                    chain.getDiskBlock().writeBlock(k, height + 1, true);
                } else {
                    logger.warn("Not find pre-block Hash : {} -> {}", preHash, k.hash());
                }
                 */
            }
        }
        logger.info("Find blocks from headers, count : {}, and to get it", cb.length);
        loadChainBlock(ctx);
    }

    public void loadChainBlock(ChannelHandlerContext ctx) {
        Peer peer = ctx.channel().attr(Peer.PEER).get();
        ctx.executor().execute(() -> {
            List<GetData.Message> ms = new ArrayList<>(10);
            Iterator<Uint256> iterator = readyBlocks.keySet().iterator();
            while (iterator.hasNext()) {
                if (ms.size() < 10) {
                    LoadingBlock v = readyBlocks.get(iterator.next());
                    boolean ic = v.compareAndInc(0);
                    if (ic) {
                        GetData.Message msg = new GetData.Message()
                                .setHashIn(v.hash)
                                .setTypeIn(GetDataType.WITNESS_BLOCK);
                        ms.add(msg);
                    }
                } else break;
            }
            GetData data = new GetData(peer).setMessages(ms.toArray(new GetData.Message[]{}));
            ctx.writeAndFlush(data);
        });
    }

    public void service(ChannelHandlerContext ctx, Block request) {
        ChainBlock cb = request.getChainBlock();
        logger.info("Get one blocks from peer : {}", request.getChainBlock().hash());
        Optional<HeightBlock> hc = chain.getDiskBlock().writeBlock(cb, true);
        if (hc.isPresent()) {
            FileTransaction[] ft = hc.get().getFileChainBlock().getFileTransactions();
            chain.getTransactionStore().serializationTransaction(ft);
        }
        readyBlocks.remove(request.getChainBlock().hash());
        if (readyBlocks.isEmpty()) { // TODO:: 服务器断开会导致永远不为空
            sendGetHeader(ctx);
        }
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

    public class LoadingBlock {
        public final Uint256 hash;
        private int status;

        public LoadingBlock(Uint256 hash) {
            this.hash = hash;
        }

        public synchronized boolean compareAndInc(int i) {
            if (this.status == i) {
                this.status += 1;
                return true;
            }
            return false;
        }
    }
}
