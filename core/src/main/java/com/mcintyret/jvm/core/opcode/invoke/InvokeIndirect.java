package com.mcintyret.jvm.core.opcode.invoke;

import com.mcintyret.jvm.core.clazz.Method;
import com.mcintyret.jvm.core.clazz.NativeMethod;
import com.mcintyret.jvm.core.exec.ExecutionStackElement;
import com.mcintyret.jvm.core.exec.OperationContext;
import com.mcintyret.jvm.core.exec.Variables;
import com.mcintyret.jvm.core.oop.Oop;
import com.mcintyret.jvm.parse.Modifier;

abstract class InvokeIndirect extends Invoke {

    @Override
    protected final void doInvoke(Method method, OperationContext ctx) {
        Variables args = getMethodArgs(ctx, method);

        Method implementation = getImplementationMethod(method, args.getOop(0));

        if (implementation.hasModifier(Modifier.NATIVE)) {
            invokeNativeMethod((NativeMethod) method, args, ctx);
        } else {
            int maxLocalVars = implementation.getCode().getMaxLocals();
            if (maxLocalVars > args.length()) {
                // TODO: refactor with similar code in VariableStackImpl
                Variables tmp = new Variables(maxLocalVars);
                System.arraycopy(args.getRawValues(), 0, tmp.getRawValues(), 0, args.length());
                System.arraycopy(args.getTypes(), 0, tmp.getTypes(), 0, args.length());
                args = tmp;
            }

            ctx.getExecutionStack().push(
                new ExecutionStackElement(implementation, args, implementation.getClassObject().getConstantPool(), ctx.getExecutionStack()));
        }

        afterInvoke(ctx);
    }

    protected void afterInvoke(OperationContext ctx) {
        // Do nothing by default
    }

    protected abstract Method getImplementationMethod(Method method, Oop oop);

}
