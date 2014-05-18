package com.mcintyret.jvm.core.opcode.constant;

import com.mcintyret.jvm.core.opcode.OpCode;
import com.mcintyret.jvm.core.opcode.OperationContext;

class Ldc extends OpCode {

    @Override
    public void execute(OperationContext ctx) {
        Integer i = (Integer) ctx.getConstantPool().get(ctx.getByteIterator().next());

        ctx.getStack().push(i);
    }

    @Override
    public byte getByte() {
        return 0x12;
    }
}
