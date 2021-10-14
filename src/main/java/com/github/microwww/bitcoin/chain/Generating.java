package com.github.microwww.bitcoin.chain;

import com.github.microwww.bitcoin.conf.CChainParams;
import com.github.microwww.bitcoin.math.Uint256;
import com.github.microwww.bitcoin.math.Uint32;
import com.github.microwww.bitcoin.script.PubKeyScript;
import com.github.microwww.bitcoin.util.ByteUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.nio.charset.StandardCharsets;

public class Generating {
    public static final long COIN = 100_000_000;
    public static final long MAX_MONEY = 21_000_000 * COIN;

    public boolean MoneyRange(long nValue) {
        return (nValue >= 0 && nValue <= MAX_MONEY);
    }

    public static ChainBlock createGenesisBlock(Uint32 nTime, Uint32 nNonce, Uint32 nBits, int nVersion, long amount) {
        String pszTimestamp = "The Times 03/Jan/2009 Chancellor on brink of second bailout for banks";
        byte[] genesisOutputScript = ByteUtil.hex("04678afdb0fe5548271967f1a67130b7105cd6a828e03909a67962e0ea1f61deb649f6bc3f4cef38c4f35504e51ec112de5c384df7ba0b8d578a4c702b6bf11d5f");

        ByteBuf buffer = Unpooled.buffer();
        byte[] msg = pszTimestamp.getBytes(StandardCharsets.ISO_8859_1);
        buffer.writeByte(0x04).writeBytes(new byte[]{(byte) 0xff, (byte) 0xff, 0x00, 0x1d})
                .writeByte(0x01).writeBytes(new byte[]{0x04})
                .writeByte(msg.length)
                .writeBytes(msg);
        return createGenesisBlock(ByteUtil.readAll(buffer), genesisOutputScript, nTime, nNonce, nBits, nVersion, amount);
    }

    private static ChainBlock createGenesisBlock(byte[] coinbase, byte[] pk, Uint32 nTime, Uint32 nNonce, Uint32 nBits, int nVersion, long amount) {
        RawTransaction txNew = new RawTransaction();
        txNew.setVersion(1);

        // txNew.vin[0].scriptSig = CScript() << 486604799 << CScriptNum(4) << std::vector<unsigned char>((const unsigned char*)pszTimestamp, (const unsigned char*)pszTimestamp + strlen(pszTimestamp));
        TxIn in = new TxIn();
        in.setScript(coinbase);
        in.setPreTxHash(Uint256.ZERO);
        in.setPreTxOutIndex(-1);
        in.setSequence(Uint32.MAX_VALUE);

        txNew.setTxIns(new TxIn[]{in});

        TxOut out = new TxOut();
        out.setValue(amount);

        ByteBuf bf = PubKeyScript.Type.P2PK.scriptPubKey(pk);

        out.setScriptPubKey(ByteUtil.readAll(bf));
        txNew.setTxOuts(new TxOut[]{out});
        txNew.setLockTime(Uint32.ZERO);

        ChainBlock genesis = new ChainBlock();
        genesis.header.setTime(nTime);
        genesis.header.setBits(nBits);
        genesis.header.setNonce(nNonce);
        genesis.header.setVersion(nVersion);
        genesis.setTxs(new RawTransaction[]{txNew});
        genesis.header.setPreHash(Uint256.ZERO);
        genesis.header.setMerkleRoot(genesis.merkleRoot());

        genesis.header.setHeight(0);
        txNew.setBlockHash(genesis.hash());

        return genesis;
    }

    public static long getBlockSubsidy(int nHeight, CChainParams.Env env) {
        int halvings = nHeight / env.params.getSubsidyHalvingInterval();
        // Force block reward to zero when right shift is undefined.
        if (halvings >= 64)
            return 0;

        long nSubsidy = 50 * COIN;
        // Subsidy is cut in half every 210,000 blocks which will occur approximately every 4 years.
        nSubsidy >>= halvings;
        return nSubsidy;
    }
}
