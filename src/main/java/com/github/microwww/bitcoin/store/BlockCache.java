package com.github.microwww.bitcoin.store;

import com.github.microwww.bitcoin.math.Uint256;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class BlockCache<T> {
    private static final Logger logger = LoggerFactory.getLogger(BlockCache.class);
    public static int MAX_CACHE = 2 * 24 * 6;
    private Map<Uint256, T> cache = Collections.synchronizedMap(new LinkedHashMap<>(MAX_CACHE + 10)); // 缓存 2 天的块
    private AtomicInteger count = new AtomicInteger();
    private AtomicInteger hit = new AtomicInteger();

    public Optional<T> get(Uint256 key, Supplier<Optional<T>> supplier) {
        int v = count.incrementAndGet();
        if (logger.isDebugEnabled() && v % 100 == 0)
            logger.debug("Cache HIT {}/{} ", hit.intValue(), v);
        T h = cache.get(key);
        if (h == null) {
            return supplier.get();
        }
        hit.incrementAndGet();
        return Optional.of(h);
    }

    public T put(Uint256 key, T value) {
        int size = cache.size();
        int c = size - MAX_CACHE;
        if (c >= 0) {
            logger.debug("Cache size over flow : {}", MAX_CACHE);
            cache.remove(cache.keySet().iterator().next());
        }
        return cache.put(key, value);
    }
}
