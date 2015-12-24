package com.mcintyret.jvm.core.opcode;

import java.util.List;

import com.mcintyret.jvm.core.exec.OperationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mcintyret.jvm.core.util.ByteIterator;
import com.mcintyret.jvm.core.exec.ExecutionStackElement;
import com.mcintyret.jvm.core.Heap;
import com.mcintyret.jvm.core.clazz.AbstractClassObject;
import com.mcintyret.jvm.core.clazz.Method;
import com.mcintyret.jvm.core.constantpool.ConstantPool;
import com.mcintyret.jvm.core.nativeimpls.NativeReturn;
import com.mcintyret.jvm.core.oop.OopClass;
import com.mcintyret.jvm.parse.attribute.CodeException;

/**
 * User: tommcintyre
 * Date: 5/26/14
 */
public class AThrow extends OpCode {

    private static final Logger LOG = LoggerFactory.getLogger(AThrow.class);

    @Override
    public void execute(OperationContext ctx) {
        OopClass thrown = Heap.getOopClass(ctx.getStack().pop());
        LOG.warn("Throwing exception of type {} from method {}", thrown.getClassObject(), ctx.getMethod());

        ExecutionStackElement elem = ctx.getExecutionStack().peek();
        ExecutionStackElement prev = null;
        while (elem != null) {
            Method m = elem.getMethod();
            ConstantPool cp = m.getClassObject().getConstantPool();
            List<CodeException> exceptions = m.getCode().getCodeExceptions();
            ByteIterator bi = elem.getByteIterator();
            int pos = bi.getPos();

            for (CodeException exception : exceptions) {
                if (pos >= exception.getStartPc() && pos <= exception.getEndPc()) {
                    int catchType = exception.getCatchType();
                    boolean caught = false;

                    if (catchType == 0) {
                        LOG.info("In finally block of method {}", ctx.getMethod());
                        caught = true;
                    } else {
                        AbstractClassObject catchTypeClass = cp.getClassObject(catchType);
                        if (thrown.getClassObject().isInstanceOf(catchTypeClass)) {
                            LOG.info("In catch block of method {} for Exception type {}", elem.getMethod(), catchTypeClass);
                            caught = true;
                        }
                    }

                    if (caught) {
                        bi.setPos(exception.getHandlerPc());

                        elem.getStack().clear();
                        elem.getStack().push(thrown.getAddress());
                        return;
                    }
                }
            }

            ctx.getExecutionStack().pop();
            prev = elem;
            elem = ctx.getExecutionStack().peek();
        }

        // If we're here, the Exception has gone all the way to the top.
        LOG.warn("Did not catch error of type {}. Exiting from method {}", thrown.getClassObject(), prev.getMethod());
        ctx.getExecutionStack().setFinalReturn(NativeReturn.forThrowable(thrown));
    }


    @Override
    public byte getByte() {
        return (byte) 0xBF;
    }
}
