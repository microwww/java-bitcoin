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

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

@Component
public class PeerConnection implements ApplicationListener<BitcoinAddPeerEvent> {
    private static final Logger logger = LoggerFactory.getLogger(PeerConnection.class);
    private final Map<String, Peer> peers = new ConcurrentSkipListMap<>();

    @Autowired
    LocalBlockChain localBlockChain;
    @Autowired
    PeerChannelInboundHandler peerChannelInboundHandlerEventPublisher;

    @Override
    public void onApplicationEvent(BitcoinAddPeerEvent event) {
        EventLoopGroup executors = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap()
                .group(executors)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) {
                        ch.pipeline()
                                .addLast(new BitcoinNetEncode(localBlockChain.getSettings()))
                                .addLast(new BitcoinNetDecode(localBlockChain.getSettings()))
                                .addLast(peerChannelInboundHandlerEventPublisher);
                    }
                });
        // connection
        Peer peer = event.getBitcoinSource();
        try {
            bootstrap.connect(peer.getHost(), peer.getPort())
                    .addListener((DefaultChannelPromise e) -> {
                        InetSocketAddress address = (InetSocketAddress) e.channel().localAddress();
                        peer.setLocalAddress(address);
                        this.addPeers(address, peer);
                        logger.info("Connection FROM: " + e.channel().localAddress() + ", TO: " + e.channel().remoteAddress());
                    })
                    .sync().channel().closeFuture()
                    .sync()
                    .addListener(e -> {
                        executors.shutdownGracefully();
                        peers.remove(key(peer.getLocalAddress()));
                    });
        } catch (InterruptedException e) {
            logger.error("Peer error : {}", peer, e);
        }
    }

    public Peer getPeer(ChannelHandlerContext ctx) {
        return this.getPeer((InetSocketAddress) ctx.channel().localAddress());
    }

    public Map<String, Peer> getPeers() {
        return Collections.unmodifiableMap(peers);
    }

    public PeerConnection addPeers(InetSocketAddress address, Peer peer) {
        this.peers.put(key(address), peer);
        return this;
    }

    public PeerConnection remove(InetSocketAddress address) {
        this.peers.remove(key(address));
        return this;
    }

    public Peer getPeer(InetSocketAddress address) {
        return this.peers.get(key(address));
    }

    private String key(InetSocketAddress dr) {
        return dr.getHostString() + ":" + dr.getPort();
    }
}
