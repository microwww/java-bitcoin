package com.github.microwww.bitcoin.provider;

import com.github.microwww.bitcoin.AbstractEnv;
import com.github.microwww.bitcoin.conf.CChainParams;
import com.github.microwww.bitcoin.net.PeerChannelServerProtocol;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;

class ServerStarterTest extends AbstractEnv {
    private static final Logger logger = LoggerFactory.getLogger(ServerStarterTest.class);

    public ServerStarterTest() {
        super(CChainParams.Env.MAIN);
        this.chainParams.settings.setPort(0);
    }

    @Test
    void onApplicationEvent() throws InterruptedException, IOException {
        ServerStarter start = new ServerStarter(chainParams, new PeerChannelServerProtocol(localBlockChain, null), localBlockChain);
        CountDownLatch latch = new CountDownLatch(1);
        CountDownLatch d2 = new CountDownLatch(1);
        start.newThreadSTART(e -> {
            latch.countDown();
            InetSocketAddress sa = (InetSocketAddress) e.channel().localAddress();
            logger.info("Listener {}:{}", sa.getHostName(), sa.getPort());
        }, e -> {
            logger.info("这里可以优雅处理关闭");
            d2.countDown();
        });
        latch.await();
        start.close();
        d2.await();
    }
}