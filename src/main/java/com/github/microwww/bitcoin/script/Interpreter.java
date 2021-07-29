package com.github.microwww.bitcoin.script;

import com.github.microwww.bitcoin.math.Uint8;

import java.util.Stack;

/**
 * `interpreter.cpp`
 * Script is a stack machine (like Forth) that evaluates a predicate
 * returning a bool indicating valid or not.  There are no loops.
 */
public class Interpreter {
    private Stack<Uint8> stack = new Stack<>();
}
