package com.github.microwww.bitcoin.math;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

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
public class MerkleTree<U, T> {
    private T hash;
    private MerkleTree<U, T> left;
    private MerkleTree<U, T> right;
    private U data;

    public static <T, U> MerkleTree merkleTree(List<U> list, Function<U, T> mapper, BiFunction<T, T, T> reducer) {
        if (list.isEmpty()) {
            throw new IllegalArgumentException("NOT EMPTY");
        }

        List<MerkleTree<U, T>> trees = list.stream().map(u -> {
            MerkleTree<U, T> leaf = new MerkleTree();
            leaf.hash = mapper.apply(u);
            leaf.data = u;
            return leaf;
        }).collect(Collectors.toList());
        while (trees.size() > 1) {
            ArrayList<MerkleTree<U, T>> tr = new ArrayList<>();
            for (int i = 0; i < trees.size(); i += 2) {
                int next = i + 1;
                if (next >= trees.size()) next = i;
                MerkleTree<U, T> node = new MerkleTree();
                node.left = trees.get(i);
                node.right = trees.get(next);
                node.hash = reducer.apply(node.left.hash, node.right.hash);
                tr.add(node);
            }
            trees = tr;
        }
        return trees.get(0);
    }

    public static <T, U> MerkleTree merkleTree1(List<U> list, Function<U, T> mapper, BiFunction<T, T, T> reducer) {
        if (list.isEmpty()) {
            throw new IllegalArgumentException("NOT EMPTY");
        }
        if (list.size() == 1) {
            MerkleTree merkleTree = new MerkleTree();
            U leaf = list.get(0);
            merkleTree.hash = mapper.apply(leaf);
            merkleTree.data = leaf;
            return merkleTree;
        }
        int left = (list.size() + 1) / 2;
        MerkleTree<U, T> tree = new MerkleTree();

        List<U> L = list.subList(0, left);
        tree.left = merkleTree1(L, mapper, reducer);

        List<U> r = list.subList(left, list.size());
        tree.right = merkleTree1(r, u -> {
            T apply = mapper.apply(u);
            if (r.size() < L.size()) {
                return reducer.apply(apply, apply);
            }
            return apply;
        }, reducer);

        tree.hash = reducer.apply(tree.left.hash, tree.right.hash);
        return tree;
    }

    public T getHash() {
        return hash;
    }

    public void setHash(T hash) {
        this.hash = hash;
    }

    public MerkleTree<U, T> getLeft() {
        return left;
    }

    public void setLeft(MerkleTree<U, T> left) {
        this.left = left;
    }

    public MerkleTree<U, T> getRight() {
        return right;
    }

    public void setRight(MerkleTree<U, T> right) {
        this.right = right;
    }

    public U getData() {
        return data;
    }

    public void setData(U data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return this.toString("").toString();
    }

    public StringBuilder toString(final String prefix) {
        StringBuilder join = new StringBuilder();
        join.append("{ MerkleTree").append("\n");
        join.append(prefix).append("    hash  = ").append(hash).append(", data = ").append(data).append("\n")
                .append(prefix).append("    left  = ");
        if (left != null) {
            join.append(left.toString(prefix + "    "));
        }
        join.append("\n")
                .append(prefix).append("    right = ");
        if (right != null) {
            join.append(right.toString(prefix + "    ")).append("\n");
        } else join.append("\n");

        return join.append(prefix).append("}");
    }
}
