package com.github.microwww.bitcoin.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Provider 比较是 `=` 而不是 `equals` 方法
 *
 * @param <T>
 */
public class TimeoutTaskManager<T> implements Closeable {
    private static final Logger logger = LoggerFactory.getLogger(TimeoutTaskManager.class);

    // 下面的 3 个属性用来实现 队列的取值 和 将该值赋给 current 同步完成
    protected final Queue<T> queue = new LinkedList<>();
    ReentrantLock lock = new ReentrantLock();
    Condition waiting = lock.newCondition();

    private final AtomicLong stopTime = new AtomicLong();
    protected final BiConsumer<T, TimeoutTaskManager<T>> consumer;
    private final long waitMilliseconds;
    private T current;
    private Map<String, Object> cache = new ConcurrentHashMap<>();
    private Thread thread;
    protected final Map<Integer, BiConsumer<T, T>> changeListeners = new ConcurrentSkipListMap<>();
    private boolean noProvider = true;

    public TimeoutTaskManager(Consumer<T> consumer, int time, TimeUnit unit) {
        this((t, x) -> consumer.accept(t), time, unit);
    }

    public TimeoutTaskManager(BiConsumer<T, TimeoutTaskManager<T>> consumer, int time, TimeUnit unit) {
        waitMilliseconds = TimeUnit.MILLISECONDS.convert(time, unit);
        this.consumer = consumer;
        listener();
    }

    private void listener() {
        CountDownLatch latch = new CountDownLatch(1);
        TaskManager.POOL.submit(() -> {
            thread = Thread.currentThread();
            latch.countDown();
            while (true) { // 死循环, 除非是中断请求
                try { // 如果没有新的任务, 原先任务仍然可以正常提交 ! 有新的任务, 无法修改 touch !
                    T ct = current;
                    lock.lock();
                    try {
                        this.changeProviderOrAwait();
                    } finally {
                        lock.unlock();
                    }
                    T newProvider = this.current;
                    changeListeners.forEach((k, v) -> {
                        v.accept(ct, newProvider);
                    });
                    TaskManager.POOL.submit(() -> {
                        logger.debug("Run new submit task");
                        consumer.accept(newProvider, this);
                    });
                    Thread.yield();
                    this.awaitTimeout(newProvider);
                } catch (RuntimeException ex) {
                    logger.error("Task run error", ex);
                } catch (InterruptedException e) {
                    Thread.interrupted();
                    return;
                }
            }
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.interrupted();
        }
    }

    private void changeProviderOrAwait() throws InterruptedException {
        while (true) {
            T newSupporter;
            synchronized (this) {
                newSupporter = queue.poll();
                if (newSupporter != null) {
                    logger.debug("POLL a new supporter : {}, less: {}", newSupporter, queue.size());
                    this.current = newSupporter;
                    noProvider = false;
                    break;
                }
                noProvider = true;
                logger.debug("No supporter, last provider is: {}", this.current);
            }
            if (newSupporter == null) {
                logger.debug("Release lock and Waiting .....");
                waiting.await();
            }
        }
    }

    private void awaitTimeout(T provider) throws InterruptedException {
        boolean touch = this.touch(provider);
        if (!touch) {
            logger.warn("This is not usually the case ! WHY ? {} --> {}", this.current, provider);
            return;
        }
        while (true) {
            logger.debug("Provider {} waiting timeout !", provider);
            LockSupport.parkUntil(stopTime.get());
            synchronized (this) {
                if (System.currentTimeMillis() > stopTime.get()) {
                    logger.debug("Provider timeout, {}, {}, start new task !", stopTime.get(), provider);
                    break;
                }
                logger.debug("Provider {} not timeout !", provider);
            }
        }
    }

    public synchronized boolean can(T me) {
        runningAssert();
        return me == current;
    }

    public TimeoutTaskManager<T> assertIsMe(T me) throws IllegalStateException {
        return this.assertIsMe(me, "I am timeout: %s", me);
    }

    public synchronized TimeoutTaskManager<T> ifMe(T me, Runnable doing) throws IllegalStateException {
        if (this.can(me)) {
            doing.run();
        }
        return this;
    }

    public TimeoutTaskManager<T> assertIsMe(T me, String format, Object... args) throws IllegalStateException {
        if (!this.can(me)) {
            throw new IllegalStateException(String.format(format, args));
        }
        return this;
    }

    public boolean touch(T me) {
        return this.touch(me, "-");
    }

    public boolean touch(T me, String message) {
        return this.touch(me, waitMilliseconds, TimeUnit.MILLISECONDS, message);
    }

    /**
     * waitMilliseconds * 20
     *
     * @param me
     * @return
     */
    public boolean touchTwentyFold(T me) {
        return this.touchTwentyFold(me, "-");
    }

    public boolean touchTwentyFold(T me, String message) {
        return this.touch(me, 20 * waitMilliseconds, TimeUnit.MINUTES, message);
    }

    /**
     * waitMilliseconds * 10
     *
     * @param me
     * @return
     */
    public boolean touchTenFold(T me) {
        return touchTenFold(me, "-");
    }

    public boolean touchTenFold(T me, String message) {
        return this.touch(me, 10 * waitMilliseconds, TimeUnit.SECONDS, message);
    }

    private synchronized boolean resetCurrent(T val) {
        boolean res = (this.current == val);
        if (res) {
            this.stopTime.set(0);
            LockSupport.unpark(this.thread);
        }
        return res;
    }

    public boolean touch(T me, long time, TimeUnit unit) {
        return touch(me, time, unit, "-");
    }

    public synchronized boolean touch(T me, long time, TimeUnit unit, String message) {
        boolean can = this.can(me);
        if (can) {
            long convert = TimeUnit.MILLISECONDS.convert(time, unit);
            stopTime.set(System.currentTimeMillis() + convert);
            if (logger.isDebugEnabled())
                logger.debug("Reset stop time: {}, {}", new Date(stopTime.get()), message);
        }
        return can;
    }

    public void addProvider(T provider) {
        lock.lock();
        try {// first lock , second this, same as poll
            synchronized (this) {
                runningAssert();
                queue.add(provider);
                waiting.signal();
            }
        } finally {
            lock.unlock();
        }
    }

    public synchronized void remove(T e) {
        runningAssert();
        boolean rm = queue.remove(e);
        if (!rm && can(e)) {
            resetCurrent(e);
        }
    }

    public synchronized Optional<T> getCurrent() {
        return Optional.ofNullable(current);
    }

    public TimeoutTaskManager<T> addChangeListeners(BiConsumer<T, T> cn) {
        return this.addChangeListeners((int) (Math.random() * 100), cn);
    }

    /**
     * @param order
     * @param cn    BiConsumer.accept(T1,T2), T1 source, T2 target,  source maybe null, target never.
     * @return
     */
    public TimeoutTaskManager<T> addChangeListeners(int order, BiConsumer<T, T> cn) {
        changeListeners.put(order, cn);
        return this;
    }

    public <U> U getCache(String key, Class<U> clazz) {
        return (U) cache.get(key);
    }

    public TimeoutTaskManager<T> putCache(String key, Object cache) {
        runningAssert();
        this.cache.put(key, cache);
        return this;
    }

    public synchronized List<T> getProviders() {
        return new ArrayList<>(queue);
    }

    public synchronized boolean isNoProvider() {
        return noProvider;
    }

    private void runningAssert() {
        if (thread.isInterrupted()) {
            throw new RuntimeException(new InterruptedException("TimeoutTaskManager is STOP"));
        }
    }

    @Override
    public void close() throws IOException {
        thread.interrupt();
        synchronized (this) {
            queue.clear();
        }
    }
}