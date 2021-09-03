package com.github.microwww.bitcoin.chain;

import com.github.microwww.bitcoin.math.Uint256;
import com.github.microwww.bitcoin.math.Uint32;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.math.BigInteger;
import java.util.function.Function;

public class PowDifficulty {
    private static final Logger logger = LoggerFactory.getLogger(PowDifficulty.class);

    public static final Uint32 bnProofOfWorkLimit = new Uint32(0x1d00ffff);
    public static final BigInteger nProofOfWorkLimit = difficultyUncompress(bnProofOfWorkLimit);
    public static final int nTargetTimespan = 14 * 24 * 60 * 60; // two weeks 单位秒
    public static final int nTargetSpacing = 10 * 60;  //理想状态 每10分钟生成一个区块
    public static final int nInterval = nTargetTimespan / nTargetSpacing;  //每2016个区块调整一次难度

    public static Uint32 nextWorkRequired(ChainHeight pindexLast, Function<Integer, ChainBlock> pre2016) {

        // Genesis block
        if (pindexLast == null)  //创世区块采用系统定义的最小难度值
            return bnProofOfWorkLimit;

        // Only change once per interval
        if ((pindexLast.getHeight() + 1) % nInterval != 0)
            return pindexLast.getChainBlock().header.getBits();

        // Go back by what we want to be 14 days worth of blocks
        ChainBlock pindexFirst = pre2016.apply(pindexLast.getHeight() + 1 - nInterval);

        // Limit adjustment step
        long nActualTimespan = pindexLast.getChainBlock().header.getTime().longValue() - pindexFirst.header.getTime().longValue();
        return timespan(pindexLast.getChainBlock().header.getBits(), nActualTimespan / 1000);
    }

    /**
     * @param oBits
     * @param nActualTimespan 间隔的时间, 单位: 秒
     * @return
     */
    public static Uint32 timespan(Uint32 oBits, long nActualTimespan) {
        logger.debug("Adjustment nActualTimespan = {}  before bounds", nActualTimespan);
        if (nActualTimespan < nTargetTimespan / 4)    //限定nActualTimespan 最小最大值
            nActualTimespan = nTargetTimespan / 4;
        if (nActualTimespan > nTargetTimespan * 4)
            nActualTimespan = nTargetTimespan * 4;

        // Retarget
        //新的难度值 = 旧难度值 * （nActualTimespan/nTargetTimespan）
        BigInteger dBits = difficultyUncompress(oBits);
        BigInteger bnNew = dBits.multiply(BigInteger.valueOf(nActualTimespan)).divide(BigInteger.valueOf(nTargetTimespan));

        if (bnNew.compareTo(nProofOfWorkLimit) > 0)  //bnProofOfWorkLimit 最小难度值
            bnNew = nProofOfWorkLimit;

        Uint32 nBits = difficultyCompress(bnNew);

        /// debug print
        if (logger.isDebugEnabled()) {
            logger.debug("GetNextWorkRequired RETARGET *****");
            logger.debug("nTargetTimespan = {}    nActualTimespan = {}", nTargetTimespan, nActualTimespan);
            logger.debug("Before: {}  {}", oBits, dBits);
            logger.debug("After:  {}  {}", nBits, new Uint256(bnNew));
        }
        return nBits;     //返回新的难度对应的 nBits 值
    }


    public static BigInteger difficultyUncompress(Uint32 compact) {
        long nCompact = compact.longValue();
        int nSize = (int) (nCompact >> 24);
        Assert.isTrue((nCompact & 0x00800000) == 0, "difficulty not negative");
        BigInteger nWord = BigInteger.valueOf(nCompact & 0x007fffff);
        if (nSize <= 3) {
            nWord = nWord.shiftRight(8 * (3 - nSize));
        } else {
            nWord = nWord.shiftLeft(8 * (nSize - 3));
        }
        return nWord;
    }

    public static Uint32 difficultyCompress(BigInteger compact) {
        int len = (compact.bitLength() + 7) / 8;
        if (compact.testBit(len * 8 - 1)) { // 负数
            len += 1;
        }
        long nCompact = len << 24;
        if (len <= 3) {
            nCompact = compact.shiftLeft(8 * (3 - len)).intValueExact();
        } else {
            int pay = compact.shiftRight(8 * (len - 3)).intValueExact();
            Assert.isTrue(pay > 0 && pay <= 0x007FFFFF, "Difficulty Compress overflow");
            nCompact |= pay;
        }
        return new Uint32(nCompact);
    }
}
