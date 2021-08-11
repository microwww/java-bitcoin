package com.github.microwww.bitcoin.script.ins;

import com.github.microwww.bitcoin.script.Instruction;
import com.github.microwww.bitcoin.script.Interpreter;
import com.github.microwww.bitcoin.script.ScriptOperation;
import com.github.microwww.bitcoin.util.ByteUtil;
import io.netty.buffer.ByteBuf;
import org.springframework.util.Assert;

public enum Instruction_4C_50 implements Instruction {
    OP_PUSHDATA1 {
        @Override
        public ScriptOperation compile(ByteBuf bf) {
            int len = Byte.toUnsignedInt(bf.readByte());
            byte[] bytes = ByteUtil.readLength(bf, len);
            return new ScriptOperation(this, bytes);
        }

        @Override
        public void exec(Interpreter executor, Object data) {
            executor.stack.push((byte[]) data);
        }
    },// 76
    OP_PUSHDATA2 {
        @Override
        public ScriptOperation compile(ByteBuf bf) {
            int len = Short.toUnsignedInt(bf.readShortLE());
            byte[] bytes = ByteUtil.readLength(bf, len);
            return new ScriptOperation(this, bytes);
        }

        @Override
        public void exec(Interpreter executor, Object data) {
            executor.stack.push((byte[]) data);
        }
    },
    OP_PUSHDATA4 {
        @Override
        public ScriptOperation compile(ByteBuf bf) {
            int len = bf.readIntLE();
            Assert.isTrue(len >= 0, "LEN >= 0");
            byte[] bytes = ByteUtil.readLength(bf, len);
            return new ScriptOperation(this, bytes);
        }

        @Override
        public void exec(Interpreter executor, Object data) {
            executor.stack.push((byte[]) data);
        }
    },
    OP_1NEGATE,
    OP_RESERVED,// 80
    ;

    @Override
    public ScriptOperation compile(ByteBuf bf) {
        throw new UnsupportedOperationException(this.name());
    }

    @Override
    public void exec(Interpreter executor, Object data) {
        throw new UnsupportedOperationException(this.name());
    }

    public byte opcode() {
        return (byte) (0x4C + this.ordinal());
    }

}
