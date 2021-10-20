package com.github.microwww.bitcoin.store;

import com.github.microwww.bitcoin.chain.RawTransaction;
import com.github.microwww.bitcoin.math.Uint256;
import com.github.microwww.bitcoin.wallet.Wallet;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@Component
public class TxPool {
    private BlockCache<Uint256, RawTransaction> cache = new BlockCache<>(1000);
    private final Wallet wallet;

    public TxPool(Wallet wallet) {
        this.wallet = wallet;
    }

    public Optional<RawTransaction> get(Uint256 key, Supplier<Optional<RawTransaction>> supplier) {
        return cache.get(key, supplier);
    }

    public RawTransaction put(Uint256 key, RawTransaction value) {
        wallet.localTransaction(value);
        return cache.put(key, value);
    }

    public RawTransaction remove(Uint256 key) {
        return cache.remove(key);
    }

    public List<Uint256> get(int count) {
        return cache.get(count);
    }
}
