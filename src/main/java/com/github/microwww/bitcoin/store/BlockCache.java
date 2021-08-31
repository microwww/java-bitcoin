package com.github.microwww.bitcoin.store;

import com.github.microwww.bitcoin.math.Uint256;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

public class BlockCache {
    private static final Logger logger = LoggerFactory.getLogger(BlockCache.class);
    public static int MAX_CACHE = 2 * 24 * 6;
    private Map<Uint256, HeightBlock> cache = Collections.synchronizedMap(new LinkedHashMap<>(MAX_CACHE + 10)); // 缓存 2 天的块
    private AtomicInteger count = new AtomicInteger();
    private AtomicInteger hit = new AtomicInteger();

    public Optional<HeightBlock> get(Uint256 key, Callable<Optional<HeightBlock>> supplier) throws Exception {
        int v = count.incrementAndGet();
        if (logger.isDebugEnabled() && v % 100 == 0)
            logger.debug("Cache HIT {}/{} ", hit.intValue(), v);
        HeightBlock h = cache.get(key);
        if (h == null) {
            return supplier.call();
        }
        hit.incrementAndGet();
        return Optional.of(h);
    }

    public HeightBlock put(Uint256 key, HeightBlock value) {
        int size = cache.size();
        int c = size - MAX_CACHE;
        if (c >= 0) {
            logger.debug("Cache size over flow : {}", MAX_CACHE);
            cache.remove(cache.keySet().iterator().next());
        }
        return cache.put(key, value);
    }
}
