package com.mcintyret.jvm.core.domain;

import com.mcintyret.jvm.core.Heap;
import com.mcintyret.jvm.core.clazz.AbstractClassObject;
import com.mcintyret.jvm.core.oop.OopClassClass;

/**
 * User: tommcintyre
 * Date: 5/25/14
 */
public abstract class ReferenceType implements Type {

    @Override
    public SimpleType getSimpleType() {
        return SimpleType.REF;
    }

    @Override
    public boolean isPrimitive() {
        return false;
    }

    public abstract AbstractClassObject getClassObject();

    private OopClassClass oopClassClass;

    @Override
    public OopClassClass getClassOop() {
        return oopClassClass == null ? (oopClassClass = Heap.allocateAndGet(CLASS_CLASS.newObject((clazz, fields) ->
                new OopClassClass(clazz, fields, getClassObject())))) : oopClassClass;
    }

}
