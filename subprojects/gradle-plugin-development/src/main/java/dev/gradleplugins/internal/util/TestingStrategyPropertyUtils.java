package dev.gradleplugins.internal.util;

import dev.gradleplugins.GradlePluginTestingStrategy;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.provider.Property;
import org.gradle.api.reflect.TypeOf;

public final class TestingStrategyPropertyUtils {
    public static final String TESTING_STRATEGY_EXTENSION_NAME = "testingStrategy";
    public static final TypeOf<Property<GradlePluginTestingStrategy>> TESTING_STRATEGY_PROPERTY_TYPE = new TypeOf<Property<GradlePluginTestingStrategy>>() {};

    @SuppressWarnings("unchecked")
    public static Property<GradlePluginTestingStrategy> testingStrategy(Object target) {
        return (Property<GradlePluginTestingStrategy>) ((ExtensionAware) target).getExtensions().getByName(TESTING_STRATEGY_EXTENSION_NAME);
    }
}
