package com.github.microwww.bitcoin.script;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.ArrayList;
import java.util.List;

public class Compiler {
    private final ByteBuf script;

    public Compiler(byte[] bytes) {
        this(bytes, 0);
    }

    public Compiler(byte[] bytes, int offset) {
        this.script = Unpooled.copiedBuffer(bytes);
        this.script.readerIndex(offset);
    }

    public List<SourceCode> compile() {
        List<SourceCode> list = new ArrayList<>();
        while (script.readableBytes() > 0) {
            byte code = script.readByte();
            int i = script.readerIndex();
            ScriptOperation compile = Instructions.SET.select(code).compile(script);
            list.add(new SourceCode(compile, i, script.readerIndex() - i));
        }
        return list;
    }

    public class SourceCode {
        public final ScriptOperation opt;
        public final int position;
        public final int length;

        public SourceCode(ScriptOperation opt, int position, int length) {
            this.opt = opt;
            this.position = position;
            this.length = length;
        }

        public byte[] getBeforeSource() {
            byte[] bytes = new byte[position];
            script.getBytes(0, bytes);
            return bytes;
        }

        public byte[] getRemainingSource() {
            int p = position + length;
            byte[] bytes = new byte[script.writerIndex() - p];
            script.getBytes(p, bytes);
            return bytes;
        }
    }
}
