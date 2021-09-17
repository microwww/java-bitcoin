package com.github.microwww.bitcoin.rpc;

public class WalletController {
    /**
     * abandontransaction "txid"
     * abortrescan
     * addmultisigaddress nrequired ["key",...] ( "label" "address_type" )
     * backupwallet "destination"
     * bumpfee "txid" ( options )
     * createwallet "wallet_name" ( disable_private_keys blank )
     * dumpprivkey "address"
     * dumpwallet "filename"
     * encryptwallet "passphrase"
     * getaddressesbylabel "label"
     * getaddressinfo "address"
     * getbalance ( "dummy" minconf include_watchonly )
     * getnewaddress ( "label" "address_type" )
     * getrawchangeaddress ( "address_type" )
     * getreceivedbyaddress "address" ( minconf )
     * getreceivedbylabel "label" ( minconf )
     * gettransaction "txid" ( include_watchonly )
     * getunconfirmedbalance
     * getwalletinfo
     * importaddress "address" ( "label" rescan p2sh )
     * importmulti "requests" ( "options" )
     * importprivkey "privkey" ( "label" rescan )
     * importprunedfunds "rawtransaction" "txoutproof"
     * importpubkey "pubkey" ( "label" rescan )
     * importwallet "filename"
     * keypoolrefill ( newsize )
     * listaddressgroupings
     * listlabels ( "purpose" )
     * listlockunspent
     * listreceivedbyaddress ( minconf include_empty include_watchonly "address_filter" )
     * listreceivedbylabel ( minconf include_empty include_watchonly )
     * listsinceblock ( "blockhash" target_confirmations include_watchonly include_removed )
     * listtransactions ( "label" count skip include_watchonly )
     * listunspent ( minconf maxconf ["address",...] include_unsafe query_options )
     * listwalletdir
     * listwallets
     * loadwallet "filename"
     * lockunspent unlock ( [{"txid":"hex","vout":n},...] )
     * removeprunedfunds "txid"
     * rescanblockchain ( start_height stop_height )
     * sendmany "" {"address":amount} ( minconf "comment" ["address",...] replaceable conf_target "estimate_mode" )
     * sendtoaddress "address" amounttosend ( "comment" "comment_to" subtractfeefromamount replaceable conf_target "estimate_mode" )
     * sethdseed ( newkeypool "seed" )
     * setlabel "address" "label"
     * settxfee feeamount
     * signmessage "address" "message"
     * signrawtransactionwithwallet "hexstring" ( [{"txid":"hex","vout":n,"scriptPubKey":"hex","redeemScript":"hex","witnessScript":"hex","amount":amount},...] "sighashtype" )
     * unloadwallet ( "wallet_name" )
     * walletcreatefundedpsbt [{"txid":"hex","vout":n,"sequence":n},...] [{"address":amount},{"data":"hex"},...] ( locktime options bip32derivs )
     * walletlock
     * walletpassphrase "passphrase" timeout
     * walletpassphrasechange "oldpassphrase" "newpassphrase"
     * walletprocesspsbt "psbt" ( sign "sighashtype" bip32derivs )
     */
}
