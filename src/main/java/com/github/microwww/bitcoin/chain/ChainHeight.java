package com.github.microwww.bitcoin.chain;

public class ChainHeight {
    private ChainBlock chainBlock;
    private int height;

    public ChainHeight() {
    }

    public ChainHeight(int height, ChainBlock chainBlock) {
        this.chainBlock = chainBlock;
        this.height = height;
    }

    public ChainBlock getChainBlock() {
        return chainBlock;
    }

    public ChainHeight setChainBlock(ChainBlock chainBlock) {
        this.chainBlock = chainBlock;
        return this;
    }

    public int getHeight() {
        return height;
    }

    public ChainHeight setHeight(int height) {
        this.height = height;
        return this;
    }
}