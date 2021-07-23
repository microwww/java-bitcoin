package com.github.microwww.bitcoin.net.protocol;

import com.github.microwww.bitcoin.math.Uint32;

import java.util.Optional;

/**
 * Invs always use TX/WTX or BLOCK.
 * FILTERED_BLOCK / CMPCT_BLOCK / WITNESS_BLOCK / WITNESS_TX can only occur in getdata.
 */
public enum GetDataType { // C++ `enum GetDataMsg`
    UNDEFINED(0, Inv.class),
    TX(1, Inv.class),
    BLOCK(2, Inv.class),
    WTX(5, Inv.class),                                          //!< Defined in BIP 339
    // The following can only occur in getdata. Invs always use TX/WTX or BLOCK.
    FILTERED_BLOCK(3, GetData.class),                           //!< Defined in BIP37
    CMPCT_BLOCK(4, GetData.class),                              //!< Defined in BIP152
    WITNESS_BLOCK(BLOCK.type.longValue() | E.MSG_WITNESS_FLAG, GetData.class), //!< Defined in BIP144 , 0x40 00 00 02
    WITNESS_TX(TX.type.longValue() | E.MSG_WITNESS_FLAG, GetData.class),       //!< Defined in BIP144 , 0x40 00 00 01
    ;
    // MSG_FILTERED_WITNESS_BLOCK is defined in BIP144 as reserved for future
    // use and remains unused.
    // MSG_FILTERED_WITNESS_BLOCK = MSG_FILTERED_BLOCK | MSG_WITNESS_FLAG,
    private final Uint32 type;
    private final Class<?> support;

    GetDataType(long type, Class<?> support) {
        this.type = new Uint32(type);
        this.support = support;
    }

    public static Optional<GetDataType> select(Uint32 type) {
        for (GetDataType value : GetDataType.values()) {
            if (value.type.equals(type)) {
                return Optional.of(value);
            }
        }
        return Optional.empty();
    }

    public void validity(Object req) throws UnsupportedNetProtocolException {
        if (req != null && req.getClass().equals(this.support)) {
            return;
        }
        throw new UnsupportedNetProtocolException("Un supported request Type : " + req.getClass().getSimpleName());
    }

    private class E {
        public static final long MSG_WITNESS_FLAG = 1 << 30; // 0x 40 00 00 00
        public static final long MSG_TYPE_MASK = 0xffffffff >> 2;
    }

    public Uint32 getType() {
        return type;
    }
}
