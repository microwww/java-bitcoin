package com.github.microwww.bitcoin.provider;

import com.github.microwww.bitcoin.chain.ChainBlock;
import com.github.microwww.bitcoin.conf.Settings;
import com.github.microwww.bitcoin.math.Uint256;
import com.github.microwww.bitcoin.math.Uint64;
import com.github.microwww.bitcoin.net.Peer;
import com.github.microwww.bitcoin.net.protocol.*;
import com.github.microwww.bitcoin.store.HeightBlock;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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

    public void doAction(ChannelHandlerContext ctx, AbstractProtocol ver) {
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

        ctx.executor().execute(() -> {
            ctx.write(new FeeFilter(peer));
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
                        ChainBlock fd = cb.get().getBlock().getBlock();
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
            String ok = k.hash().toHexReverse256();
            if (ok.equalsIgnoreCase(k.header.getMerkleRoot().toHexReverse256())) {
                logger.warn("Merkle Root can not match, block hash : {}", k.hash().toHexReverse256());
                continue;
            }
            logger.debug("Find new block : {}, tx: {}", ok, k.header.getTxCount());
            Uint256 preHash = k.header.getPreHash();
            int height = chain.getDiskBlock().getHeight(preHash);
            if (height >= 0) {
                chain.getDiskBlock().writeBlock(k, height + 1, true);
            } else {
                Optional<HeightBlock> hc = chain.getDiskBlock().readBlock(preHash);
                if (hc.isPresent()) {
                    height = hc.get().getHeight();
                    chain.getDiskBlock().writeBlock(k, height + 1, true);
                } else {
                    logger.warn("Not find pre-block Hash : {} -> {}", preHash, k.hash());
                }
            }
        }
        if (config.isTxIndex()) {
            logger.info("Find blocks from headers, count : {}, and to get it", cb.length);
            ctx.executor().execute(() -> {
                GetData.Message[] ms = new GetData.Message[cb.length];
                GetData data = new GetData(request.getPeer());
                for (int i = 0; i < cb.length; i++) {
                    ChainBlock hd = cb[i];
                    ms[i] = new GetData.Message()
                            .setHashIn(hd.hash())
                            .setTypeIn(GetDataType.WITNESS_BLOCK);
                }
                data.setMessages(ms);
                ctx.writeAndFlush(data);
            });
        }
    }

    public void service(ChannelHandlerContext ctx, Block request) {
        ChainBlock cb = request.getChainBlock();
        logger.info("Get one blocks from peer : {}", request.getChainBlock().hash());
        chain.getDiskBlock().writeBlock(cb, true);
    }

    public void service(ChannelHandlerContext ctx, Tx request) {
        logger.debug("Get new tx: {}, add to pool", request.getTransaction().hash());
        chain.getTxMemPool().add(request.getTransaction());
    }

    public void service(ChannelHandlerContext ctx, Inv request) {
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

}
