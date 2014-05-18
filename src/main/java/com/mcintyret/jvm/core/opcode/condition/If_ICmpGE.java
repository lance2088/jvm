package com.mcintyret.jvm.core.opcode.condition;

public class If_ICmpGE extends BinaryCondition {

    @Override
    protected boolean conditionMet(int a, int b) {
        return a >= b;
    }

    @Override
    public byte getByte() {
        return (byte) 0xA2;
    }
}
