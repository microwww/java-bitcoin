package com.github.microwww.bitcoin.script;

import com.github.microwww.bitcoin.math.Uint8;
import org.junit.jupiter.api.Test;

import java.util.Stack;

import static org.junit.jupiter.api.Assertions.*;

class InterpreterTest {
    BytesStack stack = new BytesStack();

    @Test
    public void testV() {
        stack.push(20);
        stack.push(52);
        ScriptNames.OP_ADD.opt(stack);
        System.out.println(stack.peek());
    }
}