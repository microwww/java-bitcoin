package com.github.microwww.bitcoin.store;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class BlockCache<U, T> {
    private static final Logger logger = LoggerFactory.getLogger(BlockCache.class);
    private final int max;
    private final Map<U, T> cache;
    private AtomicInteger count = new AtomicInteger();
    private AtomicInteger hit = new AtomicInteger();

    public BlockCache(int max) {
        this.max = max;
        cache = new LinkedHashMap<>(max + 10);
    }

    public synchronized Optional<T> get(U key, Supplier<T> supplier) {
        int v = count.incrementAndGet();
        if (logger.isDebugEnabled() && v % 100 == 0)
            logger.debug("Cache HIT {}/{} ", hit.intValue(), v);
        T h = cache.get(key);
        if (h == null) {
            return Optional.ofNullable(supplier.get());
        }
        hit.incrementAndGet();
        return Optional.of(h);
    }

    public synchronized T put(U key, T value) {
        int size = cache.size();
        int c = size - max;
        if (c >= 0) {
            logger.debug("Cache size over flow : {}", max);
            cache.remove(cache.keySet().iterator().next());
        }
        return cache.put(key, value);
    }

    public synchronized T remove(U key) {
        return cache.remove(key);
    }

    public synchronized List<U> get(int count) {
        int i = 0;
        List<U> list = new ArrayList<>();
        for (U u : cache.keySet()) {
            if (list.size() >= count) {
                break;
            }
            list.add(u);
        }
        return list;
    }

    public void clear() {
        cache.clear();
    }
}
