package com.github.microwww.bitcoin.provider;

import com.github.microwww.bitcoin.chain.BlockChainContext;
import com.github.microwww.bitcoin.chain.ChainBlock;
import com.github.microwww.bitcoin.math.Uint256;
import com.github.microwww.bitcoin.net.Peer;
import com.github.microwww.bitcoin.net.protocol.*;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@Component
public class PeerChannelProtocol {
    private static final Logger logger = LoggerFactory.getLogger(PeerChannelProtocol.class);

    public void doAction(ChannelHandlerContext ctx, AbstractProtocol ver) {
        try {
            Method service = PeerChannelProtocol.class.getDeclaredMethod("service", ChannelHandlerContext.class, ver.getClass());
            service.invoke(this, ctx, ver);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            logger.warn("Request error !", e);
            throw new UnsupportedOperationException(e);
        } catch (UnsupportedOperationException e) {
            throw e;
        }
    }

    public void service(ChannelHandlerContext ctx, AbstractProtocol protocol) {
        throw new UnsupportedOperationException("Unsupported protocol : " + protocol.support().cmd());
    }

    public void service(ChannelHandlerContext ctx, Version version) {
        Peer peer = BlockChainContext.getPeer(ctx);
        peer.setVersion(version);
        BlockChainContext.getPeer(ctx).setMeReady(true);
        // TODO :: 发送ack需要一个合适的时机
        ctx.write(new VerACK(peer));
    }

    public void service(ChannelHandlerContext ctx, VerACK ack) {
        Peer peer = BlockChainContext.getPeer(ctx);
        peer.setRemoteReady(true);
        ctx.executor().execute(() -> {
            ctx.write(new GetAddr(peer));
        });

        ctx.executor().execute(() -> {
            int height = BlockChainContext.get().getHeight().intValue();
            int step = 1;
            // TODO:: 这个规则需要确认
            List<Uint256> list = new ArrayList<>();
            for (int i = height; i >= 0; i -= step) {
                if (list.size() >= 2000) {
                    break;
                }
                if (list.size() > 10) {
                    step *= 2;
                }
                list.add(BlockChainContext.get().getHash(i).hash());
            }
            GetHeaders hd = new GetHeaders(peer).setList(list);
            ctx.write(hd);
        });
    }

    public void service(ChannelHandlerContext ctx, Headers block) {
        for (ChainBlock k : block.getHeaders()) {
            String ok = k.hash().toHexReverse256();
            if (ok.equalsIgnoreCase(k.header.getMerkleRoot().toHexReverse256())) {
                logger.warn("Merkle Root can not match, block hash : {}", k.hash().toHexReverse256());
                continue;
            }
            logger.info("Find new block : {}", ok);
            BlockChainContext.get().addBlock(k);
        }
    }
}
