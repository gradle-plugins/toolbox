package dev.gradleplugins.runnerkit;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

final class ClassUtils {
    @SuppressWarnings("unchecked")
    public static <T> T staticInvoke(String className, String methodName, Class<?>[] parameterTypes, Object... args) {
        try {
            Class<?> clazz = Class.forName(className);
            Method method = clazz.getMethod(methodName, parameterTypes);
            return (T) method.invoke(null, args);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Please verify your dependencies on runnerKit.", e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T newInstance(String className) {
        try {
            Class<?> clazz = Class.forName(className);
            Constructor<T> constructor = (Constructor<T>) clazz.getDeclaredConstructor();
            return (T) constructor.newInstance();
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
            throw new RuntimeException("Please verify your dependencies on runnerKit.", e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T newInstance(String className, Class<?>[] parameterTypes, Object... args) {
        try {
            Class<?> clazz = Class.forName(className);
            Constructor<T> constructor = (Constructor<T>) clazz.getDeclaredConstructor(parameterTypes);
            return (T) constructor.newInstance(args);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
            throw new RuntimeException("Please verify your dependencies on runnerKit.", e);
        }
    }
}
