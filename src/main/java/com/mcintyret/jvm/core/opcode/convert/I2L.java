package com.mcintyret.jvm.core.opcode.convert;

import com.mcintyret.jvm.core.WordStack;
import com.mcintyret.jvm.core.opcode.OpCode;
import com.mcintyret.jvm.core.opcode.OperationContext;

class I2L extends OpCode {

    @Override
    public void execute(OperationContext ctx) {
        WordStack stack = ctx.getStack();
        stack.push((long) stack.pop());
    }

    @Override
    public byte getByte() {
        return (byte) 0x85;
    }
}


