package com.github.microwww.bitcoin.chain;

import com.github.microwww.bitcoin.math.Uint256;
import com.github.microwww.bitcoin.math.Uint32;
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

    public ChainBlock createGenesisBlock() {
        return createGenesisBlock(new Uint32(1296688602), new Uint32(2), new Uint32(0x207fffff), 1, 50 * COIN);
    }

    public ChainBlock createGenesisBlock(Uint32 nTime, Uint32 nNonce, Uint32 nBits, int nVersion, long amount) {
        String pszTimestamp = "The Times 03/Jan/2009 Chancellor on brink of second bailout for banks";
        byte[] genesisOutputScript = ByteUtil.hex("04678afdb0fe5548271967f1a67130b7105cd6a828e03909a67962e0ea1f61deb649f6bc3f4cef38c4f35504e51ec112de5c384df7ba0b8d578a4c702b6bf11d5f");
        return createGenesisBlock(pszTimestamp, genesisOutputScript, nTime, nNonce, nBits, nVersion, amount);
    }

    public ChainBlock createGenesisBlock(String pszTimestamp, byte[] genesisOutputScript, Uint32 nTime, Uint32 nNonce, Uint32 nBits, int nVersion, long amount) {
        RawTransaction txNew = new RawTransaction();
        txNew.setVersion(1);

        // txNew.vin[0].scriptSig = CScript() << 486604799 << CScriptNum(4) << std::vector<unsigned char>((const unsigned char*)pszTimestamp, (const unsigned char*)pszTimestamp + strlen(pszTimestamp));
        TxIn in = new TxIn();
        ByteBuf buffer = Unpooled.buffer();
        byte[] msg = pszTimestamp.getBytes(StandardCharsets.ISO_8859_1);
        buffer.writeLong(486604799L)
                .writeByte(1)
                .writeByte(4)
                .writeByte(msg.length)
                .writeBytes(msg);
        in.setPreTxHash(Uint256.ZERO);
        in.setSequence(Uint32.MAX_VALUE);
        in.setScript(ByteUtil.readAll(buffer));

        txNew.setTxIns(new TxIn[]{in});

        TxOut out = new TxOut();
        out.setValue(amount);
        out.setScriptPubKey(genesisOutputScript);

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
        return genesis;
    }

}
