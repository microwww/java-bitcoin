package com.github.microwww.bitcoin.event;

import org.springframework.context.ApplicationEvent;

import java.net.URI;

public abstract class BitcoinEvent<T> extends ApplicationEvent {
    /**
     * Bitcoin 相关的事件
     *
     * @param source the object on which the Event initially occurred
     * @throws IllegalArgumentException if source is null
     */
    public BitcoinEvent(T source) {
        super(source);
    }

    public T getBitcoinSource() {
        return (T) super.getSource();
    }
}
