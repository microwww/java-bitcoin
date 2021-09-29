package com.github.microwww.bitcoin.script.instruction;

import com.github.microwww.bitcoin.script.Interpreter;
import com.github.microwww.bitcoin.util.ByteUtil;
import io.netty.buffer.ByteBuf;
import org.springframework.util.Assert;

public abstract class Constants {

    public static PushValue pushValue(byte[] bytes) {
        Assert.isTrue(bytes.length <= 75, "PushValue max <= 75");
        PushValue v = new PushValue(bytes.length);
        v.setOperand(bytes);
        return v;
    }

    public static class PushValue extends AbstractScriptOperand {
        public PushValue(int code) {
            super(code);
        }

        @Override
        public void operand(ByteBuf bf) {
            this.operand = ByteUtil.readLength(bf, code);
        }

        @Override
        public void exec(Interpreter executor) {
            executor.stack.push(operand);
        }
    }

    static class OP_PUSHDATA1 extends AbstractScriptOperand {
        public OP_PUSHDATA1(int code) {
            super(code);
        }

        @Override
        public void operand(ByteBuf bf) {
            int len = Byte.toUnsignedInt(bf.readByte());
            this.operand = ByteUtil.readLength(bf, len);
        }

        @Override
        public void exec(Interpreter executor) {
            executor.stack.push(operand);
        }
    }// 76

    static class OP_PUSHDATA2 extends AbstractScriptOperand {
        public OP_PUSHDATA2(int code) {
            super(code);
        }

        @Override
        public void operand(ByteBuf bf) {
            int len = Short.toUnsignedInt(bf.readShortLE());
            this.operand = ByteUtil.readLength(bf, len);
        }

        @Override
        public void exec(Interpreter executor) {
            executor.stack.push(operand);
        }
    }

    static class OP_PUSHDATA4 extends AbstractScriptOperand {
        public OP_PUSHDATA4(int code) {
            super(code);
        }

        @Override
        public void operand(ByteBuf bf) {
            int len = bf.readIntLE();
            Assert.isTrue(len >= 0, "LEN >= 0");
            this.operand = ByteUtil.readLength(bf, len);
        }

        @Override
        public void exec(Interpreter executor) {
            executor.stack.push(operand);
        }
    }

    //OP_1NEGATE,
    //OP_RESERVED,// 80

    public static class PushCode extends AbstractScriptNoOperand {
        public PushCode(int code) {
            super(code);
        }

        @Override
        public void exec(Interpreter executor) {
            int v = this.opcode() & 0x0F;
            executor.stack.push(new byte[]{(byte) v});
        }
    }
}
