package com.mcintyret.jvm.core;

import com.mcintyret.jvm.core.clazz.ClassObject;
import com.mcintyret.jvm.core.exec.Execution;
import com.mcintyret.jvm.core.exec.Thread;
import com.mcintyret.jvm.core.exec.Threads;
import com.mcintyret.jvm.core.exec.Variables;
import com.mcintyret.jvm.core.oop.Oop;
import com.mcintyret.jvm.core.oop.OopArray;
import com.mcintyret.jvm.core.oop.OopClass;
import com.mcintyret.jvm.core.type.SimpleType;
import com.mcintyret.jvm.core.util.Utils;
import com.mcintyret.jvm.load.ClassLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Phaser;
import java.util.concurrent.atomic.AtomicInteger;

public class Heap {

    private static final Logger LOG = LoggerFactory.getLogger(Heap.class);

    private static final int INITIAL_HEAP_SIZE = 32;

    private static final int MAX_HEAP_SIZE = 10000;

    private static volatile Oop[] OOP_TABLE = new Oop[INITIAL_HEAP_SIZE];

    private static AtomicInteger heapAllocationPointer = new AtomicInteger();

    public static final int NULL_POINTER = 0;

    private static final StringPool STRING_POOL = new StringPool();

    private static final ConcurrentMap<java.lang.Thread, NativeMethodParts> nativeMethodArgs = new ConcurrentHashMap<>();

    public static void enterNativeMethod() {
        nativeMethodArgs.put(currentThread(), new NativeMethodParts());
    }

    public static void exitNativeMethod() {
        nativeMethodArgs.remove(currentThread());
    }

    private static java.lang.Thread currentThread() {
        return java.lang.Thread.currentThread();
    }

    public static Oop getOop(int address) {
        if (address == NULL_POINTER) {
            return null;
        }
        Oop oop = OOP_TABLE[address];
        if (oop == null) {
            throw new IllegalArgumentException("No Oop found at address " + address);
        }
        return oop;
    }

    public static OopClass getOopClass(int address) {
        return (OopClass) getOop(address);
    }

    public static OopArray getOopArray(int address) {
        return (OopArray) getOop(address);
    }

    public static void register() {
        GC_PHASER.register();
    }

    public static void deregister() {
        GC_PHASER.arriveAndDeregister();
    }

    public static void atSafePoint() {
        if (GC_PHASER.getArrivedParties() > 0) {
            GC_PHASER.arriveAndAwaitAdvance();
        }
    }

    private static final Phaser GC_PHASER = new Phaser() {
        @Override
        protected boolean onAdvance(int ignoredA, int ignoredB) {
            garbageCollection();

            return false;
        }
    };

    private static void garbageCollection() {
        // At this point we know that all the threads are awaiting the Phaser (!)

        OOP_TABLE = new GarbageCollector().run();
    }

    public static void registerNativeMethodArgs(Variables argArray) {
        nativeMethodArgs.computeIfPresent(currentThread(), (t, nmp) -> {
            nmp.variables.add(argArray);
            return nmp;
        });
    }

    private static class GarbageCollector {

        private static final float MAX_THRESHOLD = 0.85F;

        private final Map<Integer, Integer> newOopAddresses = new HashMap<>();

        private final HeapChecker heapChecker = new HeapChecker();

        // Optimistically assume we can reclaim enough resources that the existing heap size is big enough
        private int index = 1; // 0 is reserved for NULL_POINTER
        private Oop[] newOops = new Oop[OOP_TABLE.length];


        public Oop[] run() {
            LOG.warn("Starting GC, initial heap size: {}", OOP_TABLE.length);
            for (Oop oop : OOP_TABLE) {
                if (oop != null) {
                    heapChecker.register(oop);
                }
            }

            // The String Pool
            // TODO: some kind of PERM_GEN so we don't have to do this every time
            for (Oop oop : STRING_POOL.lookupMap.values()) {
                gcOop(oop);
            }

            // All the execution stacks in all the live threads
            for (Thread thread : Threads.getAll()) {
                for (Execution stack : thread.getExecutions()) {
                    gcVariables(stack.getLocalVariables());
                    gcVariables(stack.getStack().asVariables());
                }
                gcOop(thread.getThisThread()); // Remember the thread itself
            }

            // Run for any objects currently being created in native methods
            nativeMethodArgs.computeIfPresent(currentThread(), (t, nmp) -> {
                nmp.variables.forEach(this::gcVariables);
                nmp.oops.forEach(this::gcOop);
                return nmp;
            });

            // finally look at static objects
            for (ClassObject classObject : ClassLoader.getDefaultClassLoader().getLoadedClasses()) {
                gcOop(classObject.getOop(true)); // this will keep any reflection data.

                gcVariables(classObject.getStaticFieldValues());
            }

            for (int i = 1; i < newOops.length; i++) {
                Oop oop = newOops[i];
                if (oop == null) {
                    break;
                }
                oop.getMarkRef().setLive(false); // reset for the next GC
            }

            if (index > MAX_THRESHOLD * newOops.length) {
                expand();
            }

            LOG.warn("Finished GC, final heap size: {}", index);

            heapAllocationPointer.set(index - 1);

            heapChecker.confirmOops();

            return newOops;
        }

        private void gcVariables(Variables variables) {
            for (int i = 0; i < variables.length(); i++) {
                if (variables.getType(i) == SimpleType.REF) {
                    int address = variables.getRawValue(i);

                    if (address == NULL_POINTER) {
                        continue;
                    }

                    Integer newAddress = newOopAddresses.get(address);

                    if (newAddress != null) {
                        // we have already GC'd the Oop that this address pointed to and moved it to a new address.
                        // Update this reference and move on
                        variables.put(i, SimpleType.REF, newAddress);
                        continue;
                    }

                    Oop oop = variables.getOop(i);

                    gcOop(oop);

                    variables.put(i, SimpleType.REF, oop.getAddress()); // update the address
                }
            }
        }

        private void gcOop(Oop oop) {
            if (oop != null && !oop.getMarkRef().isLive()) {
                // we haven't visited this one yet
                oop.getMarkRef().setLive(true);

                checkOopIsOnHeap(oop);

                // Actually keep the added Oop for the next cycle. This updates the Oop's address
                int oldAddress = oop.getAddress();
                addOop(oop);
                int newAddress = oop.getAddress();

                // Update the table before we recurse into this Oop's fields
                // - this ensures that circular references remain valid
                Integer oldAddressFromMap = newOopAddresses.put(oldAddress, newAddress);
                if (oldAddressFromMap != null) {
                    throw new IllegalStateException("Oop GCd multiple times");
                }

                gcVariables(oop.getFields());

            }
        }

        private void checkOopIsOnHeap(Oop oop) {
            for (Oop heapOop : OOP_TABLE) {
                if (heapOop == oop) {
                    return;
                }
            }
            throw new IllegalStateException();
        }

        private void addOop(Oop oop) {
            if (index == newOops.length) {
                expand();
            }
            newOops[index] = oop;
            oop.setAddress(index);
            index++;
        }

        private void expand() {
            Oop[] tmp = new Oop[newOops.length * 2];
            System.arraycopy(newOops, 0, tmp, 0, index);
            newOops = tmp;
        }

        private class HeapChecker {

            private final Map<Oop, Oop[]> updatedOops = new HashMap<>();

            public void register(Oop oop) {
                Variables vars = oop.getFields();
                Oop[] array = new Oop[vars.length()];
                for (int i = 0; i < vars.length(); i++) {
                    if (vars.getType(i) == SimpleType.REF) {
                        array[i] = Heap.getOop(vars.getRawValue(i));
                    }
                }
                updatedOops.put(oop, array);
            }

            public void confirmOops() {
                for (int i = 1; i < newOops.length; i++) {
                    Oop oop = newOops[i];
                    if (oop == null) {
                        break;
                    }

                    Oop[] array = updatedOops.remove(oop);

                    if (array == null) {
                        throw new IllegalStateException("Didn't register Oop");
                    }

                    Variables vars = oop.getFields();
                    for (int v = 0; v < vars.length(); v++) {
                        if (vars.getType(v) == SimpleType.REF) {
                            if (array[v] != newOops[vars.getRawValue(v)]) {
                                throw new IllegalStateException("Oops don't match");
                            }
                        }
                    }
                }

//                if (!updatedOops.isEmpty()) {
//                    throw new IllegalStateException("Registered Oop not on new heap");
//                }
            }

        }

    }

    public static int allocate(Oop oop) {
        int heapPointer = heapAllocationPointer.incrementAndGet();

        if (heapPointer >= OOP_TABLE.length) {
            if (heapPointer >= MAX_HEAP_SIZE) {
                throw new OutOfMemoryError("No more heap space! Should probably do some kind of GC...");
            }

            // need to do some GCing!
            GC_PHASER.arriveAndAwaitAdvance();

            // try again
            return allocate(oop);
        }

        OOP_TABLE[heapPointer] = oop;
        oop.setAddress(heapPointer);

        nativeMethodArgs.computeIfPresent(currentThread(), (t, nmp) -> {
            nmp.oops.add(oop);
            return nmp;
        });

        return heapPointer;
    }


    public static <O extends Oop> O allocateAndGet(O oop) {
        allocate(oop);
        return oop;
    }

    public static int intern(String string) {
        return STRING_POOL.intern(string);
    }

    private static class StringPool {

        private final Map<String, Oop> lookupMap = new HashMap<>();

        public int intern(String string) {

            Oop stringOop = lookupMap.get(string);
            if (stringOop == null) {
                stringOop = Utils.toOopString(string);
                lookupMap.put(string, stringOop);
            }
            return stringOop.getAddress();
        }
    }

    private static class NativeMethodParts {

        private final Set<Oop> oops = new HashSet<>();

        private final Set<Variables> variables = new HashSet<>();

    }

}
