package com.github.microwww.bitcoin.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Proxy;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

public class TaskTimeoutManager<T, U> {
    private static final Logger logger = LoggerFactory.getLogger(TaskTimeoutManager.class);
    private static final ThreadLocal<Long> taskFlag = new ThreadLocal();

    private final AtomicLong stop = new AtomicLong(); // 配合 taskFlag, 每次启动任务都会 +1(任何时候都只有一个任务在执行), 这样只有对应值的任务才可以设置状态

    private final TaskManager<T> taskManager;
    private final long timeoutMilliseconds;
    private AtomicLong currentTime = new AtomicLong();
    private Condition condition = new ReentrantLock().newCondition();
    private U cache;

    public TaskTimeoutManager(Consumer<T> consumer, int time, TimeUnit unit) {
        timeoutMilliseconds = unit.convert(time, TimeUnit.MILLISECONDS);
        taskManager = new TaskManager(1, proxy(consumer));
    }

    private Consumer<T> proxy(Consumer<T> consumer) {
        return (Consumer<T>) Proxy.newProxyInstance(consumer.getClass().getClassLoader(), new Class[]{Consumer.class}, (proxy, method, args) -> {
            if ("accept".equals(method.getName())) {
                taskFlag.set(stop.incrementAndGet());
                TaskManager.POOL.submit(() -> {
                    try {
                        this.touch();
                        while (true) {
                            boolean active = condition.awaitUntil(new Date(currentTime.get()));
                            synchronized (this) {
                                if (active || System.currentTimeMillis() > currentTime.get()) {
                                    stop.incrementAndGet();
                                    break;
                                }
                            }
                        }
                        synchronized (taskManager) {
                            Iterator<T> iterator = taskManager.doing.keySet().iterator();
                            if (iterator.hasNext()) {
                                taskManager.release(iterator.next());
                            }
                        }
                    } catch (InterruptedException e) {
                        Thread.interrupted();
                        return;
                    }
                });
            }
            return method.invoke(consumer, args);
        });
    }

    public boolean can() {
        Long v = taskFlag.get();
        return stop.get() == v.longValue();
    }

    public void touch() {
        this.touch(timeoutMilliseconds, TimeUnit.MILLISECONDS);
    }

    public void touchOneMinutes() {
        this.touch(1, TimeUnit.MINUTES);
    }

    public synchronized void touch(long time, TimeUnit unit) {
        if (this.can()) {
            long convert = unit.convert(time, TimeUnit.MILLISECONDS);
            currentTime.set(System.currentTimeMillis() + convert);
        } else {
            throw new IllegalStateException("New Task has start !");
        }
    }

    public TaskManager<T> getTaskManager() {
        return taskManager;
    }

    public U getCache() {
        return cache;
    }

    public TaskTimeoutManager<T, U> setCache(U cache) {
        this.cache = cache;
        return this;
    }
}