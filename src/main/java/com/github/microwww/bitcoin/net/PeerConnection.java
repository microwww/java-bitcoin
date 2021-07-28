package com.github.microwww.bitcoin.net;

import com.github.microwww.bitcoin.event.BitcoinAddPeerEvent;
import com.github.microwww.bitcoin.provider.LocalBlockChain;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;

@Component
public class PeerConnection implements ApplicationListener<BitcoinAddPeerEvent>, Closeable {
    private static final Logger logger = LoggerFactory.getLogger(PeerConnection.class);
    private static EventLoopGroup executors = new NioEventLoopGroup();

    @Autowired
    LocalBlockChain localBlockChain;
    @Autowired
    PeerChannelInboundHandler peerChannelInboundHandlerEventPublisher;

    @Override
    public void onApplicationEvent(BitcoinAddPeerEvent event) {
        Peer peer = event.getBitcoinSource();
        Bootstrap bootstrap = new Bootstrap()
                .group(executors)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) {
                        ch.attr(Peer.PEER).set(peer);
                        ch.pipeline()
                                .addLast(new BitcoinNetEncode(localBlockChain.getChainParams()))
                                .addLast(new BitcoinNetDecode(localBlockChain.getChainParams()))
                                .addLast(peerChannelInboundHandlerEventPublisher);
                    }
                });
        // connection
        try {
            bootstrap.connect(peer.getHost(), peer.getPort())
                    .addListener((DefaultChannelPromise e) -> {
                        if (e.isSuccess()) {
                            InetSocketAddress address = (InetSocketAddress) e.channel().localAddress();
                            peer.setLocalAddress(address);
                            logger.info("Connection FROM: " + e.channel().localAddress() + ", TO: " + e.channel().remoteAddress());
                        }
                    })
                    .sync().channel().closeFuture()
                    .sync()
                    .addListener(e -> {
                        logger.info("Close Peer {}:{}", peer.getHost(), peer.getPort());
                    });
        } catch (InterruptedException e) {
            logger.error("Peer error : {}", peer, e);
        }
    }

    @Override
    public void close() throws IOException {
        executors.shutdownGracefully();
    }
}
