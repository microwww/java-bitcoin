package com.github.microwww.bitcoin.net.protocol;

import com.github.microwww.bitcoin.math.Uint32;

import java.util.Optional;

/**
 * Invs always use TX/WTX or BLOCK.
 * FILTERED_BLOCK / CMPCT_BLOCK / WITNESS_BLOCK / WITNESS_TX can only occur in getdata.
 */
public enum GetDataType { // C++ `enum GetDataMsg`
    UNDEFINED(0),
    TX(1),
    BLOCK(2),
    FILTERED_BLOCK(3),                           //!< Defined in BIP37
    CMPCT_BLOCK(4),                              //!< Defined in BIP152
    WTX(5),                                      //!< Defined in BIP 339
    WITNESS_BLOCK(BLOCK.type.longValue() | E.MSG_WITNESS_FLAG), //!< Defined in BIP144 , 0x40 00 00 02
    WITNESS_TX(TX.type.longValue() | E.MSG_WITNESS_FLAG),       //!< Defined in BIP144 , 0x40 00 00 01
    ;
    // MSG_FILTERED_WITNESS_BLOCK is defined in BIP144 as reserved for future
    // use and remains unused.
    // MSG_FILTERED_WITNESS_BLOCK = MSG_FILTERED_BLOCK | MSG_WITNESS_FLAG,
    private final Uint32 type;

    GetDataType(long type) {
        this.type = new Uint32(type);
    }

    public static Optional<GetDataType> select(Uint32 type) {
        for (GetDataType value : GetDataType.values()) {
            if (value.type.equals(type)) {
                return Optional.of(value);
            }
        }
        return Optional.empty();
    }

    private class E {
        public static final long MSG_WITNESS_FLAG = 1 << 30; // 0x 40 00 00 00
        public static final long MSG_TYPE_MASK = 0xffffffff >> 2;
    }

    public Uint32 getType() {
        return type;
    }
}
