package com.github.microwww.bitcoin.script.ins;

import com.github.microwww.bitcoin.script.Instruction;
import com.github.microwww.bitcoin.script.Interpreter;
import com.github.microwww.bitcoin.script.ScriptOperation;
import com.github.microwww.bitcoin.script.ex.ScriptDisableException;
import com.github.microwww.bitcoin.util.ByteUtil;
import io.netty.buffer.ByteBuf;

import java.math.BigInteger;

public enum Instruction_8B_A5 implements Instruction {

    // numeric
    OP_1ADD { // 139

        @Override
        public void exec(Interpreter executor, Object data) {
            int v = executor.stack.assertNotEmpty().popInt() + 1;
            executor.stack.push(v);
        }
    },
    OP_1SUB { // 140

        @Override
        public void exec(Interpreter executor, Object data) {
            int v = executor.stack.assertNotEmpty().popInt() - 1;
            executor.stack.push(v);
        }
    },
    OP_2MUL {
        @Override
        public void exec(Interpreter executor, Object data) {
            throw new ScriptDisableException(this.name());
        }
    },
    OP_2DIV {
        @Override
        public void exec(Interpreter executor, Object data) {
            throw new ScriptDisableException(this.name());
        }
    },
    OP_NEGATE {
        @Override
        public void exec(Interpreter executor, Object data) {
            int i = executor.stack.assertNotEmpty().popInt();
            if (i > 0) {
                executor.stack.push(-i);
            } else {
                executor.stack.push(i);
            }
        }
    },
    OP_ABS {
        @Override
        public void exec(Interpreter executor, Object data) {
            int i = executor.stack.assertNotEmpty().popInt();
            if (i >= 0) {
                executor.stack.push(i);
            } else {
                executor.stack.push(-i);
            }
        }
    },
    OP_NOT { // 145

        @Override
        public void exec(Interpreter executor, Object data) {
            int i = executor.stack.assertNotEmpty().popInt();
            if (i == 0) {
                executor.stack.push(1);
            } else {
                executor.stack.push(0);
            }
        }
    },
    OP_0NOTEQUAL {
        @Override
        public void exec(Interpreter executor, Object data) {
            int i = executor.stack.assertNotEmpty().popInt();
            if (i != 0) {
                executor.stack.push(1);
            } else {
                executor.stack.push(0);
            }
        }
    },
    OP_ADD {
        @Override
        public void exec(Interpreter executor, Object data) {
            int a = executor.stack.assertSizeGE(2).popInt();
            int b = executor.stack.popInt();
            executor.stack.push(a + b);
        }
    },
    OP_SUB {
        @Override
        public void exec(Interpreter executor, Object data) {
            int a = executor.stack.assertSizeGE(2).popInt();
            int b = executor.stack.popInt();
            executor.stack.push(a - b);
        }
    },
    OP_MUL {
        @Override
        public void exec(Interpreter executor, Object data) {
            throw new ScriptDisableException(this.name());
        }
    },
    OP_DIV { // 150

        @Override
        public void exec(Interpreter executor, Object data) {
            throw new ScriptDisableException(this.name());
        }
    },
    OP_MOD {
        @Override
        public void exec(Interpreter executor, Object data) {
            throw new ScriptDisableException(this.name());
        }
    },
    OP_LSHIFT {
        @Override
        public void exec(Interpreter executor, Object data) {
            throw new ScriptDisableException(this.name());
        }
    },
    OP_RSHIFT {
        @Override
        public void exec(Interpreter executor, Object data) {
            throw new ScriptDisableException(this.name());
        }
    },
    OP_BOOLAND {
        @Override
        public void exec(Interpreter executor, Object data) {
            int a = executor.stack.assertSizeGE(2).popInt();
            int b = executor.stack.popInt();
            if (a != 0 && b != 0) {
                executor.stack.push(1);
            } else {
                executor.stack.push(0);
            }
        }
    },
    OP_BOOLOR { // 155

        @Override
        public void exec(Interpreter executor, Object data) {
            int a = executor.stack.assertSizeGE(2).popInt();
            int b = executor.stack.popInt();
            if (a != 0 || b != 0) {
                executor.stack.push(1);
            } else {
                executor.stack.push(0);
            }
        }
    },
    OP_NUMEQUAL {
        @Override
        public void exec(Interpreter executor, Object data) {
            int a = executor.stack.assertSizeGE(2).popInt();
            int b = executor.stack.popInt();
            if (a == b) {
                executor.stack.push(1);
            } else {
                executor.stack.push(0);
            }
        }
    },
    OP_NUMEQUALVERIFY,
    OP_NUMNOTEQUAL,
    OP_LESSTHAN,
    OP_GREATERTHAN, // 160
    OP_LESSTHANOREQUAL,
    OP_GREATERTHANOREQUAL,
    OP_MIN {
        @Override
        public void exec(Interpreter executor, Object data) {
            // 3ee060fb1856f111859fb108d079635a2d225ef68d5ae5250ce70d39ac2a2dc4
            byte[] p1 = executor.stack.assertSizeGE(2).pop();
            byte[] p2 = executor.stack.pop();
            BigInteger min = new BigInteger(ByteUtil.reverse(p1)).min(new BigInteger(ByteUtil.reverse(p2)));
            executor.stack.push(ByteUtil.reverse(min.toByteArray()));
        }
    },
    OP_MAX {
        @Override
        public void exec(Interpreter executor, Object data) {
            byte[] p1 = executor.stack.assertSizeGE(2).pop();
            byte[] p2 = executor.stack.pop();
            BigInteger min = new BigInteger(ByteUtil.reverse(p1)).max(new BigInteger(ByteUtil.reverse(p2)));
            executor.stack.push(ByteUtil.reverse(min.toByteArray()));
        }
    },
    OP_WITHIN {
        @Override
        public void exec(Interpreter executor, Object data) {
            byte[] max = executor.stack.assertSizeGE(3).pop();
            BigInteger min = new BigInteger(ByteUtil.reverse(executor.stack.pop()));
            BigInteger x = new BigInteger(ByteUtil.reverse(executor.stack.pop()));
            if (new BigInteger(ByteUtil.reverse(max)).compareTo(x) > 0) {
                if (min.compareTo(x) <= 0) {
                    executor.stack.push(1);
                    return;
                }
            }
            executor.stack.push(0);
        }
    }, // 165
    ;

    @Override
    public ScriptOperation compile(ByteBuf bf) {
        return new ScriptOperation(this, ZERO);
    }

    @Override
    public void exec(Interpreter executor, Object data) {
        throw new UnsupportedOperationException(this.toString());
    }

    public byte opcode() {
        return (byte) (0x8B + this.ordinal());
    }

    @Override
    public String toString() {
        byte c = this.opcode();
        return this.name() + "|" + c + "|0x" + ByteUtil.hex(new byte[]{c});
    }
}
