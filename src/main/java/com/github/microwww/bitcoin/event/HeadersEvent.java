package com.github.microwww.bitcoin.event;

import com.github.microwww.bitcoin.net.protocol.Headers;

public class HeadersEvent extends BitcoinEvent<Headers> {

    public HeadersEvent(Headers source) {
        super(source);
    }
}
