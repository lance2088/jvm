package com.mcintyret.jvm.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Main {

    public static void main(String[] args) throws IOException {
        for (int i = 0; i < 100; i++) {
//            threadingTests();
//            simpleIntArrays();
//            interfaceMethods();
//            stringsAndNatives();
//            stringLength();
//            readFile();
        }
//        readFromClassPath();
//        scanner();
        testZip();
//        assignableFrom();
    }

    private static void assignableFrom() {
        System.out.println(List.class.isAssignableFrom(ArrayList.class));
        System.out.println(ArrayList.class.isAssignableFrom(List.class));
        System.out.println(List.class.isAssignableFrom(List.class));
        System.out.println(ArrayList.class.isAssignableFrom(ArrayList.class));

        System.out.println(new ArrayList<>() instanceof List);
        System.out.println(new HashSet<>() instanceof List);
    }

    private static void testZip() throws IOException {
        File file = new File(System.getProperty("user.dir") + "/docs/zipped.zip");

        ZipFile zipFile = new ZipFile(file);

        Enumeration<? extends ZipEntry> it = zipFile.entries();
        ZipEntry contents = it.nextElement();

        System.out.println(contents.getName());
        System.out.println(toString(zipFile.getInputStream(contents)));

        contents = zipFile.getEntry("zipped.txt");
        System.out.println(contents.getName());
        System.out.println(toString(zipFile.getInputStream(contents)));
    }

    private static void scanner() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Ready for input");

        String line;
        boolean running = true;
        while (running) {
            line = scanner.nextLine();
            System.out.println(reverse(line));
            running = false;
        }
    }

    private static void readFile() throws IOException {
        File file = new File(System.getProperty("user.dir") + "/docs/testFile.txt");
        InputStream stream = new FileInputStream(file);

        String string = toString(stream);

        System.out.println(string);
        System.err.println(string);

    }

    private static void threadingTests() {
        threadingTestAtomicInteger();

        threadingTestSynchronizedBlock();
        threadingTestSynchronizedStaticMethod();
        threadingTestSynchronizedInstanceMethod();

        threadingTestSynchronizedBlockThrowingException();
        threadingTestSynchronizedStaticMethodThrowingException();
        threadingTestSynchronizedInstanceMethodThrowingException();

        threadingTestSynchronizedNativeStaticMethod();
        threadingTestSynchronizedNativeInstanceMethod();
        threadingTestSynchronizedNativeStaticMethodThrowingException();
        threadingTestSynchronizedNativeInstanceMethodThrowingException();
    }

    private static void readFromClassPath() throws IOException {
        InputStream stream = Main.class.getResourceAsStream("/testFile.txt");

        System.out.println(toString(stream));
    }

    private static String toString(InputStream stream) throws IOException {
        byte[] bytes = new byte[1024];
        stream.read(bytes);
        stream.close();
        return new String(bytes);
    }

    private static void stringLength() {
        print(String.valueOf("foo".length()));
    }

    private static void interfaceMethods() {
        Foo foo = new FooBar();
        print(foo.doFoo());
    }

    private static void stringsAndNatives() {
        String str = "HELLO WORLD";

        String reversed = reverse(str);

        print(reversed);
        System.out.println(reversed);
        System.err.println("Now I'm printing an error message!");

        try {
            doFoo();
        } catch (FooBarException e) {
            print("Caught the exception!");
            print(Arrays.toString(e.getStackTrace()));
//            throw new RuntimeException();
        }

        isAssignableFrom();
    }

    private static void doSimpleThreadingTest(int num, Runnable action) {
        final Random random = new Random();
        final CountDownLatch latch = new CountDownLatch(num);
        for (int i = 0; i < num; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(100 + random.nextInt(50));
                    } catch (InterruptedException e) {
                        throw new AssertionError(e);
                    }
                    try {
                        action.run();
                    } finally {
                        latch.countDown();
                    }
                }
            }).start();
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new AssertionError(e);
        }
    }

    // Test threading using an AtomicInteger
    private static void threadingTestAtomicInteger() {
        int num = 40;
        AtomicInteger ai = new AtomicInteger();
        doSimpleThreadingTest(num, new Runnable() {
            @Override
            public void run() {
                ai.incrementAndGet();
            }
        });
        System.out.println("Should be " + num + ": " + ai);
    }

    // Test threading using a synchronized block
    private static void threadingTestSynchronizedBlock() {
        final int[] a = new int[1];
        int num = 4;

        doSimpleThreadingTest(num, new Runnable() {
            @Override
            public void run() {
                synchronized (a) {
                    a[0]++;
                }
            }
        });

        System.out.println("Should be " + num + ": " + a[0]);
    }

    // Test threading using a synchronized static method
    private static void threadingTestSynchronizedStaticMethod() {
        final int[] a = new int[1];
        int num = 40;

        doSimpleThreadingTest(num, new Runnable() {
            @Override
            public void run() {
                incrementStatic(a);
            }
        });

        System.out.println("Should be " + num + ": " + a[0]);
    }


    // Test threading using a synchronized instance method
    private static void threadingTestSynchronizedInstanceMethod() {
        final int[] a = new int[1];
        int num = 40;

        class Sync {
            synchronized void incrementFirst(int[] a) {
                a[0]++;
            }
        }

        Sync sync = new Sync();

        doSimpleThreadingTest(num, new Runnable() {
            @Override
            public void run() {
                sync.incrementFirst(a);
            }
        });

        System.out.println("Should be " + num + ": " + a[0]);
    }

    // Test threading using a synchronized block and throwing an exception
    private static void threadingTestSynchronizedBlockThrowingException() {
        final int[] a = new int[2];
        int num = 40;

        doSimpleThreadingTest(num, new Runnable() {
            @Override
            public void run() {
                synchronized (a) {
                    incrementExceptionNotSynchronized(a);
                }
            }
        });

        System.out.println("Should be " + num + ": " + a[0]);
        System.out.println("Should be " + (num / 2) + ": " + a[1]);
    }

    // Test threading using a synchronized static method and throwing an exception
    private static void threadingTestSynchronizedStaticMethodThrowingException() {
        final int[] a = new int[2];
        int num = 40;

        doSimpleThreadingTest(num, new Runnable() {
            @Override
            public void run() {
                incrementStaticException(a);
            }
        });

        System.out.println("Should be " + num + ": " + a[0]);
        System.out.println("Should be " + (num / 2) + ": " + a[1]);
    }

    // Test threading using a synchronized instance method and throwing an exception
    private static void threadingTestSynchronizedInstanceMethodThrowingException() {
        final int[] a = new int[2];
        int num = 40;

        class Sync {
            synchronized void incrementException(int[] a) {
                incrementExceptionNotSynchronized(a);
            }
        }

        Sync sync = new Sync();

        doSimpleThreadingTest(num, new Runnable() {
            @Override
            public void run() {
                sync.incrementException(a);
            }
        });

        System.out.println("Should be " + num + ": " + a[0]);
        System.out.println("Should be " + (num / 2) + ": " + a[1]);
    }

    // Test threading using a synchronized static native method
    private static void threadingTestSynchronizedNativeStaticMethod() {
        final int[] a = new int[1];
        int num = 40;

        doSimpleThreadingTest(num, new Runnable() {
            @Override
            public void run() {
                incrementNativeStatic(a);
            }
        });

        System.out.println("Should be " + num + ": " + a[0]);
    }


    // Test threading using a synchronized instance native method
    private static void threadingTestSynchronizedNativeInstanceMethod() {
        final int[] a = new int[1];
        int num = 40;

        Main main = new Main();

        doSimpleThreadingTest(num, new Runnable() {
            @Override
            public void run() {
                main.incrementNativeInstance(a);
            }
        });

        System.out.println("Should be " + num + ": " + a[0]);
    }

    // Test threading using a synchronized native static method and throwing an exception
    private static void threadingTestSynchronizedNativeStaticMethodThrowingException() {
        final int[] a = new int[2];
        int num = 40;

        doSimpleThreadingTest(num, new Runnable() {
            @Override
            public void run() {
                incrementNativeStaticException(a);
            }
        });

        System.out.println("Should be " + num + ": " + a[0]);
        System.out.println("Should be " + (num / 2) + ": " + a[1]);
    }

    // Test threading using a synchronized native instance method and throwing an exception
    private static void threadingTestSynchronizedNativeInstanceMethodThrowingException() {
        final int[] a = new int[2];
        int num = 40;

        Main main = new Main();

        doSimpleThreadingTest(num, new Runnable() {
            @Override
            public void run() {
                main.incrementNativeInstanceException(a);
            }
        });

        System.out.println("Should be " + num + ": " + a[0]);
        System.out.println("Should be " + (num / 2) + ": " + a[1]);
    }


    private static synchronized void incrementStatic(int[] a) {
        a[0]++;
    }

    private static synchronized void incrementStaticException(int[] a) {
        incrementExceptionNotSynchronized(a);
    }

    private static void incrementExceptionNotSynchronized(int[] a) {
        if (a[0]++ % 2 == 0) {
            // Testing whether we give up the lock on exiting via an exception
            throw new RuntimeException();
        }
        a[1]++;
    }

    private static void isAssignableFrom() {
        System.out.println("Should be true: " + Integer.class.isAssignableFrom(Number.class));
        System.out.println("Should be false: " + Integer.class.isAssignableFrom(String.class));
        System.out.println("Should be true: " + HashMap.class.isAssignableFrom(Map.class));
    }

    private static void doFoo() throws FooBarException {
        throw new FooBarException();
    }

    private static void simpleIntArrays() {
        int[] array = new int[10];
        int a = 6;
        array[5] = 17;
        int res = array[a - 1] + a;

        char[] chars = {'h', 'e', 'l', 'l', 'o', ' ', 'w', 'o', 'r', 'l', 'd'};
    }

    private static String reverse(String in) {
        char[] chars = new char[in.length()];
        for (int i = 0; i < chars.length; i++) {
            chars[i] = in.charAt(in.length() - (i + 1));
        }
        return new String(chars);
    }

    private static native void print(String in);

    public static class FooBarException extends Exception {

    }

    private synchronized native void incrementNativeInstance(int[] a);

    private static synchronized native void incrementNativeStatic(int[] a);

    private synchronized native void incrementNativeInstanceException(int[] a);

    private static synchronized native void incrementNativeStaticException(int[] a);
}
