package com.mcintyret.jvm.core.opcode;

import com.mcintyret.jvm.core.exec.OperationContext;
import com.mcintyret.jvm.core.exec.VariableStack;

public abstract class BinaryIntOp extends OpCode {

    @Override
    public final void execute(OperationContext ctx) {
        VariableStack stack = ctx.getStack();
        stack.pushInt(binaryOp(stack.popInt(), stack.popInt()));
    }

    protected abstract int binaryOp(int a, int b);

}
