package com.mcintyret.jvm.core.opcode.condition;

class IfEq extends UnaryCondition {

    @Override
    public byte getByte() {
        return (byte) 0x99;
    }

    @Override
    protected boolean conditionMet(int pop) {
        return pop == 0;
    }
}
