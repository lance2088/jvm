package com.mcintyret.jvm.core.opcode.constant;

import com.mcintyret.jvm.core.opcode.OpCode;
import com.mcintyret.jvm.core.exec.OperationContext;

class FConst_1 extends OpCode {

    @Override
    public void execute(OperationContext ctx) {
        ctx.getStack().push(1F);
    }

    @Override
    public byte getByte() {
        return 0x0C;
    }
}
