package com.mcintyret.jvm.core.domain;

import com.mcintyret.jvm.core.clazz.AbstractClassObject;
import com.mcintyret.jvm.core.clazz.ClassObject;
import com.mcintyret.jvm.load.*;
import com.mcintyret.jvm.load.ClassLoader;

import java.util.HashMap;
import java.util.Map;

public final class NonArrayType extends ReferenceType {

    private static final Map<String, NonArrayType> CACHE = new HashMap<>();

    public static NonArrayType forClass(String className) {
        NonArrayType ret = CACHE.get(className);
        if (ret == null) {
            ret = new NonArrayType(className);
            CACHE.put(className, ret);
        }
        return ret;
    }

    private final String className;

    private NonArrayType(String className) {
        this.className = className;
    }

    public String getClassName() {
        return className;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NonArrayType that = (NonArrayType) o;

        return className.equals(that.className);
    }

    @Override
    public int hashCode() {
        return className.hashCode();
    }

    @Override
    public String toString() {
        return "L" + className + ";";
    }

    @Override
    public ClassObject getClassObject() {
        return ClassLoader.getDefaultClassLoader().getClassObject(className);
    }
}