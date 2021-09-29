package com.github.microwww.bitcoin.wallet;

import com.github.microwww.bitcoin.chain.RawTransaction;
import com.github.microwww.bitcoin.chain.TxIn;
import com.github.microwww.bitcoin.chain.TxOut;
import com.github.microwww.bitcoin.math.Uint256;
import com.github.microwww.bitcoin.math.Uint32;

import java.nio.charset.StandardCharsets;

public class GenTransaction {
    public static final GenTransaction G = new GenTransaction();

    private GenTransaction() {
    }

    public RawTransaction genCoinbaseTransaction(String coinbase, long amount, byte[] scriptPubKey) {
        TxOut out = new TxOut();
        out.setValue(amount);
        out.setScriptPubKey(scriptPubKey);
        return this.genCoinbaseTransaction(coinbase, new TxOut[]{out});
    }

    public RawTransaction genCoinbaseTransaction(String coinbase, TxOut... outs) {
        RawTransaction tx = new RawTransaction();
        tx.setVersion(1);
        TxIn in = new TxIn();
        in.setPreTxHash(Uint256.ZERO);
        in.setPreTxOutIndex(-1);
        in.setScript(coinbase.getBytes(StandardCharsets.UTF_8));
        in.setSequence(Uint32._ONE);
        tx.setTxIns(new TxIn[]{in});
        tx.setTxOuts(outs);
        tx.setLockTime(Uint32.ZERO);
        return tx;
    }
}
