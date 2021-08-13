package com.github.microwww.bitcoin.script;

import com.github.microwww.bitcoin.script.ex.ScriptException;
import com.github.microwww.bitcoin.util.ByteUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger logger = LoggerFactory.getLogger(BytesStack.class);
    public static final int MAX_SCRIPT_ELEMENT_SIZE = 520;
    private Stack<byte[]> stack = new Stack<>();
    private ByteBuf cache = Unpooled.buffer(520);

    public BytesStack push(byte[] item) {
        if (item.length > MAX_SCRIPT_ELEMENT_SIZE) {
            throw new IllegalArgumentException("Max : " + MAX_SCRIPT_ELEMENT_SIZE);
        }
        stack.push(item);
        return this;
    }

    public BytesStack push(int item) {
        cache.clear();
        byte[] bytes = ByteUtil.readLength(cache.writeIntLE(item), 4);
        stack.push(bytes);
        return this;
    }

    public BytesStack push(long item) {
        cache.clear();
        byte[] bytes = ByteUtil.readLength(cache.writeLongLE(item), 4);
        stack.push(bytes);
        return this;
    }

    public byte[] pop() {
        return stack.pop();
    }

    public int popInt() {
        byte[] pop = stack.peek();
        if (pop.length != 4) {
            throw new ScriptException("length != 4");
        }
        cache.clear();
        return cache.writeBytes(stack.pop()).readIntLE();
    }

    public boolean peekSuccess() {
        byte[] pop = stack.peek();
        if (pop.length != 4) {
            throw new ScriptException("length != 4");
        }
        return cache.writeBytes(pop).readIntLE() == 1;
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

    /**
     * @param index from 0
     * @return
     */
    public byte[] peek(int index) {
        return stack.get(stack.size() - index - 1);
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

    public BytesStack clear() {
        stack.clear();
        return this;
    }

    public BytesStack assertNotEmpty() {
        if (stack.size() > 0) {
            return this;
        } else throw new IllegalArgumentException("STACK SIZE not empty");
    }

    public BytesStack assertSizeGE(int size) {
        return this.assertSizeGE(size, "STACK SIZE < " + size);
    }

    public BytesStack assertSizeGE(int size, String message) {
        if (stack.size() >= size) {
            return this;
        } else throw new IllegalArgumentException(message);
    }

    public int size() {
        return stack.size();
    }

    public boolean isEmpty() {
        return stack.isEmpty();
    }

    public void print() {
        if (!logger.isDebugEnabled()) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Bottom").append("\n");
        stack.forEach(e -> {
            sb.append("    0x").append(ByteUtil.hex(e)).append("\n");
        });
        sb.append("Top");
        logger.debug("stack : \n{}", sb);
    }
}
