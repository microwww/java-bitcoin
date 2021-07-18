package com.github.microwww.bitcoin.math;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

class MerkleTreeTest {

    @Test
    void merkleTree() {
        List<Int256> list = Arrays.asList(new Int256("1", 10), new Int256("2", 10), new Int256("3", 10));
        MerkleTree tree = MerkleTree.merkleTree(list, Int256::sha256sha256, (v1, v2) -> v1.sha256sha256(v2));
        System.out.println(tree);
    }
}