package com.github.microwww.bitcoin.rpc;

import com.github.microwww.bitcoin.chain.ChainBlock;
import com.github.microwww.bitcoin.chain.PowDifficulty;
import com.github.microwww.bitcoin.chain.RawTransaction;
import com.github.microwww.bitcoin.math.Uint256;
import com.github.microwww.bitcoin.model.Block;
import com.github.microwww.bitcoin.store.DiskBlock;
import com.github.microwww.bitcoin.util.ByteUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Optional;

/**
 * clearmempool
 * getbestblockhash
 * getblockchaininfo
 * getblockcount
 * getblockhash height
 * getblockheader "blockhash" ( verbose )
 * getblockstats hash_or_height ( stats )
 * getchaintips
 * getchaintxstats ( nblocks "blockhash" )
 * getdifficulty
 * getmempoolancestors "txid" ( verbose )
 * getmempooldescendants "txid" ( verbose )
 * getmempoolentry "txid"
 * getmempoolinfo
 * getrawmempool ( verbose )
 * gettxout "txid" n ( include_mempool )
 * gettxoutproof ["txid",...] ( "blockhash" )
 * gettxoutsetinfo
 * preciousblock "blockhash"
 * pruneblockchain height
 * savemempool
 * scantxoutset "actiontoexecute" [scanobjects,...]
 * verifychain ( checklevel nblocks )
 * verifytxoutproof "proof
 */
@RestController
public class BlockchainController {

    public BlockchainController(DiskBlock diskBlock) {
        this.diskBlock = diskBlock;
    }

    @Autowired
    DiskBlock diskBlock;

    /**
     * getblock "blockhash" ( verbosity )
     *
     * @param blockhash
     * @param verbosity
     * @return
     */
    // TODO:: not set !
    @GetMapping("/api/getblock")
    public Mono<Block> getBlock(@RequestParam String blockhash, @RequestParam(defaultValue = "1") int verbosity) {
        Uint256 hash = new Uint256(ByteUtil.hexReverse(blockhash));
        Optional<ChainBlock> ch = diskBlock.getChinBlock(hash);
        if (ch.isPresent()) {
            ChainBlock block = ch.get();
            Block bk = new Block();
            bk.setBits(block.header.getBits().toHex());
            bk.setHeight(block.getHeight());
            bk.setChainwork(null);
            int height = diskBlock.getLastBlock().getHeight();
            bk.setConfirmations(height - block.getHeight());
            double dif = BigDecimal.valueOf(block.header.getNonce().longValue())
                    .divide(
                            BigDecimal.valueOf(PowDifficulty.bnProofOfWorkLimit.longValue()),
                            9,
                            RoundingMode.HALF_UP
                    ).doubleValue();
            bk.setDifficulty(dif);
            bk.setHash(block.hash().toHexReverse256());
            // bk.setMediantime();
            bk.setMerkleroot(block.header.getMerkleRoot().toHexReverse256());
            // bk.setNextblockhash(null);
            bk.setNonce(block.header.getNonce().longValue());
            bk.setnTx(block.getTxs().length);
            bk.setPreviousblockhash(null);
            bk.setSize(block.serialization().length);
            //bk.setStrippedsize(?);
            bk.setTime(block.header.getTime().longValue());
            String[] txs = Arrays.stream(block.getTxs()).map(RawTransaction::hash).map(Uint256::toHexReverse256).toArray(String[]::new);
            bk.setTx(txs);
            bk.setVersion(block.header.getVersion());
            bk.setVersionHex(String.format("%08x", block.header.getVersion()));
            return Mono.just(bk);
        }
        return Mono.empty();
    }
}
