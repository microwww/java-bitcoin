package com.github.microwww.bitcoin.provider;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class TimeoutTaskManagerTest {

    @Test
    @Timeout(5)
    void touch() throws InterruptedException {
        AtomicInteger count = new AtomicInteger();
        TimeoutTaskManager<String> manager = new TimeoutTaskManager<>(v -> {
            count.incrementAndGet();
        }, 500, TimeUnit.MILLISECONDS);

        String val = "0000";
        manager.addTask(val);
        for (int i = 0; i < 10; i++) {
            Thread.sleep(100);
            assertTrue(manager.touch(val));
        }
        Thread.sleep(600);
        Thread.yield();

        assertTrue(manager.touch(val)); // 虽然超时, 但仍然没有被重置, 可以继续使用
        manager.assertIsMe(val);

        manager.addTask(val + "0");
        Thread.sleep(100);
        Thread.yield();
        assertFalse(manager.touch(val));
        try {
            manager.assertIsMe(val);
            fail();
        } catch (IllegalStateException e) {
        }
        assertEquals(2, count.get());
    }
}