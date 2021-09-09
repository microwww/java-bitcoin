package com.github.microwww.bitcoin.provider;

import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class TaskManager<T> {
    public static final ExecutorService POOL = Executors.newCachedThreadPool();

    private final Semaphore semaphore;
    private final Consumer<T> consumer;
    final ExecutorService executor;
    final Map<T, Object> doing = new ConcurrentHashMap<>();
    final BlockingQueue<T> queue = new LinkedBlockingQueue<>();

    public TaskManager(int max, Consumer<T> consumer) {
        this(max, consumer, POOL);
    }

    public TaskManager(int max, Consumer<T> consumer, ExecutorService executor) {
        semaphore = new Semaphore(max);
        this.executor = executor;
        this.consumer = consumer;
    }

    public void add(T task) {
        queue.add(task);
        next();
    }

    /**
     * new thread to get and run
     */
    public void next() {
        if (semaphore.tryAcquire()) {
            if (queue.isEmpty()) {
                semaphore.release();
                return;
            }
            executor.submit(() -> {
                try {
                    T take = queue.take();
                    doing.put(take, take);
                    consumer.accept(take);
                    return take;
                } catch (Exception ex) {
                    semaphore.release();
                    throw ex;
                }
            });
        }
    }

    public int waiting() {
        return queue.size();
    }

    public int doing() {
        return doing.size();
    }

    public void release(Runnable run) {
        Object remove = doing.remove(run);
        if (remove != null) {
            semaphore.release();
        }
    }

    public void remove(T e) {
        queue.remove(e);
        doing.remove(e);
    }
}