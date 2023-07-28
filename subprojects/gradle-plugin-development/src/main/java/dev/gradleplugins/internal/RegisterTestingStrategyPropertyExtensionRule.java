package dev.gradleplugins.internal;

import dev.gradleplugins.GradlePluginTestingStrategy;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.invocation.Gradle;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.reflect.TypeOf;
import org.gradle.api.tasks.testing.Test;

public final class RegisterTestingStrategyPropertyExtensionRule implements Action<Test> {
    /** @see GradlePluginTestingStrategy#testingStrategy(Test) Synchronize constant */
    private static final String TESTING_STRATEGY_EXTENSION_NAME = "testingStrategy";
    private static final TypeOf<Property<GradlePluginTestingStrategy>> TESTING_STRATEGY_PROPERTY_TYPE = new TypeOf<Property<GradlePluginTestingStrategy>>() {};
    private final ObjectFactory objects;

    public RegisterTestingStrategyPropertyExtensionRule(ObjectFactory objects) {
        this.objects = objects;
    }

    @Override
    public void execute(Test task) {
        val testingStrategy = objects.property(GradlePluginTestingStrategy.class);
        task.getExtensions().add(TESTING_STRATEGY_PROPERTY_TYPE, TESTING_STRATEGY_EXTENSION_NAME, testingStrategy);
    }

    @SuppressWarnings("unchecked")
    public static Property<GradlePluginTestingStrategy> testingStrategyProperty(Test task) {
        return (Property<GradlePluginTestingStrategy>) task.getExtensions().getByName(TESTING_STRATEGY_EXTENSION_NAME);
    }
}
