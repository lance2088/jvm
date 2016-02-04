package com.mcintyret.jvm.load;

import com.google.common.collect.Iterables;

import java.util.Iterator;

import static java.util.Arrays.asList;

/**
 * User: tommcintyre
 * Date: 5/21/14
 */
public class AggregatingClassPath implements ClassPath {

    private final Iterable<ClassPath> classPaths;

    public AggregatingClassPath(Iterable<ClassPath> classPaths) {
        this.classPaths = classPaths;
    }

    public AggregatingClassPath(ClassPath... classPaths) {
        this(asList(classPaths));
    }

    @Override
    public Iterator<ClassFileResource> iterator() {
        return Iterables.concat(classPaths).iterator();
    }

    @Override
    public ClassFileResource get(String name) {
        for (ClassPath classPath : classPaths) {
            ClassFileResource cfr = classPath.get(name);
            if (cfr != null) {
                return cfr;
            }
        }
        return null;
    }
}
