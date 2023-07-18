package dev.gradleplugins.internal.runtime.dsl;

import groovy.lang.Closure;
import org.codehaus.groovy.runtime.MethodClosure;
import org.gradle.api.plugins.ExtensionAware;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

final class DefaultGradleExtensionMixIn<T> implements GradleExtensionMixIn {
    private final Class<T> publicType;
    private final T extension;

    public DefaultGradleExtensionMixIn(Class<T> publicType, T extension) {
        this.publicType = publicType;
        this.extension = extension;
    }

    @Override
    public void mixInto(ExtensionAware instance) {
        // TODO: Throws exception if already mixed into
        assert instance.getExtensions().findByName("$extension_" + publicType.getSimpleName()) == null;

        instance.getExtensions().add(publicType, "$extension_" + publicType.getSimpleName(), extension);

        for (Method method : dslMethods(publicType)) {
            @SuppressWarnings("rawtypes") final Closure body = new MethodClosure(extension, method.getName());
            GroovyHelper.instance().addNewInstanceMethod(instance, method.getName(), body);
        }
        // TODO: Mixin Property type as extension (mount them)
        // TODO: MixIn setter for Property type
    }

    private static List<Method> dslMethods(Class<?> clazz) {
        List<Method> annotatedMethods = new ArrayList<>();
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(DslMethod.class)) {
                annotatedMethods.add(method);
            }
        }
        return annotatedMethods;
    }
}
