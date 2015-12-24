package com.mcintyret.jvm.core.opcode.push;

import com.mcintyret.jvm.core.opcode.OpCode;
import com.mcintyret.jvm.core.exec.OperationContext;

class BIPush extends OpCode {
    @Override
    public void execute(OperationContext ctx) {
        ctx.getStack().push(ctx.getByteIterator().nextByte());
    }

    @Override
    public byte getByte() {
        return 0x10;
    }
}
