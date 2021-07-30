package com.github.microwww.bitcoin.script;

import com.github.microwww.bitcoin.script.ex.ScriptException;
import com.github.microwww.bitcoin.util.ByteUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.Stack;

/**
 * The stacks hold byte vectors.
 * When used as numbers, byte vectors are interpreted as little-endian variable-length integers
 * with the most significant bit determining the sign of the integer.
 */

/**
 * 非线程安全的类
 */
public class BytesStack {
    private Stack<byte[]> stack = new Stack<>();
    private ByteBuf cache = Unpooled.buffer(256);

    public byte[] push(byte[] item) {
        return stack.push(item);
    }

    public byte[] push(int item) {
        cache.clear();
        byte[] bytes = ByteUtil.readLength(cache.writeIntLE(item), 4);
        return stack.push(bytes);
    }

    public byte[] push(long item) {
        cache.clear();
        byte[] bytes = ByteUtil.readLength(cache.writeLongLE(item), 4);
        return stack.push(bytes);
    }

    public byte[] pop() {
        return stack.pop();
    }

    public int popInt() {
        byte[] pop = stack.peek();
        if (pop.length > 4) {
            throw new ScriptException("length > 4");
        }
        cache.clear();
        return cache.writeBytes(stack.pop()).readIntLE();
    }

    public long popLong() {
        byte[] pop = stack.peek();
        if (pop.length > 8) {
            throw new ScriptException("length > 8");
        }
        cache.clear();
        return cache.writeBytes(stack.pop()).readLongLE();
    }

    public byte[] peek() {
        return stack.peek();
    }

    public int peekInt() {
        byte[] pop = stack.peek();
        if (pop.length > 4) {
            throw new ScriptException("length > 4");
        }
        return cache.clear().writeBytes(pop).readIntLE();
    }

    public long peekLong() {
        byte[] pop = stack.peek();
        if (pop.length > 8) {
            throw new ScriptException("length > 8");
        }
        return cache.clear().writeBytes(pop).readLongLE();
    }

    public boolean empty() {
        return stack.empty();
    }

    public BytesStack assertNotEmpty() {
        if (stack.size() > 0) {
            return this;
        } else throw new IllegalArgumentException("STACK SIZE not empty");
    }

    public BytesStack assertSizeGE(int size) {
        if (stack.size() >= size) {
            return this;
        } else throw new IllegalArgumentException("STACK SIZE < " + size);
    }

    public int size() {
        return stack.size();
    }

    public boolean isEmpty() {
        return stack.isEmpty();
    }
}
