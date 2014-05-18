package com.mcintyret.jvm.core.opcode.constant;

import com.mcintyret.jvm.core.opcode.OpCode;
import com.mcintyret.jvm.core.opcode.OperationContext;

class IConst_2 extends OpCode {

    @Override
    public void execute(OperationContext ctx) {
        ctx.getStack().push(2);
    }

    @Override
    public byte getByte() {
        return 0x05;
    }
}


