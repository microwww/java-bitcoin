package com.github.microwww.bitcoin.math;

import java.math.BigInteger;
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

    /**
     * ????????????, ?????????????????????????????? ??????hash???????????????
     * ??????: ???????????????????????????!!??????!!???????????????????????????????????????????????? ?????????????????????, ?????? List??????????????????
     *
     * @param <U>     ??????????????????
     * @param <T>     hash ??????????????????
     * @param list    ????????????
     * @param mapper
     * @param reducer
     * @return
     */
    public static <U, T> MerkleTree merkleTree(List<U> list, Function<U, T> mapper, BiFunction<T, T, T> reducer) {
        if (list.isEmpty()) {
            throw new IllegalArgumentException("NOT EMPTY");
        }
        /**
         * ????????????:
         * ???????????? ????????? MerkleTree ??????????????????(trees),
         * len ????????????, ??????????????????(len + 1) / 2)?????????,
         * ??????, ???????????????(next = i+1)??????(???len?????????), ?????????????????????(next = i)
         * ????????????????????????????????? node, ???????????? ??????(trees) ???????????????
         * ?????????????????????, ?????????????????? len == 1 ??????, ????????????
         *
         * ???????????? trees ??????, ????????? bitcoin ??????clone??????, ?????????????????????????????????.
         */
        List<MerkleTree<U, T>> trees = list.stream().map(u -> {
            T hash = mapper.apply(u);
            return new MerkleTree<U, T>().setHash(hash).setData(u);
        }).collect(Collectors.toList());
        int len = trees.size();
        while (len > 1) {
            for (int i = 0; i < len; i += 2) {
                int next = i + 1;
                if (next >= len) { // ????????????????????????
                    next = i;
                }
                MerkleTree<U, T> node = new MerkleTree();
                node.left = trees.get(i);
                node.right = trees.get(next);
                node.hash = reducer.apply(node.left.hash, node.right.hash);
                trees.set(i / 2, node);
            }
            len = (len + 1) / 2;
        }
        return trees.get(0);
    }

    public T getHash() {
        return hash;
    }

    public MerkleTree<U, T> setHash(T hash) {
        this.hash = hash;
        return this;
    }

    public MerkleTree<U, T> getLeft() {
        return left;
    }

    public MerkleTree<U, T> setLeft(MerkleTree<U, T> left) {
        this.left = left;
        return this;
    }

    public MerkleTree<U, T> getRight() {
        return right;
    }

    public MerkleTree<U, T> setRight(MerkleTree<U, T> right) {
        this.right = right;
        return this;
    }

    public U getData() {
        return data;
    }

    public MerkleTree<U, T> setData(U data) {
        this.data = data;
        return this;
    }

    public String stringData() {
        return toString(this.data);
    }

    public String stringHash() {
        return toString(this.hash);
    }

    public static String toString(Object data) {
        if (data == null) {
            return "N-U-L-L";
        }
        if (data instanceof byte[]) {
            return new BigInteger(1, (byte[]) data).toString(16);
        }
        return data.toString();
    }

    @Override
    public String toString() {
        return this.toString("").toString();
    }

    public StringBuilder toString(final String prefix) {
        StringBuilder join = new StringBuilder();
        join.append("{ MerkleTree#").append(System.identityHashCode(this)).append("\n");
        join.append(prefix).append("    hash  = ").append(stringHash()).append(", data = ").append(stringData()).append("\n")
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
