package com.github.microwww.bitcoin.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class SynchronizedTimeoutTaskManager<T, U> {
    private static final Logger logger = LoggerFactory.getLogger(SynchronizedTimeoutTaskManager.class);

    private final AtomicLong stopTime = new AtomicLong();

    protected final BlockingQueue<T> queue = new LinkedBlockingQueue<>();
    protected final BiConsumer<T, SynchronizedTimeoutTaskManager<T, U>> consumer;

    private final long waitMilliseconds;
    private T current;
    private U cache;
    private Thread thread;

    public SynchronizedTimeoutTaskManager(Consumer<T> consumer, int time, TimeUnit unit) {
        this((t, x) -> consumer.accept(t), time, unit);
    }

    public SynchronizedTimeoutTaskManager(BiConsumer<T, SynchronizedTimeoutTaskManager<T, U>> consumer, int time, TimeUnit unit) {
        waitMilliseconds = unit.convert(time, TimeUnit.MILLISECONDS);
        this.consumer = consumer;
        listener();
    }

    private void listener() {
        TaskManager.POOL.submit(() -> {
            thread = Thread.currentThread();
            while (true) { // 死循环, 除非是中断请求
                try { // 如果没有新的任务, 原先任务仍然可以正常提交 ! 有新的任务, 无法修改 touch !
                    T task = queue.poll(1, TimeUnit.MINUTES);
                    if (task != null) {
                        this.current = task;
                        TaskManager.POOL.submit(() -> consumer.accept(task, this));
                        this.awaitTimeout(task);
                    } else {
                        logger.debug("Waiting .....");
                    }
                } catch (RuntimeException ex) {
                    logger.error("Task run error", ex);
                } catch (InterruptedException e) {
                    Thread.interrupted();
                    return;
                }
            }
        });
    }

    private void awaitTimeout(T task) throws InterruptedException {
        boolean touch = this.touch(task);
        if (!touch) {
            return;
        }
        while (true) {
            LockSupport.parkUntil(stopTime.get());
            synchronized (this) {
                if (System.currentTimeMillis() > stopTime.get()) {
                    break;
                }
            }
        }
    }

    public synchronized boolean can(T me) {
        if (current == null || me == current) {
            return true;
        }
        return false;
    }

    public synchronized void assertIsMe(T me) {
        Assert.isTrue(this.can(me), "I am timeout");
    }

    public boolean touch(T me) {
        return this.touch(me, waitMilliseconds, TimeUnit.MILLISECONDS);
    }

    public boolean touchOneMinutes(T me) {
        return this.touch(me, 1, TimeUnit.MINUTES);
    }

    public boolean touchFiveSeconds(T me) {
        return this.touch(me, 5, TimeUnit.SECONDS);
    }

    private synchronized boolean setCurrent(T val) {
        boolean res = this.current == val;
        if (res) {
            this.stopTime.set(0);
            LockSupport.unpark(this.thread);
        }
        return res;
    }

    public synchronized boolean touch(T me, long time, TimeUnit unit) {
        boolean can = this.can(me);
        if (can) {
            long convert = TimeUnit.MILLISECONDS.convert(time, unit);
            stopTime.set(System.currentTimeMillis() + convert);
        }
        return can;
    }

    public void addTask(T task) {
        queue.add(task);
    }

    public Optional<T> getCurrent() {
        return Optional.ofNullable(current);
    }

    public U getCache() {
        return cache;
    }

    public SynchronizedTimeoutTaskManager<T, U> setCache(U cache) {
        this.cache = cache;
        return this;
    }
}