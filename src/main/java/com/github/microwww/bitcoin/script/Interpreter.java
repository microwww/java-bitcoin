package com.github.microwww.bitcoin.script;

import com.github.microwww.bitcoin.chain.RawTransaction;
import com.github.microwww.bitcoin.math.Uint8;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.Optional;

public class Interpreter {
    private static final Logger logger = LoggerFactory.getLogger(Interpreter.class);

    protected final RawTransaction transaction;
    private int indexTxIn = 0;
    private ByteBuf script;
    private byte[] scripts;
    protected final BytesStack stack = new BytesStack();
    private int lastCodeSeparator = -1;

    public Interpreter(RawTransaction tx) {
        Assert.isTrue(tx != null, "Not NULL");
        this.transaction = tx.clone();
    }

    public Interpreter indexTxIn(int i) {
        Assert.isTrue(transaction.getTxIns().length >= indexTxIn, "over-flow TX-IN index");
        indexTxIn = i;
        return this;
    }

    public Interpreter nextTxIn() {
        indexTxIn(indexTxIn + 1);
        return this;
    }

    public Interpreter executor(byte[] aScript) {
        Assert.isTrue(aScript != null, "Not NULL");
        this.scripts = aScript;
        this.script = Unpooled.copiedBuffer(aScript);
        while (script.readableBytes() > 0) {
            Uint8 op = new Uint8(script.readByte());
            ScriptNames sn = ScriptNames.values()[op.intValue()];
            if (logger.isDebugEnabled())
                logger.debug("Before Operation : {}, {}", sn.name(), stack.size());
            sn.opt(this);
            if (logger.isDebugEnabled())
                logger.debug("After  Operation : {}, {}", sn.name(), stack.size());
        }
        return this;
    }

    public Optional<byte[]> pop() {
        Assert.isTrue(script.readableBytes() == 0, "Script Not done");
        if (this.stack.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(this.stack.pop());
    }

    ByteBuf getScript() {
        return script;
    }

    public byte[] getScripts() {
        return Arrays.copyOf(scripts, scripts.length);
    }

    public int getIndexTxIn() {
        return indexTxIn;
    }

    /**
     * @return if do not exist return -1
     */
    public int getLastCodeSeparator() {
        return lastCodeSeparator;
    }

    protected void setLastCodeSeparator(int lastCodeSeparator) {
        this.lastCodeSeparator = lastCodeSeparator;
    }
}
