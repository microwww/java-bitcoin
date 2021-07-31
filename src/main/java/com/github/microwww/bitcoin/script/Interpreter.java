package com.github.microwww.bitcoin.script;

import com.github.microwww.bitcoin.math.Uint8;
import com.github.microwww.bitcoin.util.ByteUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.Arrays;

public class Interpreter {
    private static final Logger logger = LoggerFactory.getLogger(Interpreter.class);

    protected final ByteBuf script;
    protected final BytesStack stack = new BytesStack();

    /**
     * @param script 脚本
     * @param init   初始化栈, 按照顺序压栈
     */
    public Interpreter(byte[] script, byte[]... init) {
        Assert.isTrue(script != null, "Not NULL");
        this.script = Unpooled.copiedBuffer(script);
        for (byte[] bytes : init) {
            this.stack.push(Arrays.copyOf(bytes, bytes.length));
        }
    }

    public byte[] executor() {
        while (script.readableBytes() > 0) {
            Uint8 op = new Uint8(script.readByte());
            ScriptNames sn = ScriptNames.values()[op.intValue()];
            if (logger.isDebugEnabled())
                logger.debug("Before-Operation : {}, ", sn.name() + ", " + stack.size() + ", " + ByteUtil.hex(stack.peek()));
            sn.opt(this);
            if (logger.isDebugEnabled())
                logger.debug("After--Operation : {}, ", sn.name() + ", " + stack.size() + ", " + ByteUtil.hex(stack.peek()));
        }
        if (stack.isEmpty()) {
            return null;
        }
        return stack.pop();
    }
}
