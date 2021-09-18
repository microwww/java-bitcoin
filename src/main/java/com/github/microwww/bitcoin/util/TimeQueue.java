package com.github.microwww.bitcoin.util;

import cn.hutool.cache.GlobalPruneTimer;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

public class TimeQueue<T> {
    private final Queue<T> queue = new ConcurrentLinkedQueue<>();
    private final int limit;
    private final Consumer<Queue<T>> consumer;

    public TimeQueue(Consumer<Queue<T>> consumer, int limit, long times) {
        this.consumer = consumer;
        this.limit = limit;

        GlobalPruneTimer.INSTANCE.schedule(() -> {
            consumer.accept(queue);
        }, times);
    }

    public void add(T t) {
        queue.add(t);
        int limit = queue.size();
        if (this.limit >= limit) {
            consumer.accept(queue);
        }
    }

    public void now() {
        consumer.accept(queue);
    }
}
