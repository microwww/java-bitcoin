package com.github.microwww.bitcoin.conf;

import com.github.microwww.bitcoin.chain.BlockChainContext;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "conf")
public class Config {

    private Settings bitcoin = new Settings();

    public Settings getBitcoin() {
        return bitcoin;
    }

    public void setBitcoin(Settings bitcoin) {
        this.bitcoin = bitcoin;
    }

    public BlockChainContext getBlockInfo() {
        return BlockChainContext.get();
    }
}
