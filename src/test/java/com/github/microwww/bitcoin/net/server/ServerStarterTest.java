package com.github.microwww.bitcoin.net.server;

import com.github.microwww.bitcoin.conf.CChainParams;
import com.github.microwww.bitcoin.conf.Settings;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class ServerStarterTest {

    @Test
    @Disabled
    void onApplicationEvent() throws InterruptedException, IOException {
        ServerStarter start = new ServerStarter();
        start.chainParams = new CChainParams(new Settings());
        //start.handler = new ServerChannelInboundHandler();

        start.newThreadSTART(e -> {
            System.out.println(e);
        }, e -> {
            System.out.println("这里可以优雅处理关闭");
        });
    }
}