package com.github.microwww.bitcoin.script;

import com.github.microwww.bitcoin.script.instruction.Script;
import com.github.microwww.bitcoin.script.instruction.ScriptNames;
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
            int code = Byte.toUnsignedInt(script.readByte());
            int i = script.readerIndex();
            Script compile = ScriptNames.values()[code].operand(script);
            list.add(new SourceCode(compile, i, script.readerIndex() - i));
        }
        return list;
    }

    public class SourceCode {
        public final Script script;
        public final int position;
        public final int length;

        public SourceCode(Script script, int position, int length) {
            this.script = script;
            this.position = position;
            this.length = length;
        }

        public byte[] getBeforeSource() {
            byte[] bytes = new byte[position];
            Compiler.this.script.getBytes(0, bytes);
            return bytes;
        }

        public byte[] getRemainingSource() {
            int p = position + length;
            byte[] bytes = new byte[Compiler.this.script.writerIndex() - p];
            Compiler.this.script.getBytes(p, bytes);
            return bytes;
        }

        public Script getScript() {
            return script;
        }
    }
}
