package com.mcintyret.jvm.core.opcode.math.rem;

import com.mcintyret.jvm.core.opcode.BinaryIntOp;

class IRem extends BinaryIntOp {

    @Override
    protected int binaryOp(int a, int b) {
        return b % a;
    }

    @Override
    public byte getByte() {
        return 0x70;
    }

}
