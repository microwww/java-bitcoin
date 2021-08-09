package com.github.microwww.bitcoin.script;

import com.github.microwww.bitcoin.chain.RawTransaction;
import com.github.microwww.bitcoin.chain.TxOut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.*;
import java.util.function.Function;

public class Interpreter {
    private static final Logger logger = LoggerFactory.getLogger(Interpreter.class);

    protected final RawTransaction transaction;
    private int indexTxIn = 0;
    private TxOut preout;
    private List<Compiler.SourceCode> script = Collections.EMPTY_LIST;
    private byte[] scripts;
    protected final BytesStack stack;
    private int lastCodeSeparator = -1;
    private boolean internally = false; // These words are used internally for assisting with transaction matching. They are invalid if used in actual scripts.
    private Map<String, Function<byte[], byte[]>> preProcess = new LinkedHashMap<>();

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
        Iterator<String> iterator = preProcess.keySet().iterator();
        while (iterator.hasNext()) {
            aScript = preProcess.get(iterator.next()).apply(aScript);
        }
        this.script = new Compiler(aScript).compile();
        for (Compiler.SourceCode sc : script) {
            ScriptOperation sn = sc.opt;
            if (logger.isDebugEnabled())
                logger.debug("Before Operation : {}, {}", sn, stack.size());
            sn.exec(this, sc.position);
            if (logger.isDebugEnabled())
                logger.debug("After  Operation : {}, {}", sn, stack.size());
        }

        for (TemplateTransaction value : TemplateTransaction.values()) {
            if (value.isSupport(aScript)) {
                value.executor(this);
                break;
            }
        }

        return this;
    }

    public void addPreProcess(String key, Function<byte[], byte[]> preProcess) {
        this.preProcess.put(key, preProcess);
    }

    public void removePreProcess(String key) {
        this.preProcess.remove(key);
    }

    public Optional<byte[]> peek() {
        if (this.stack.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(this.stack.peek());
    }

    public boolean stackSizeEqual(int size) {
        return this.stack.size() == size;
    }

    public boolean topIsTrue() {
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
