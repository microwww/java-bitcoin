package com.github.microwww.bitcoin.store;

/**
 * txdb.cpp
 */
public enum LevelDBPrefix {
    DB_COIN('C'),                // 0X43 / 67
    DB_COINS('c'),               // 0X63 / 99
    DB_BLOCK_FILES('f'),         // 0X66 / 102
    DB_BLOCK_INDEX('b'),         // 0X62 / 98
    DB_BEST_BLOCK('B'),          // 0X42 / 66  | indexes/txindex , regtest\chainstate
    DB_HEAD_BLOCKS('H'),         // 0X48 / 72
    DB_FLAG('F'),                // 0X46 / 70
    DB_REINDEX_FLAG('R'),        // 0X52 / 82  | blocks/index
    DB_LAST_BLOCK('l'),          // 0X6c / 108 | blocks/index
    ;
    public final char prefix;
    public final byte prefixByte;
    public final byte[] prefixBytes;

    LevelDBPrefix(char prefix) {
        this.prefix = prefix;
        this.prefixByte = (byte) prefix;
        this.prefixBytes = new byte[]{prefixByte};
    }
}
