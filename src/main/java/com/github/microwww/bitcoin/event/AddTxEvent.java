package com.github.microwww.bitcoin.event;

import com.github.microwww.bitcoin.net.protocol.Tx;

public class AddTxEvent extends BitcoinEvent<Tx> {

    public AddTxEvent(Tx source) {
        super(source);
    }
}
