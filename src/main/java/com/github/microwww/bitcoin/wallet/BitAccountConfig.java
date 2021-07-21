package com.github.microwww.bitcoin.wallet;

public enum BitAccountConfig {
    MAIN() {
        public byte getAddressHeader() {
            return 0;
        }

        public byte getDumpedPrivateKeyHeader() {
            return (byte) 128;
        }

        @Override
        public String cashPrefix() {
            return "bitcoincash";
        }
    },
    TEST() {
        public byte getAddressHeader() {
            return 111;
        }

        public byte getDumpedPrivateKeyHeader() {
            return (byte) 239;
        }

        public String cashPrefix() {
            return "bchtest";
        }
    }, REG_TEST() {
        public byte getAddressHeader() {
            return 111;
        }

        public byte getDumpedPrivateKeyHeader() {
            return (byte) 239;
        }

        public String cashPrefix() {
            return "bchreg";
        }
    };

    public abstract byte getAddressHeader();

    public abstract byte getDumpedPrivateKeyHeader();

    public abstract String cashPrefix();

    public byte cashAddressHeader() {
        return 0;
    }
}
