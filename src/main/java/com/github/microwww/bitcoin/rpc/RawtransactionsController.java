package com.github.microwww.bitcoin.rpc;

import com.github.microwww.bitcoin.chain.RawTransaction;
import com.github.microwww.bitcoin.chain.TxOut;
import com.github.microwww.bitcoin.conf.CChainParams;
import com.github.microwww.bitcoin.math.Uint256;
import com.github.microwww.bitcoin.model.ScriptPubKey;
import com.github.microwww.bitcoin.model.ScriptSig;
import com.github.microwww.bitcoin.model.TxVin;
import com.github.microwww.bitcoin.model.TxVout;
import com.github.microwww.bitcoin.script.PubKeyScript;
import com.github.microwww.bitcoin.script.instruction.ScriptNames;
import com.github.microwww.bitcoin.store.DiskBlock;
import com.github.microwww.bitcoin.store.IndexTransaction;
import com.github.microwww.bitcoin.util.ByteUtil;
import com.github.microwww.bitcoin.wallet.Env;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Optional;

/**
 * analyzepsbt "psbt"
 * combinepsbt ["psbt",...]
 * combinerawtransaction ["hexstring",...]
 * converttopsbt "hexstring" ( permitsigdata iswitness )
 * createpsbt [{"txid":"hex","vout":n,"sequence":n},...] [{"address":amount},{"data":"hex"},...] ( locktime replaceable )
 * createrawtransaction [{"txid":"hex","vout":n,"sequence":n},...] [{"address":amount},{"data":"hex"},...] ( locktime replaceable )
 * decodepsbt "psbt"
 * decoderawtransaction "hexstring" ( iswitness )
 * decodescript "hexstring"
 * finalizepsbt "psbt" ( extract )
 * fundrawtransaction "hexstring" ( options iswitness )
 * getrawtransaction "txid" ( verbose "blockhash" )
 * joinpsbts ["psbt",...]
 * sendrawtransaction "hexstring" ( allowhighfees )
 * signrawtransaction "hexstring" ( [{"txid":"hex","vout":n,"scriptPubKey":"hex","redeemScript":"hex","amount":amount},...] ) ["privatekey",...] ( "sighashtype" )
 * signrawtransactionwithkey "hexstring" ["privatekey",...] ( [{"txid":"hex","vout":n,"scriptPubKey":"hex","redeemScript":"hex","witnessScript":"hex","amount":amount},...] "sighashtype" )
 * testmempoolaccept ["rawtx",...] ( allowhighfees )
 * utxoupdatepsbt "psbt"
 */
@RestController
public class RawtransactionsController {
    @Autowired
    CChainParams chainParams;
    @Autowired
    DiskBlock diskBlock;
    @Autowired
    IndexTransaction indexTransaction;

    public RawtransactionsController(CChainParams chainParams, DiskBlock diskBlock, IndexTransaction indexTransaction) {
        this.chainParams = chainParams;
        this.diskBlock = diskBlock;
        this.indexTransaction = indexTransaction;
    }

    /**
     * getrawtransaction "txid" ( verbose "blockhash" )
     */
    // TODO:: not set !
    @GetMapping("/api/getrawtransaction")
    public RpcRawTransaction getRawTransaction(
            @RequestParam String txid,
            @RequestParam(defaultValue = "0") int verbose,
            @RequestParam(required = false) String blockhash
    ) {
        Uint256 hash = new Uint256(ByteUtil.hexReverse(txid));
        Optional<RawTransaction> raw = indexTransaction.getTransaction(hash);
        if (raw.isPresent()) {
            RpcRawTransaction res = new RpcRawTransaction();
            RawTransaction tx = raw.get();
            ByteBuf buffer = Unpooled.buffer();
            tx.getBlockHash().flatMap(diskBlock::getChinBlock).ifPresent(e -> {
                res.setBlockhash(e.hash().toHexReverse256());
                res.setBlocktime(e.header.getTime().intValue());
                res.setConfirmations(diskBlock.getLastBlock().getHeight() - e.getHeight());
            });
            res.setHash(tx.whash().toHexReverse256());
            byte[] bytes = ByteUtil.readAll(tx.serialization(buffer.clear()));
            res.setHex(ByteUtil.hex(bytes));
            res.setLocktime(tx.getLockTime().intValue());
            res.setSize(bytes.length);
            // res.setTime();
            res.setTxid(tx.txid().toHexReverse256());
            res.setVersion(tx.getVersion());
            TxVin[] vins = Arrays.stream(tx.getTxIns()).map(e -> {
                TxVin in = new TxVin();
                in.setSequence(e.getSequence().longValue());
                if (e.getPreTxOutIndex() < 0) {
                    in.setCoinbase(ByteUtil.hex(e.getScript()));
                } else {
                    in.setTxid(e.getPreTxHash().toHexReverse256());
                    in.setVout(e.getPreTxOutIndex());
                    ScriptSig ss = in.getScriptSig();
                    ss.setHex(ByteUtil.hex(e.getScript()));
                    ss.setAsm(ScriptNames.beautify(e.getScript()).toString());
                    Optional<byte[][]> tw = e.getTxWitness();
                    if (tw.isPresent()) {
                        String[] wts = Arrays.stream(tw.get()).map(ByteUtil::hex).toArray(String[]::new);
                        in.setTxinwitness(wts);
                    }
                }
                return in;
            }).toArray(TxVin[]::new);
            res.setVin(vins);
            TxVout[] outs = new TxVout[tx.getTxOuts().length];
            for (int i = 0; i < outs.length; i++) {
                TxOut e = tx.getTxOuts()[i];
                TxVout out = new TxVout();
                out.setN(i);
                out.setValue(e.toBTC());
                out.setScriptPubKey(new ScriptPubKey());
                ScriptPubKey pk = out.getScriptPubKey();
                pk.setReqSigs(pk.getReqSigs());
                pk.setHex(ByteUtil.hex(e.getScriptPubKey()));
                pk.setAsm(ScriptNames.beautify(e.getScriptPubKey()).toString());
                PubKeyScript st = e.getScriptTemplate();
                pk.setType(st.getType().name());
                Env env = chainParams.env.addressType();
                st.getAddress(tx, env).ifPresent(addr -> {
                    pk.setAddresses(new String[]{addr});
                });
                outs[i] = out;
            }
            res.setVout(outs);
            // res.setVsize();
            // res.setWeight();
            return res;
        }
        return null;
    }

    public static class RpcRawTransaction extends com.github.microwww.bitcoin.model.RawTransaction {
    }

}
