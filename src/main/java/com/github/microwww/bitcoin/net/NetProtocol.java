package com.github.microwww.bitcoin.net;

import com.github.microwww.bitcoin.net.protocol.*;
import com.github.microwww.bitcoin.provider.Peer;
import com.github.microwww.bitcoin.util.ByteUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum NetProtocol {

    /**
     * The version message provides information about the transmitting node to the
     * receiving node at the beginning of a connection.
     */
    VERSION() {
        @Override
        public Version parse(Peer peer, byte[] buf) {
            return new Version(peer).read(buf);
        }
    },

    /**
     * The verack message acknowledges a previously-received version message,
     * informing the connecting node that it can begin to send other messages.
     */
    VERACK() {
        @Override
        public VerACK parse(Peer peer, byte[] buf) {
            return new VerACK(peer).read(buf);
        }
    },
    /**
     * The addr (IP address) message relays connection information for peers on the
     * network.
     */
    ADDR {
        @Override
        public Addr parse(Peer peer, byte[] buf) throws IgnoreNetProtocolException {
            return new Addr(peer).read(buf);
        }
    },
    /**
     * The addrv2 message relays connection information for peers on the network just
     * like the addr message, but is extended to allow gossiping of longer node
     * addresses (see BIP155).
     */
    ADDRV2 {
        @Override
        public AbstractProtocol parse(Peer peer, byte[] buf) throws IgnoreNetProtocolException {
            return new AddrV2(peer).read(buf);
        }
    },
    /**
     * The sendaddrv2 message signals support for receiving ADDRV2 messages (BIP155).
     * It also implies that its sender can encode as ADDRV2 and would send ADDRV2
     * instead of ADDR to a peer that has signaled ADDRV2 support by sending SENDADDRV2.
     */
    SENDADDRV2() {
        @Override
        public SendAddrV2 parse(Peer peer, byte[] buf) {
            return new SendAddrV2(peer).read(buf);
        }
    },
    /**
     * The inv message (inventory message) transmits one or more inventories of
     * objects known to the transmitting peer.
     */
    INV() {
        @Override
        public Inv parse(Peer peer, byte[] buf) {
            return new Inv(peer).read(buf);
        }
    },
    /**
     * The getdata message requests one or more data objects from another node.
     */
    GETDATA {
        @Override
        public GetData parse(Peer peer, byte[] buf) throws IgnoreNetProtocolException {
            return new GetData(peer).read(buf);
        }
    },
    /**
     * The merkleblock message is a reply to a getdata message which requested a
     * block using the inventory type MSG_MERKLEBLOCK.
     *
     * @since protocol version 70001 as described by BIP37.
     */
    MERKLEBLOCK,
    /**
     * The getblocks message requests an inv message that provides block header
     * hashes starting from a particular point in the block chain.
     */
    GETBLOCKS,
    /**
     * The getheaders message requests a headers message that provides block
     * headers starting from a particular point in the block chain.
     *
     * @since protocol version 31800.
     */
    GETHEADERS() {
        @Override
        public GetHeaders parse(Peer peer, byte[] buf) {
            return new GetHeaders(peer).read(buf);
        }
    },
    /**
     * The tx message transmits a single transaction.
     */
    TX() {
        @Override
        public Tx parse(Peer peer, byte[] buf) {
            return new Tx(peer).read(buf);
        }
    },
    /**
     * The headers message sends one or more block headers to a node which
     * previously requested certain headers with a getheaders message.
     *
     * @since protocol version 31800.
     */
    HEADERS() {
        @Override
        public Headers parse(Peer peer, byte[] buf) {
            return new Headers(peer).read(buf);
        }
    },
    /**
     * The block message transmits a single serialized block.
     */
    BLOCK() {
        @Override
        public Block parse(Peer peer, byte[] buf) {
            return new Block(peer).read(buf);
        }
    },
    /**
     * The getaddr message requests an addr message from the receiving node,
     * preferably one with lots of IP addresses of other receiving nodes.
     */
    GETADDR {
        @Override
        public GetAddr parse(Peer peer, byte[] buf) throws IgnoreNetProtocolException {
            return new GetAddr(peer).read(buf);
        }
    },
    /**
     * The mempool message requests the TXIDs of transactions that the receiving
     * node has verified as valid but which have not yet appeared in a block.
     *
     * @since protocol version 60002.
     */
    MEMPOOL,
    /**
     * The ping message is sent periodically to help confirm that the receiving
     * peer is still connected.
     */
    PING() {
        @Override
        public Ping parse(Peer peer, byte[] buf) {
            return new Ping(peer).read(buf);
        }
    },
    /**
     * The pong message replies to a ping message, proving to the pinging node that
     * the ponging node is still alive.
     *
     * @since protocol version 60001 as described by BIP31.
     */
    PONG() {
        @Override
        public Pong parse(Peer peer, byte[] buf) {
            return new Pong(peer).read(buf);
        }
    },
    REJECT() {
        @Override
        public Reject parse(Peer peer, byte[] buf) {
            return new Reject(peer).read(buf);
        }
    },
    /**
     * The notfound message is a reply to a getdata message which requested an
     * object the receiving node does not have available for relay.
     *
     * @since protocol version 70001.
     */
    NOTFOUND,
    /**
     * The filterload message tells the receiving peer to filter all relayed
     * transactions and requested merkle blocks through the provided filter.
     *
     * @since protocol version 70001 as described by BIP37.
     * Only available with service bit NODE_BLOOM since protocol version
     * 70011 as described by BIP111.
     */
    FILTERLOAD,
    /**
     * The filteradd message tells the receiving peer to add a single element to a
     * previously-set bloom filter, such as a new public key.
     *
     * @since protocol version 70001 as described by BIP37.
     * Only available with service bit NODE_BLOOM since protocol version
     * 70011 as described by BIP111.
     */
    FILTERADD,
    /**
     * The filterclear message tells the receiving peer to remove a previously-set
     * bloom filter.
     *
     * @since protocol version 70001 as described by BIP37.
     * Only available with service bit NODE_BLOOM since protocol version
     * 70011 as described by BIP111.
     */
    FILTERCLEAR,
    /**
     * Indicates that a node prefers to receive new block announcements via a
     * "headers" message rather than an "inv".
     *
     * @since protocol version 70012 as described by BIP130.
     */
    SENDHEADERS() {
        @Override
        public SendHeaders parse(Peer peer, byte[] buf) {
            return new SendHeaders(peer).read(buf);
        }
    },
    /**
     * The feefilter message tells the receiving peer not to inv us any txs
     * which do not meet the specified min fee rate.
     *
     * @since protocol version 70013 as described by BIP133
     */
    FEEFILTER() {
        @Override
        public AbstractProtocol parse(Peer peer, byte[] buf) {
            return new FeeFilter(peer).read(buf);
        }
    },
    /**
     * Contains a 1-byte bool and 8-byte LE version number.
     * Indicates that a node is willing to provide blocks via "cmpctblock" messages.
     * May indicate that a node prefers to receive new block announcements via a
     * "cmpctblock" message rather than an "inv", depending on message contents.
     *
     * @since protocol version 70014 as described by BIP 152
     */
    SENDCMPCT() {
        @Override
        public SendCmpct parse(Peer peer, byte[] buf) {
            return new SendCmpct(peer).read(buf);
        }
    },
    /**
     * Contains a CBlockHeaderAndShortTxIDs object - providing a header and
     * list of "short txids".
     *
     * @since protocol version 70014 as described by BIP 152
     */
    CMPCTBLOCK,
    /**
     * Contains a BlockTransactionsRequest
     * Peer should respond with "blocktxn" message.
     *
     * @since protocol version 70014 as described by BIP 152
     */
    GETBLOCKTXN,
    /**
     * Contains a BlockTransactions.
     * Sent in response to a "getblocktxn" message.
     *
     * @since protocol version 70014 as described by BIP 152
     */
    BLOCKTXN,
    /**
     * getcfilters requests compact filters for a range of blocks.
     * Only available with service bit NODE_COMPACT_FILTERS as described by
     * BIP 157 & 158.
     */
    GETCFILTERS,
    /**
     * cfilter is a response to a getcfilters request containing a single compact
     * filter.
     */
    CFILTER,
    /**
     * getcfheaders requests a compact filter header and the filter hashes for a
     * range of blocks, which can then be used to reconstruct the filter headers
     * for those blocks.
     * Only available with service bit NODE_COMPACT_FILTERS as described by
     * BIP 157 & 158.
     */
    GETCFHEADERS,
    /**
     * cfheaders is a response to a getcfheaders request containing a filter header
     * and a vector of filter hashes for each subsequent block in the requested range.
     */
    CFHEADERS,
    /**
     * getcfcheckpt requests evenly spaced compact filter headers, enabling
     * parallelized download and validation of the headers between them.
     * Only available with service bit NODE_COMPACT_FILTERS as described by
     * BIP 157 & 158.
     */
    GETCFCHECKPT,
    /**
     * cfcheckpt is a response to a getcfcheckpt request containing a vector of
     * evenly spaced filter headers for blocks on the requested chain.
     */
    CFCHECKPT,
    /**
     * Indicates that a node prefers to relay transactions via wtxid, rather than
     * txid.
     *
     * @since protocol version 70016 as described by BIP 339.
     */
    WTXIDRELAY() {
        @Override
        public WtxidRelay parse(Peer peer, byte[] buf) {
            return new WtxidRelay(peer).read(buf);
        }
    };

    private static final Logger logger = LoggerFactory.getLogger(NetProtocol.class);
    private final String command = this.name().toLowerCase();

    public AbstractProtocol parse(Peer peer, byte[] buf) throws IgnoreNetProtocolException {
        logger.warn("Net protocol parse is not support : {}", this.command);
        throw new IgnoreNetProtocolException(this.command);
    }

    public String command() {
        return command;
    }

    public static NetProtocol select(String cmd) throws UnsupportedNetProtocolException {
        try {
            return NetProtocol.valueOf(cmd.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new UnsupportedNetProtocolException(cmd);
        }
    }

    public byte[] toByte(int size) {
        byte[] bt = new byte[size];
        byte[] ch = ByteUtil.UTF8(this.command());
        System.arraycopy(ch, 0, bt, 0, ch.length);
        return bt;
    }

    public static NetProtocol toMessageType(byte[] bytes) throws IllegalArgumentException {
        return NetProtocol.valueOf(NetProtocol.toType(bytes));
    }

    public static String toType(byte[] bytes) {
        String command = new String(bytes);
        for (int i = 0; i < bytes.length; i++) {
            if (bytes[i] == 0) {
                command = new String(bytes, 0, i);
                break;
            }
        }
        return command;
    }
}
