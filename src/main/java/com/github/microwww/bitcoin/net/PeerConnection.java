package com.github.microwww.bitcoin.net;

import com.github.microwww.bitcoin.conf.BlockInfo;
import com.github.microwww.bitcoin.conf.Config;
import com.github.microwww.bitcoin.event.BitcoinAddPeerEvent;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;

@Component
public class PeerConnection implements ApplicationListener<BitcoinAddPeerEvent> {
    private static final Logger logger = LoggerFactory.getLogger(PeerConnection.class);

    private static EventLoopGroup executors = new NioEventLoopGroup();

    @Autowired
    Config config;
    @Autowired
    PeerChannelInboundHandler peerChannelInboundHandlerEventPublisher;

    @Override
    public void onApplicationEvent(BitcoinAddPeerEvent event) {
        Bootstrap bootstrap = new Bootstrap()
                .group(executors)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) {
                        ch.pipeline()
                                .addLast(new BitcoinNetEncode())
                                .addLast(new BitcoinNetDecode(config.getBitcoin()))
                                .addLast(peerChannelInboundHandlerEventPublisher);
                    }
                });
        // connection
        Peer peer = event.getBitcoinSource();
        try {
            bootstrap.connect(peer.getHost(), peer.getPort())
                    .addListener((DefaultChannelPromise e) -> {
                        BlockInfo.getInstance().addPeers((InetSocketAddress) e.channel().localAddress(), peer);
                        logger.info("Connection FROM: " + e.channel().localAddress() + ", TO: " + e.channel().remoteAddress());
                    })
                    .sync().channel().closeFuture()
                    .sync()
                    .addListener(e -> {
                        executors.shutdownGracefully();
                    });
        } catch (InterruptedException e) {
            logger.error("Peer error : {}", peer, e);
        }
    }

}
