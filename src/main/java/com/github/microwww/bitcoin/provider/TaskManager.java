package com.github.microwww.bitcoin.provider;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class TaskManager<T> {
    public static final ExecutorService POOL = Executors.newCachedThreadPool();

    private final Semaphore semaphore;
    private final BiConsumer<T, TaskManager<T>> consumer;
    final ExecutorService executor;
    final Map<T, Object> doing = new HashMap<>();
    final Queue<T> queue = new LinkedList<>();

    public TaskManager(int max, Consumer<T> consumer) {
        this(max, consumer, POOL);
    }

    public TaskManager(int max, BiConsumer<T, TaskManager<T>> consumer) {
        this(max, consumer, POOL);
    }

    public TaskManager(int max, Consumer<T> consumer, ExecutorService executor) {
        this(max, (t, u) -> consumer.accept(t), executor);
    }

    public TaskManager(int max, BiConsumer<T, TaskManager<T>> consumer, ExecutorService executor) {
        semaphore = new Semaphore(max);
        this.executor = executor;
        this.consumer = consumer;
    }

    public synchronized void add(T task) {
        queue.add(task);
        next();
    }

    /**
     * new thread to get and run
     */
    public void next() {
        boolean ok = semaphore.tryAcquire();
        if (ok) {
            T take;
            synchronized (this) {
                take = queue.poll();
                if (take == null) {
                    semaphore.release();
                    return;
                }
                doing.put(take, take);
            }
            executor.submit(() -> {
                try {
                    consumer.accept(take, this);
                    return take;
                } catch (Exception ex) {
                    semaphore.release();
                    throw ex;
                }
            });
        }
    }

    public synchronized int waiting() {
        return queue.size();
    }

    public synchronized boolean isEmpty() {
        return doing.isEmpty() && queue.isEmpty();
    }

    public synchronized int doing() {
        return doing.size();
    }

    public synchronized void release(T run) {
        Object remove = doing.remove(run);
        if (remove != null) {
            semaphore.release();
        }
        next();
    }

    public synchronized void remove(T e) {
        queue.remove(e);
        release(e);
    }
}