package com.github.microwww.bitcoin.event;

import com.github.microwww.bitcoin.net.protocol.Block;

public class AddBlockEvent extends BitcoinEvent<Block> {

    public AddBlockEvent(Block source) {
        super(source);
    }
}
