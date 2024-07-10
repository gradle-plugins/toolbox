package dev.gradleplugins.internal.runtime.dsl;

import groovy.lang.Closure;

public abstract class GroovyHelper {
    private static final Object lock = new Object();
    private static GroovyHelper INSTANCE;

    private static GroovyHelper newInstance() {
        try {
            return (GroovyHelper) Class.forName("dev.gradleplugins.internal.dsl.groovy.GroovyDslRuntimeExtensions").newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static GroovyHelper instance() {
        if (INSTANCE == null) {
            synchronized (lock) {
                if (INSTANCE == null) {
                    INSTANCE = newInstance();
                }
            }
        }
        return INSTANCE;
    }

    public abstract void addNewInstanceMethod(Object self, String methodName, @SuppressWarnings("rawtypes") Closure methodBody);

//    public abstract void mixin(Class type, String methodName, Closure methodBody);
}
