package com.github.microwww.bitcoin.store;

import com.github.microwww.bitcoin.math.Uint256;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class BlockCache {
    private static final Logger logger = LoggerFactory.getLogger(BlockCache.class);
    private Map<Uint256, HeightBlock> cache = new ConcurrentHashMap<>(2 * 24 * 6); // 缓存 2 天的块
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
        return cache.put(key, value);
    }
}
