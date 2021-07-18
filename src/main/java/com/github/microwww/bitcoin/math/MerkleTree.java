package com.github.microwww.bitcoin.math;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/*     WARNING! If you're reading this because you're learning about crypto
       and/or designing a new system that will use merkle trees, keep in mind
       that the following merkle tree algorithm has a serious flaw related to
       duplicate txids, resulting in a vulnerability (CVE-2012-2459).

       The reason is that if the number of hashes in the list at a given level
       is odd, the last one is duplicated before computing the next level (which
       is unusual in Merkle trees). This results in certain sequences of
       transactions leading to the same merkle root. For example, these two
       trees:

                    A               A
                  /  \            /   \
                B     C         B       C
               / \    |        / \     / \
              D   E   F       D   E   F   F
             / \ / \ / \     / \ / \ / \ / \
             1 2 3 4 5 6     1 2 3 4 5 6 5 6

       for transaction lists [1,2,3,4,5,6] and [1,2,3,4,5,6,5,6] (where 5 and
       6 are repeated) result in the same root hash A (because the hash of both
       of (F) and (F,F) is C).

       The vulnerability results from being able to send a block with such a
       transaction list, with the same merkle root, and the same block hash as
       the original without duplication, resulting in failed validation. If the
       receiving node proceeds to mark that block as permanently invalid
       however, it will fail to accept further unmodified (and thus potentially
       valid) versions of the same block. We defend against this by detecting
       the case where we would hash two identical hashes at the end of the list
       together, and treating that identically to the block having an invalid
       merkle root. Assuming no double-SHA256 collisions, this will detect all
       known ways of changing the transactions without affecting the merkle
       root.
*/
public class MerkleTree<T, U> {
    private T hash;
    private MerkleTree<T, U> left;
    private MerkleTree<T, U> right;
    private U leaf;

    public static <T, U> MerkleTree merkleTree(List<U> list, Function<U, T> hash0, BiFunction<T, T, T> hash) {
        if (list.isEmpty()) {
            throw new IllegalArgumentException("NOT EMPTY");
        }
        if (list.size() == 1) {
            MerkleTree merkleTree = new MerkleTree();
            U leaf = list.get(0);
            merkleTree.hash = hash0.apply(leaf);
            merkleTree.leaf = leaf;
            return merkleTree;
        }
        int left = list.size() / 2 * 2;
        if (left == 2) {
            left = 1;
        }
        MerkleTree<T, U> tree = merkleTree(list.subList(0, left), hash0, hash);
        if (left < list.size()) {
            MerkleTree tr = new MerkleTree();
            tr.left = tree;
            tr.right = merkleTree(list.subList(left, list.size()), hash0, hash);
            tree = tr;
        }
        tree.hash = hash.apply(tree.left.hash, tree.right.hash);
        return tree;
    }

    public T getHash() {
        return hash;
    }

    public void setHash(T hash) {
        this.hash = hash;
    }

    public MerkleTree<T, U> getLeft() {
        return left;
    }

    public void setLeft(MerkleTree<T, U> left) {
        this.left = left;
    }

    public MerkleTree<T, U> getRight() {
        return right;
    }

    public void setRight(MerkleTree<T, U> right) {
        this.right = right;
    }

    public U getLeaf() {
        return leaf;
    }

    public void setLeaf(U leaf) {
        this.leaf = leaf;
    }

    @Override
    public String toString() {
        return "MerkleTree{" +
                "hash=" + hash +
                ", left=" + left +
                ", right=" + right +
                ", leaf=" + leaf +
                '}';
    }
}
