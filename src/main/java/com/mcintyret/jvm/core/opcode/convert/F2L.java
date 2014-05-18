package com.mcintyret.jvm.core.opcode.convert;

import com.mcintyret.jvm.core.WordStack;
import com.mcintyret.jvm.core.opcode.OpCode;
import com.mcintyret.jvm.core.opcode.OperationContext;

class F2L extends OpCode {

    @Override
    public void execute(OperationContext ctx) {
        WordStack stack = ctx.getStack();
        stack.push((long) stack.popFloat());
    }

    @Override
    public byte getByte() {
        return (byte) 0x8C;
    }
}

