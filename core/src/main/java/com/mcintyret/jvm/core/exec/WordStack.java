package com.mcintyret.jvm.core.exec;

import java.util.NoSuchElementException;

import com.mcintyret.jvm.core.clazz.ValueReceiver;
import com.mcintyret.jvm.core.util.Utils;

public class WordStack implements ValueReceiver {

    private static final int DEFAULT_SIZE = 10;

    private int[] stack;

    private int head = 0;

    public WordStack(int size) {
        stack = new int[size];
    }

    public void clear() {
        head = 0;
    }

    public WordStack() {
        this(DEFAULT_SIZE);
    }

    public void push(int i) {
        if (head == stack.length) {
            resize();
        }
        stack[head++] = i;
    }

    public int pop() {
        if (head == 0) {
            throw new NoSuchElementException();
        }
        return stack[--head];
    }

    public void push(float f) {
        push(Float.floatToIntBits(f));
    }

    public float popFloat() {
        return Float.intBitsToFloat(pop());
    }

    public void push(long l) {
        if (head >= stack.length - 1) {
            resize();
        }
        stack[head++] = (int) (l >> 32);
        stack[head++] = (int) l;
    }

    public long popLong() {
        if (head <= 1) {
            throw new NoSuchElementException();
        }
        long val = Utils.toLong(stack[head - 2], stack[head - 1]);
        head -= 2;
        return val;
    }

    public void push(double d) {
        push(Double.doubleToLongBits(d));
    }

    public double popDouble() {
        return Double.longBitsToDouble(popLong());
    }

    public int peek() {
        if (head == 0) {
            throw new NoSuchElementException();
        }
        return stack[head - 1];
    }

    private void resize() {
        int[] newStack = new int[stack.length * 2];
        System.arraycopy(stack, 0, newStack, 0, stack.length);
        stack = newStack;
    }

    @Override
    public void receiveInt(int i) {
        push(i);
    }

    @Override
    public void receiveLong(long l) {
        push(l);
    }
}
