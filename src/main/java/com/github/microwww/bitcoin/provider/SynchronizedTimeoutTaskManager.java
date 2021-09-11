package com.github.microwww.bitcoin.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class SynchronizedTimeoutTaskManager<T> {
    private static final Logger logger = LoggerFactory.getLogger(SynchronizedTimeoutTaskManager.class);

    // 下面的4个属性用来实现 队列的取值 和 将该值赋给 current 同步完成
    protected final Queue<T> queue = new LinkedList<>();
    ReentrantLock lock = new ReentrantLock();
    Condition taskCondition = lock.newCondition();

    private final AtomicLong stopTime = new AtomicLong();
    protected final BiConsumer<T, SynchronizedTimeoutTaskManager<T>> consumer;
    private final long waitMilliseconds;
    private T current;
    private Map<String, Object> cache = new ConcurrentHashMap<>();
    private Thread thread;
    protected final Map<Integer, BiConsumer<T, T>> changeListeners = new ConcurrentSkipListMap<>();

    public SynchronizedTimeoutTaskManager(Consumer<T> consumer, int time, TimeUnit unit) {
        this((t, x) -> consumer.accept(t), time, unit);
    }

    public SynchronizedTimeoutTaskManager(BiConsumer<T, SynchronizedTimeoutTaskManager<T>> consumer, int time, TimeUnit unit) {
        waitMilliseconds = unit.convert(time, TimeUnit.MILLISECONDS);
        this.consumer = consumer;
        listener();
    }

    private void listener() {
        TaskManager.POOL.submit(() -> {
            thread = Thread.currentThread();
            while (true) { // 死循环, 除非是中断请求
                try { // 如果没有新的任务, 原先任务仍然可以正常提交 ! 有新的任务, 无法修改 touch !
                    T ct = current;
                    lock.lock();
                    try {
                        while (true) {
                            T newTask;
                            synchronized (this) {
                                newTask = queue.poll();
                                if (newTask != null) {
                                    this.current = newTask;
                                    break;
                                }
                            }
                            if (newTask == null) {
                                logger.debug("Release lock and await, Waiting .....");
                                taskCondition.await();
                            }
                        }
                    } finally {
                        lock.unlock();
                    }
                    T newTask = this.current;
                    changeListeners.forEach((k, v) -> {
                        v.accept(ct, newTask);
                    });
                    TaskManager.POOL.submit(() -> {
                        consumer.accept(newTask, this);
                    });
                    Thread.yield();
                    this.awaitTimeout(newTask);
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
        return me == current;
    }

    public SynchronizedTimeoutTaskManager<T> assertIsMe(T me) {
        return this.assertIsMe(me, "I am timeout");
    }

    public SynchronizedTimeoutTaskManager<T> assertIsMe(T me, String format, Object... args) {
        Assert.isTrue(this.can(me), String.format(format, args));
        return this;
    }

    public boolean touch(T me) {
        return this.touch(me, waitMilliseconds, TimeUnit.MILLISECONDS);
    }

    /**
     * waitMilliseconds * 20
     *
     * @param me
     * @return
     */
    public boolean touchTwentyFold(T me) {
        return this.touch(me, 20 * waitMilliseconds, TimeUnit.MINUTES);
    }

    /**
     * waitMilliseconds * 10
     *
     * @param me
     * @return
     */
    public boolean touchTenFold(T me) {
        return this.touch(me, 10 * waitMilliseconds, TimeUnit.SECONDS);
    }

    private synchronized boolean resetCurrent(T val) {
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
        lock.lock();
        try {// first lock , second this, same as poll
            synchronized (this) {
                queue.add(task);
                taskCondition.signal();
            }
        } finally {
            lock.unlock();
        }
    }

    public synchronized void remove(T e) {
        boolean rm = queue.remove(e);
        if (!rm && can(e)) {
            resetCurrent(e);
        }
    }

    public synchronized Optional<T> getCurrent() {
        return Optional.ofNullable(current);
    }

    public SynchronizedTimeoutTaskManager<T> addChangeListeners(BiConsumer<T, T> cn) {
        return this.addChangeListeners((int) (Math.random() * 100), cn);
    }

    /**
     * @param order
     * @param cn    BiConsumer.accept(T1,T2), T1 source, T2 target,  source maybe null, target never.
     * @return
     */
    public SynchronizedTimeoutTaskManager<T> addChangeListeners(int order, BiConsumer<T, T> cn) {
        changeListeners.put(order, cn);
        return this;
    }

    public <U> U getCache(String key, Class<U> clazz) {
        return (U) cache.get(key);
    }

    public SynchronizedTimeoutTaskManager<T> putCache(String key, Object cache) {
        this.cache.put(key, cache);
        return this;
    }

    public synchronized List<T> getTasks() {
        return new ArrayList<>(queue);
    }
}