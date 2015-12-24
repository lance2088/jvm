package com.mcintyret.jvm.core.opcode.aload;

import com.mcintyret.jvm.core.Heap;
import com.mcintyret.jvm.core.oop.OopArray;
import com.mcintyret.jvm.core.exec.WordStack;
import com.mcintyret.jvm.core.opcode.OpCode;
import com.mcintyret.jvm.core.exec.OperationContext;

abstract class DoubleWidthALoad extends OpCode {

    @Override
    public final void execute(OperationContext ctx) {
        WordStack stack = ctx.getStack();

        int index = stack.pop() * 2;
        OopArray array = (OopArray) Heap.getOop(stack.pop());

        stack.push(array.getFields()[index]);
        stack.push(array.getFields()[index + 1]);
    }

}
