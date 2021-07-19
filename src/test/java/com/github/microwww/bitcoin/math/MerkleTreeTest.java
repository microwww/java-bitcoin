package com.github.microwww.bitcoin.math;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.HexUtil;
import com.github.microwww.bitcoin.util.ClassPath;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

class MerkleTreeTest {

    @Test
    void merkleTree100202() {
        // BTC区块链 https://btc.tokenview.com/cn/block/90011 , 高度 90011 !
        String[] trs = new String[]{
                "3b66cc8695e6fa76e49ef494cd67f57591c36e0fefe9bdbe2685f6d102b8aea2",
        };
        List<byte[]> list = Arrays.stream(trs).map(e -> HexUtil.decodeHex(e)).map(e1 -> ArrayUtil.reverse(e1)).collect(Collectors.toList());
        MerkleTree<byte[], byte[]> tree = MerkleTree.merkleTree(list, e -> e, (e1, e2) -> Hash.sha256sha256(ArrayUtil.addAll(e1, e2)));
        String hex = Hash.hexReverse(tree.getHash());
        Assertions.assertEquals("3b66cc8695e6fa76e49ef494cd67f57591c36e0fefe9bdbe2685f6d102b8aea2", hex);
    }

    @Test
    void merkleTree90011() {
        // BTC区块链 https://btc.tokenview.com/cn/block/90011 , 高度 90011 !
        String[] trs = new String[]{
                "b52325c901c945ecf8fabf2cbb11096fdd33d561c292d077e55d2a7097a146c9",
                "61bbe14856df097238863adf6b2510b5734767b0a2cc653d062cf86e3e8d13dd",
        };
        List<byte[]> list = Arrays.stream(trs).map(e -> HexUtil.decodeHex(e)).map(e1 -> ArrayUtil.reverse(e1)).collect(Collectors.toList());
        MerkleTree<byte[], byte[]> tree = MerkleTree.merkleTree(list, e -> e, (e1, e2) -> Hash.sha256sha256(ArrayUtil.addAll(e1, e2)));
        String hex = Hash.hexReverse(tree.getHash());
        Assertions.assertEquals("32ac987734f5f4ac3afc589496c5a86da74dff6e3187f64d1c6c8d5a58cd6c03", hex);
    }

    @Test
    void merkleTree90017() {
        // BTC区块链 https://btc.tokenview.com/cn/block/90017 , 高度 90017 !
        String[] trs = new String[]{
                "393e4ac19b91725ad53f96cd7a2a9a6e9f64a682e70133d6785d201e1031ab54",
                "8969d80028914caf03440647b1413f4e17aeffdd1c5106b3423f0931c7c56dbe",
                "d417bf909bc5b5c82ac38ca2cf2f3e7fd0be1237ad4aea047902569129b66e00",
        };
        List<byte[]> list = Arrays.stream(trs).map(e -> HexUtil.decodeHex(e)).map(e1 -> ArrayUtil.reverse(e1)).collect(Collectors.toList());
        //MerkleTree<byte[], byte[]> tree = MerkleTree.merkleTree(list, e -> e, (e1, e2) -> Hash.sha256sha256(ArrayUtil.addAll(e1, e2)));
        MerkleTree<byte[], Int256> tree = MerkleTree.merkleTree(list, e -> new Int256(e), (e1, e2) -> new Int256(e1.sha256sha256(e2)));
        System.out.println(tree);
        String hex = Hash.hex(tree.getHash().reverse256bit());
        Assertions.assertEquals("3ad8d9a3e530d6591e97c7c1e55ee6d0cecc4d9f798b0637135bbce7aea8c22c", hex);

        trs = new String[]{
                "393e4ac19b91725ad53f96cd7a2a9a6e9f64a682e70133d6785d201e1031ab54",
                "8969d80028914caf03440647b1413f4e17aeffdd1c5106b3423f0931c7c56dbe",
                "d417bf909bc5b5c82ac38ca2cf2f3e7fd0be1237ad4aea047902569129b66e00",
                "d417bf909bc5b5c82ac38ca2cf2f3e7fd0be1237ad4aea047902569129b66e00"
        };
        list = Arrays.stream(trs).map(e -> HexUtil.decodeHex(e)).map(e1 -> ArrayUtil.reverse(e1)).collect(Collectors.toList());
        tree = MerkleTree.merkleTree(list, e -> new Int256(e), (e1, e2) -> new Int256(e1.sha256sha256(e2)));
        hex = Hash.hex(tree.getHash().reverse256bit());
        Assertions.assertEquals("3ad8d9a3e530d6591e97c7c1e55ee6d0cecc4d9f798b0637135bbce7aea8c22c", hex);
    }

    @Test
    void merkleTree100200() {
        // BTC区块链 https://btc.tokenview.com/cn/block/90017 , 高度 90017 !
        String[] trs = new String[]{
                "c3f3507af534e86bb091dcefb7f9c70ae84431e95fcbd10d7d14f0e21a375068",
                "3f1cec836a22bac074f45ad2f8d86be47a5a1570bfa9e0898d7360e3b3ff2ca9",
                "8eca2f93acc4ab82d8677e0e0408c9cab846882bff74877c9cbc31353e78ef5a",
                "ac42aa985b99245bf7e8c426e83562f9b6766d2f5cfc2e0414a63a9baa11c613",
                "0b03a8ff9e2e8aeb91eb567995d109090806b285c102cc592214cbfdf8844fd6",
        };
        List<String> list = Arrays.stream(trs).map(e -> HexUtil.decodeHex(e)).map(e1 -> ArrayUtil.reverse(e1))
                .map(e -> HexUtil.encodeHexStr(e)).collect(Collectors.toList());
        //MerkleTree<byte[], byte[]> tree = MerkleTree.merkleTree(list, e -> e, (e1, e2) -> Hash.sha256sha256(ArrayUtil.addAll(e1, e2)));
        MerkleTree<byte[], Int256> tree = MerkleTree.merkleTree(list, e -> new Int256(e, 16), (e1, e2) -> new Int256(e1.sha256sha256(e2)));
        System.out.println(tree);
        String hex = Hash.hex(tree.getHash().reverse256bit());
        Assertions.assertEquals("72bb57d964febb27ec8b63f2c888d6f3bdbee657258f898cc8e04fdbef10a94e", hex);
    }

    @Test
    void merkleTreeHeight() {
        // 高度 150202 !
        int start = 8;
        List<String> strings = ClassPath.readClassPathFile("/data/line-data.txt");
        String ln = strings.get(start);
        List<String> list = Arrays.stream(ln.split("\"")).filter(e -> e.length() == 64)
                .map(e -> HexUtil.decodeHex(e)).map(e1 -> ArrayUtil.reverse(e1))
                .map(e -> HexUtil.encodeHexStr(e)).collect(Collectors.toList());
        MerkleTree<byte[], Int256> tree = MerkleTree.merkleTree(list, e -> new Int256(e, 16), (e1, e2) -> new Int256(e1.sha256sha256(e2)));
        System.out.println("data line " + list.size());
        System.out.println(tree);
        String hex = Hash.hex(tree.getHash().reverse256bit());
        String res = strings.get(start + 1);
        Assertions.assertEquals(res.trim(), hex);
    }

    @Test
    public void testDhash() {
        byte[] bytes = Hash.sha256sha256("hello" .getBytes(StandardCharsets.UTF_8));
        Assertions.assertEquals("9595c9df90075148eb06860365df33584b75bff782a510c6cd4883a419833d50", HexUtil.encodeHexStr(bytes));
    }
}