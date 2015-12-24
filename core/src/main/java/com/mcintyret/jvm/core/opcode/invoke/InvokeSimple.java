package com.mcintyret.jvm.core.opcode.invoke;

import com.mcintyret.jvm.core.clazz.Method;
import com.mcintyret.jvm.core.clazz.NativeMethod;
import com.mcintyret.jvm.core.exec.Execution;
import com.mcintyret.jvm.core.exec.OperationContext;
import com.mcintyret.jvm.core.exec.Variables;
import com.mcintyret.jvm.parse.Modifier;

/**
 * User: tommcintyre
 * Date: 5/20/14
 */
abstract class InvokeSimple extends Invoke {

    @Override
    protected final void doInvoke(Method method, Variables args, OperationContext ctx) {
        if (method.hasModifier(Modifier.NATIVE)) {
            invokeNativeMethod((NativeMethod) method, args, ctx);
        } else {
            ctx.getExecutionStack().push(
                new Execution(method, args, ctx.getThread()));
        }
    }

}
