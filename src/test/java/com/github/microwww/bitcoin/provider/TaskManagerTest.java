package com.github.microwww.bitcoin.provider;

import static org.junit.jupiter.api.Assertions.*;

import cn.hutool.core.util.ReflectUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

class TaskManagerTest {

    @Test
    @Timeout(2)
    void start() throws InterruptedException {
        AtomicInteger atomicInteger = new AtomicInteger();
        TaskManager<String> tm = new TaskManager<>(3, (s, m) -> {
            m.release(s);
            atomicInteger.incrementAndGet();
        });
        Semaphore semaphore = (Semaphore) ReflectUtil.getFieldValue(tm, "semaphore");
        assertEquals(3, semaphore.availablePermits());

        int count = 100;

        for (int i = 0; i < count; i++) {
            tm.add(String.format("%03d", i));
        }
        while (atomicInteger.get() < count) {
            Thread.yield();
        }
        assertEquals(100, atomicInteger.intValue());
        assertEquals(0, tm.doing.size());
        assertEquals(0, tm.queue.size());
        assertEquals(3, semaphore.availablePermits());
    }
}