package com.mcintyret.jvm.core.clazz;

import com.mcintyret.jvm.core.Field;
import com.mcintyret.jvm.core.Method;
import com.mcintyret.jvm.core.constantpool.ConstantPool;
import com.mcintyret.jvm.core.domain.ReferenceType;
import com.mcintyret.jvm.core.oop.OopClass;
import com.mcintyret.jvm.parse.Modifier;
import java.util.Set;

public class ClassObject extends AbstractClassObject {

    private final ReferenceType type;

    private final ConstantPool constantPool;

    private final Method[] instanceMethods;

    private final Method[] staticMethods;

    private final Field[] instanceFields;

    private final Field[] staticFields;

    private final int[] staticFieldValues;

    public ClassObject(ReferenceType type, Set<Modifier> modifiers, ClassObject parent, ClassObject[] interfaces,
                       ConstantPool constantPool, Method[] instanceMethods, Method[] staticMethods,
                       Field[] instanceFields, Field[] staticFields) {
        super(parent, interfaces, modifiers);
        this.type = type;
        this.constantPool = constantPool;
        this.instanceMethods = instanceMethods;
        this.staticMethods = staticMethods;
        this.instanceFields = instanceFields;
        this.staticFields = staticFields;
        this.staticFieldValues = newFieldsValuesArray(staticFields);
    }

    // For invokevirtual and invokespecial
    public Method getInstanceMethod(int i) {
        ClassObject co = this;
        while (true) {
            Method method = co.instanceMethods[i];
            if (method != null) {
                return method;
            }
            co = co.parent;
        }
    }

    public Method[] getInstanceMethods() {
        return instanceMethods;
    }

    // For invokestatic
    public Method getStaticMethod(int i) {
        return staticMethods[i];
    }

    public ConstantPool getConstantPool() {
        return constantPool;
    }

    public Field[] getInstanceFields() {
        return instanceFields;
    }

    public int[] getStaticFieldValues() {
        return staticFieldValues;
    }

    public Field[] getStaticFields() {
        return staticFields;
    }

    public OopClass newObject() {
        return new OopClass(this, null, newFieldsValuesArray(instanceFields));
    }

    private static int[] newFieldsValuesArray(Field[] fields) {
        if (fields.length == 0) {
            return new int[0];
        }
        Field lastField = fields[fields.length - 1];
        int size = lastField.getOffset() + lastField.getType().getSimpleType().getWidth();
        return new int[size];
    }

    @Override
    public ReferenceType getType() {
        return type;
    }

    public Method[] getStaticMethods() {
        return staticMethods;
    }


}