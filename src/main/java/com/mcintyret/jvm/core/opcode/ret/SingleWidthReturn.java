package com.mcintyret.jvm.core.opcode.ret;

import com.mcintyret.jvm.core.exec.VariableStack;
import com.mcintyret.jvm.core.nativeimpls.NativeReturn;

abstract class SingleWidthReturn extends BaseValueReturn {

    @Override
    protected final NativeReturn finalReturn(VariableStack stack) {
        return NativeReturn.forInt(stack.pop());
    }

    @Override
    protected final void pushReturnVal(VariableStack lower, VariableStack upper) {
        lower.push(upper.pop());
    }
}