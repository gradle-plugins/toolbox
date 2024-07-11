package dev.gradleplugins.internal.runtime.dsl;

import groovy.lang.Closure;
import groovy.lang.GroovyObject;
import org.codehaus.groovy.runtime.HandleMetaClass;

public final class GroovyHelper {
    private static final Object lock = new Object();
    private static GroovyHelper INSTANCE;

    private static GroovyHelper newInstance() {
        return new GroovyHelper();
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

    public void addNewInstanceMethod(Object self, String methodName, @SuppressWarnings("rawtypes") Closure methodBody) {
        new HandleMetaClass(((GroovyObject) self).getMetaClass(), self).setProperty(methodName, methodBody);
    }

//    public abstract void mixin(Class type, String methodName, Closure methodBody);
}
