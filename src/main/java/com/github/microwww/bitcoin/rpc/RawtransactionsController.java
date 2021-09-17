package com.github.microwww.bitcoin.rpc;

public class RawtransactionsController {
    /**
     * analyzepsbt "psbt"
     * combinepsbt ["psbt",...]
     * combinerawtransaction ["hexstring",...]
     * converttopsbt "hexstring" ( permitsigdata iswitness )
     * createpsbt [{"txid":"hex","vout":n,"sequence":n},...] [{"address":amount},{"data":"hex"},...] ( locktime replaceable )
     * createrawtransaction [{"txid":"hex","vout":n,"sequence":n},...] [{"address":amount},{"data":"hex"},...] ( locktime replaceable )
     * decodepsbt "psbt"
     * decoderawtransaction "hexstring" ( iswitness )
     * decodescript "hexstring"
     * finalizepsbt "psbt" ( extract )
     * fundrawtransaction "hexstring" ( options iswitness )
     * getrawtransaction "txid" ( verbose "blockhash" )
     * joinpsbts ["psbt",...]
     * sendrawtransaction "hexstring" ( allowhighfees )
     * signrawtransaction "hexstring" ( [{"txid":"hex","vout":n,"scriptPubKey":"hex","redeemScript":"hex","amount":amount},...] ) ["privatekey",...] ( "sighashtype" )
     * signrawtransactionwithkey "hexstring" ["privatekey",...] ( [{"txid":"hex","vout":n,"scriptPubKey":"hex","redeemScript":"hex","witnessScript":"hex","amount":amount},...] "sighashtype" )
     * testmempoolaccept ["rawtx",...] ( allowhighfees )
     * utxoupdatepsbt "psbt"
     */
}
