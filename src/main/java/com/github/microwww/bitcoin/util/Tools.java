package com.github.microwww.bitcoin.util;

import org.slf4j.Logger;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class Tools {
    private Tools() {
    }

    public static Logger timeLogger(Logger logger, int cycleSeconds) {
        long cycle = 1000 * cycleSeconds;
        return (Logger) Proxy.newProxyInstance(logger.getClass().getClassLoader(), new Class[]{Logger.class}, new InvocationHandler() {
            private long current = 0;

            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if (method.getReturnType().isAssignableFrom(Boolean.TYPE)) { // is???Enable
                    long next = System.currentTimeMillis();
                    return next - current > cycle && (boolean) method.invoke(logger, args);
                }
                switch (method.getName()) {
                    case "debug":
                    case "info":
                    case "warn":
                    case "error": {
                        long next = System.currentTimeMillis();
                        if (logger.isDebugEnabled() || next - current > cycle) {
                            current = next;
                            return method.invoke(logger, args);
                        }
                        return null;
                    }
                }
                return method.invoke(logger, args);
            }
        });
    }
}
