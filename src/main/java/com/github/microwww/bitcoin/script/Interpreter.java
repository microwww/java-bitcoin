package com.github.microwww.bitcoin.script;

import com.github.microwww.bitcoin.chain.RawTransaction;
import com.github.microwww.bitcoin.chain.TxOut;
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
    private TxOut preout;
    private ByteBuf script;
    private byte[] scripts;
    protected final BytesStack stack;
    private int lastCodeSeparator = -1;
    private boolean internally = false; // These words are used internally for assisting with transaction matching. They are invalid if used in actual scripts.

    public Interpreter(RawTransaction tx) {
        this(tx, new BytesStack());
    }

    protected Interpreter(RawTransaction tx, BytesStack bytesStack) {
        Assert.isTrue(tx != null, "Not NULL");
        this.transaction = tx.clone();
        stack = bytesStack;
    }

    public Interpreter subScript(boolean internally) {
        Interpreter ch = new Interpreter(this.transaction, this.stack);
        ch.indexTxIn = this.indexTxIn;
        ch.internally = internally;
        return ch;
    }

    public Interpreter subScript() {
        return this.subScript(false);
    }

    public Interpreter indexTxIn(int i) {
        Assert.isTrue(transaction.getTxIns().length >= indexTxIn, "over-flow TX-IN index");
        indexTxIn = i;
        this.preout = null;
        return this;
    }

    public Interpreter indexTxIn(int i, TxOut preout) {
        Assert.isTrue(transaction.getTxIns().length >= indexTxIn, "over-flow TX-IN index");
        indexTxIn = i;
        this.preout = preout;
        return this;
    }

    public Interpreter nextTxIn() {
        indexTxIn(indexTxIn + 1);
        return this;
    }

    public Interpreter nextTxIn(TxOut preout) {
        indexTxIn(indexTxIn + 1, preout);
        return this;
    }

    public Interpreter witnessPushStack() {
        byte[][] pushes = transaction.getTxIns()[indexTxIn].getTxWitness();
        if (pushes != null) {
            for (byte[] push : pushes) {
                this.stack.push(push);
            }
        }
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

        for (TemplateTransaction value : TemplateTransaction.values()) {
            if (value.isSupport(aScript)) {
                value.executor(this);
                break;
            }
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

    public boolean stackSizeEqual(int size) {
        return this.stack.size() == size;
    }

    public boolean topIsTrue() {
        Assert.isTrue(script.readableBytes() == 0, "Script Not done");
        if (!this.stack.isEmpty()) {
            byte[] pk = this.stack.peek();
            if (pk.length > 0) {
                if (pk[0] != 0) {
                    return true;
                }
            }
        }
        return false;
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

    public TxOut getPreout() {
        return preout;
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

    public boolean isInternally() {
        return internally;
    }
}
