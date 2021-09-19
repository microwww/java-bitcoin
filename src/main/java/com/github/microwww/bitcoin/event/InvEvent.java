package com.github.microwww.bitcoin.event;

import com.github.microwww.bitcoin.net.protocol.Inv;

public class InvEvent extends BitcoinEvent<Inv> {

    public InvEvent(Inv source) {
        super(source);
    }
}
