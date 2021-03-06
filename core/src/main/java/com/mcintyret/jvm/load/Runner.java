package com.mcintyret.jvm.load;

import com.mcintyret.jvm.core.Heap;
import com.mcintyret.jvm.core.clazz.ArrayClassObject;
import com.mcintyret.jvm.core.clazz.ClassObject;
import com.mcintyret.jvm.core.clazz.Field;
import com.mcintyret.jvm.core.clazz.Method;
import com.mcintyret.jvm.core.exec.Thread;
import com.mcintyret.jvm.core.exec.Threads;
import com.mcintyret.jvm.core.exec.VariableStack;
import com.mcintyret.jvm.core.exec.VariableStackImpl;
import com.mcintyret.jvm.core.exec.Variables;
import com.mcintyret.jvm.core.nativeimpls.NativeReturn;
import com.mcintyret.jvm.core.oop.OopArray;
import com.mcintyret.jvm.core.oop.OopClass;
import com.mcintyret.jvm.core.type.ArrayType;
import com.mcintyret.jvm.core.type.MethodSignature;
import com.mcintyret.jvm.core.type.NonArrayType;
import com.mcintyret.jvm.core.type.SimpleType;
import com.mcintyret.jvm.core.util.Utils;

import java.io.IOException;

public class Runner {

    private static final MethodSignature MAIN_METHOD_SIGNATURE = MethodSignature.parse("main", "([Ljava/lang/String;)V");

    // TODO: is there a nicer way?
    public static Thread MAIN_THREAD = new Thread(null, (java.lang.Thread) null); // Just for bootstrap!

    public void run(ClassPath classPath, String mainClassName, String... args) throws IOException {
        ClassLoader classLoader = ClassLoader.createClassLoader(classPath);

        // This happens early!
        Heap.registerThread();
        MAIN_THREAD = createMainThread();

        classLoader.afterInitialLoad(); // Sets System.out. Can I do this anywhere else??

        ClassObject mainClass = classLoader.getClassObject(mainClassName);

        Method mainMethod = findMainMethod(mainClass);

        ArrayClassObject aco = ArrayClassObject.forType(ArrayType.create(NonArrayType.forClass("java/lang/String"), 1));
        OopArray array = aco.newArray(args.length);
        Heap.allocate(array);

        for (int i = 0; i < args.length; i++) {
            array.getFields().put(i, SimpleType.REF, Heap.intern(args[i]));
        }

        Variables actualArgs = mainMethod.newArgArray(array);

        NativeReturn ret = Utils.executeMethodAndThrow(mainMethod, actualArgs, MAIN_THREAD);

        if (ret != null) {
            VariableStack stack = new VariableStackImpl(1);
            ret.applyValue(stack);
            try {
                OopClass obj = stack.popOop();
                if (obj.getClassObject().isInstanceOf(classLoader.getClassObject("java/lang/Throwable"))) {
                    System.out.println("Died with Exception of type: " + obj.getClassObject().getClassName());
                }

            } catch (Throwable foo) {
                // ignore
            }
        }

    }


    private static Method findMainMethod(ClassObject mainClass) {
        for (Method method : mainClass.getStaticMethods()) {
            if (method.getSignature().equals(MAIN_METHOD_SIGNATURE)) {
                return method;
            }
        }
        throw new IllegalStateException("No main method found on main class " + mainClass.getType());
    }


    private static Thread createMainThread() {
        ClassObject threadClass = ClassLoader.getClassLoader().getClassObject("java/lang/Thread");
        ClassObject threadGroupClass = ClassLoader.getClassLoader().getClassObject("java/lang/ThreadGroup");

        Method systemThreadGroupCtor = threadGroupClass.getDefaultConstructor();
        OopClass systemThreadGroup = threadGroupClass.newObject();

        Variables args = systemThreadGroupCtor.newArgArray();
        args.put(0, SimpleType.REF, Heap.allocate(systemThreadGroup));

        Utils.executeMethodAndThrow(systemThreadGroupCtor, args, MAIN_THREAD);

        // Cool, now we need the 'main' threadgroup...
        Method mainThreadGroupCtor =
            threadGroupClass.findConstructor("(Ljava/lang/Void;Ljava/lang/ThreadGroup;Ljava/lang/String;)V");
        OopClass mainThreadGroup = threadGroupClass.newObject();

        OopClass mainString = Heap.getOopClass(Heap.intern("main"));

        args = mainThreadGroupCtor.newArgArray();
        args.put(0, SimpleType.REF, Heap.allocate(mainThreadGroup));
        args.put(1, SimpleType.REF, Heap.NULL_POINTER);
        args.put(2, SimpleType.REF, systemThreadGroup.getAddress()); // parent
        args.put(3, SimpleType.REF, mainString.getAddress()); // name

        Utils.executeMethodAndThrow(mainThreadGroupCtor, args, MAIN_THREAD);

        // OK, now we can create the main thread
        OopClass mainThread = threadClass.newObject();
        Heap.allocate(mainThread);

        // The pain here is that we can't run the constructor because that depends on Thread.currentThread(),
        // which in turn depends on having the main thread properly initialized!

        // are there more fields we care about?
        Field name = threadClass.findField("name", false);
        // Thread.name is a char array!
        name.set(mainThread, mainString.getFields().getOop(0));

        Field group = threadClass.findField("group", false);
        group.set(mainThread, mainThreadGroup);

        // This is a long!
        Field tid = threadClass.findField("tid", false);
        tid.set(mainThread, 0, 1);

        Field tidGen = threadClass.findField("threadSeqNumber", true);
        tidGen.set(null, 1L);

        Field priority = threadClass.findField("priority", false);
        priority.set(mainThread, 5);

        Field threadStatus = threadClass.findField("threadStatus", false);
        threadStatus.set(mainThread, 1); // I think this is RUNNABLE

        Thread main = new Thread(mainThread, java.lang.Thread.currentThread());
        Threads.register(main);
        return main;
    }
}
