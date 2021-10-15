package com.github.microwww.bitcoin.rpc.web;

import com.github.microwww.bitcoin.conf.CChainParams;
import com.github.microwww.bitcoin.model.AddressInfo;
import com.github.microwww.bitcoin.wallet.AccountDB;
import com.github.microwww.bitcoin.wallet.Wallet;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
public class WalletController {
    Wallet wallet;
    CChainParams chainParams;

    public WalletController(Wallet wallet, CChainParams chainParams) {
        this.wallet = wallet;
        this.chainParams = chainParams;
    }

    @GetMapping("/wallet/address")
    public Flux<AddressInfo> wallet() {
        List<AccountDB> dbs = wallet.listAddress();
        return Flux.fromStream(dbs.stream().map(WalletController::mapper));
    }

    @PostMapping("/wallet/address")
    public Mono<AddressInfo> wallet(@RequestParam(defaultValue = "") String tag) {
        AccountDB gen = wallet.gen(tag);
        return Mono.just(mapper(gen));
    }

    public static AddressInfo mapper(AccountDB db) {
        AddressInfo addr = new AddressInfo();
        addr.setAddress(db.toAddress());
        addr.setIswitness(db.isWitness());
        addr.setLabel(db.getTag());
        addr.setTimestamp((int) (db.getCreateTime().getTime() / 1000));
        return addr;
    }
}
