package com.github.microwww.bitcoin.script.instruction;

import com.github.microwww.bitcoin.script.Interpreter;
import com.github.microwww.bitcoin.script.ex.ScriptDisableException;
import com.github.microwww.bitcoin.util.ByteUtil;

import java.math.BigInteger;

// Instruction_8B_A5
public abstract class Numeric {

    // numeric
    static class OP_1ADD extends AbstractScriptNoOperand {
        public OP_1ADD(int code) {
            super(code);
        }

        @Override
        public void exec(Interpreter executor) {
            int v = executor.stack.assertNotEmpty().popInt() + 1;
            executor.stack.push(v);
        }
    }

    static class OP_1SUB extends AbstractScriptNoOperand {
        public OP_1SUB(int code) {
            super(code);
        }

        @Override
        public void exec(Interpreter executor) {
            int v = executor.stack.assertNotEmpty().popInt() - 1;
            executor.stack.push(v);
        }
    }

    static class OP_2MUL extends AbstractScriptNoOperand {
        public OP_2MUL(int code) {
            super(code);
        }

        @Override
        public void exec(Interpreter executor) {
            throw new ScriptDisableException(ScriptNames.values()[code].name());
        }
    }

    static class OP_2DIV extends AbstractScriptNoOperand {
        public OP_2DIV(int code) {
            super(code);
        }

        @Override
        public void exec(Interpreter executor) {
            throw new ScriptDisableException(ScriptNames.values()[code].name());
        }
    }

    static class OP_NEGATE extends AbstractScriptNoOperand {
        public OP_NEGATE(int code) {
            super(code);
        }

        @Override
        public void exec(Interpreter executor) {
            int i = executor.stack.assertNotEmpty().popInt();
            if (i > 0) {
                executor.stack.push(-i);
            } else {
                executor.stack.push(i);
            }
        }
    }

    static class OP_ABS extends AbstractScriptNoOperand {
        public OP_ABS(int code) {
            super(code);
        }

        @Override
        public void exec(Interpreter executor) {
            int i = executor.stack.assertNotEmpty().popInt();
            if (i >= 0) {
                executor.stack.push(i);
            } else {
                executor.stack.push(-i);
            }
        }
    }

    static class OP_NOT extends AbstractScriptNoOperand {
        public OP_NOT(int code) {
            super(code);
        } // 145

        @Override
        public void exec(Interpreter executor) {
            int i = executor.stack.assertNotEmpty().popInt();
            if (i == 0) {
                executor.stack.push(1);
            } else {
                executor.stack.push(0);
            }
        }
    }

    static class OP_0NOTEQUAL extends AbstractScriptNoOperand {
        public OP_0NOTEQUAL(int code) {
            super(code);
        }

        @Override
        public void exec(Interpreter executor) {
            int i = executor.stack.assertNotEmpty().popInt();
            if (i != 0) {
                executor.stack.push(1);
            } else {
                executor.stack.push(0);
            }
        }
    }

    static class OP_ADD extends AbstractScriptNoOperand {
        public OP_ADD(int code) {
            super(code);
        }

        @Override
        public void exec(Interpreter executor) {
            int a = executor.stack.assertSizeGE(2).popInt();
            int b = executor.stack.popInt();
            executor.stack.push(a + b);
        }
    }

    static class OP_SUB extends AbstractScriptNoOperand {
        public OP_SUB(int code) {
            super(code);
        }

        @Override
        public void exec(Interpreter executor) {
            int a = executor.stack.assertSizeGE(2).popInt();
            int b = executor.stack.popInt();
            executor.stack.push(a - b);
        }
    }

    static class OP_MUL extends AbstractScriptNoOperand {
        public OP_MUL(int code) {
            super(code);
        }

        @Override
        public void exec(Interpreter executor) {
            throw new ScriptDisableException(ScriptNames.values()[code].name());
        }
    }

    static class OP_DIV extends AbstractScriptNoOperand {
        public OP_DIV(int code) {
            super(code);
        } // 150

        @Override
        public void exec(Interpreter executor) {
            throw new ScriptDisableException(ScriptNames.values()[code].name());
        }
    }

    static class OP_MOD extends AbstractScriptNoOperand {
        public OP_MOD(int code) {
            super(code);
        }

        @Override
        public void exec(Interpreter executor) {
            throw new ScriptDisableException(ScriptNames.values()[code].name());
        }
    }

    static class OP_LSHIFT extends AbstractScriptNoOperand {
        public OP_LSHIFT(int code) {
            super(code);
        }

        @Override
        public void exec(Interpreter executor) {
            throw new ScriptDisableException(ScriptNames.values()[code].name());
        }
    }

    static class OP_RSHIFT extends AbstractScriptNoOperand {
        public OP_RSHIFT(int code) {
            super(code);
        }

        @Override
        public void exec(Interpreter executor) {
            throw new ScriptDisableException(ScriptNames.values()[code].name());
        }
    }

    static class OP_BOOLAND extends AbstractScriptNoOperand {
        public OP_BOOLAND(int code) {
            super(code);
        }

        @Override
        public void exec(Interpreter executor) {
            int a = executor.stack.assertSizeGE(2).popInt();
            int b = executor.stack.popInt();
            if (a != 0 && b != 0) {
                executor.stack.push(1);
            } else {
                executor.stack.push(0);
            }
        }
    }

    static class OP_BOOLOR extends AbstractScriptNoOperand {
        public OP_BOOLOR(int code) {
            super(code);
        } // 155

        @Override
        public void exec(Interpreter executor) {
            int a = executor.stack.assertSizeGE(2).popInt();
            int b = executor.stack.popInt();
            if (a != 0 || b != 0) {
                executor.stack.push(1);
            } else {
                executor.stack.push(0);
            }
        }
    }

    static class OP_NUMEQUAL extends AbstractScriptNoOperand {
        public OP_NUMEQUAL(int code) {
            super(code);
        }

        @Override
        public void exec(Interpreter executor) {
            int a = executor.stack.assertSizeGE(2).popInt();
            int b = executor.stack.popInt();
            if (a == b) {
                executor.stack.push(1);
            } else {
                executor.stack.push(0);
            }
        }
    }

    //OP_NUMEQUALVERIFY,
    //OP_NUMNOTEQUAL,
    //OP_LESSTHAN,
    //OP_GREATERTHAN, // 160
    //OP_LESSTHANOREQUAL,
    //OP_GREATERTHANOREQUAL,

    static class OP_MIN extends AbstractScriptNoOperand {
        public OP_MIN(int code) {
            super(code);
        }

        @Override
        public void exec(Interpreter executor) {
            // 3ee060fb1856f111859fb108d079635a2d225ef68d5ae5250ce70d39ac2a2dc4
            byte[] p1 = executor.stack.assertSizeGE(2).pop();
            byte[] p2 = executor.stack.pop();
            BigInteger min = new BigInteger(ByteUtil.reverse(p1)).min(new BigInteger(ByteUtil.reverse(p2)));
            executor.stack.push(ByteUtil.reverse(min.toByteArray()));
        }
    }

    static class OP_MAX extends AbstractScriptNoOperand {
        public OP_MAX(int code) {
            super(code);
        }

        @Override
        public void exec(Interpreter executor) {
            byte[] p1 = executor.stack.assertSizeGE(2).pop();
            byte[] p2 = executor.stack.pop();
            BigInteger min = new BigInteger(ByteUtil.reverse(p1)).max(new BigInteger(ByteUtil.reverse(p2)));
            executor.stack.push(ByteUtil.reverse(min.toByteArray()));
        }
    }

    static class OP_WITHIN extends AbstractScriptNoOperand {
        public OP_WITHIN(int code) {
            super(code);
        }

        @Override
        public void exec(Interpreter executor) {
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
    } // 165

    ;
}
