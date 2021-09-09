package com.github.microwww.bitcoin.provider;

import java.lang.reflect.Proxy;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

public class TaskTimeoutManager<T> extends TaskManager<T> {
    private final long timeoutMilliseconds;
    private long currentTime;
    private Condition condition = new ReentrantLock().newCondition();

    public TaskTimeoutManager(Consumer<T> consumer, int time, TimeUnit unit) {
        super(1, proxy(consumer));
        timeoutMilliseconds = unit.convert(time, TimeUnit.MILLISECONDS);
    }

    public static <T> Consumer<T> proxy(Consumer<T> consumer) {
        return (Consumer<T>) Proxy.newProxyInstance(consumer.getClass().getClassLoader(), new Class[]{Consumer.class}, (proxy, method, args) -> {
            if ("accept".equals(method.getName())) {
                TaskManager.POOL.submit(() -> {
                });
            }
            return method.invoke(consumer, args);
        });
    }

}