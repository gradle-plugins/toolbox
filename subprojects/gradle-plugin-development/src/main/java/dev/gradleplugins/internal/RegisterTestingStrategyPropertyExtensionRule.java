package dev.gradleplugins.internal;

import dev.gradleplugins.GradlePluginTestingStrategy;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.reflect.TypeOf;
import org.gradle.api.tasks.testing.Test;

import static dev.gradleplugins.internal.util.TestingStrategyPropertyUtils.TESTING_STRATEGY_EXTENSION_NAME;
import static dev.gradleplugins.internal.util.TestingStrategyPropertyUtils.TESTING_STRATEGY_PROPERTY_TYPE;

public final class RegisterTestingStrategyPropertyExtensionRule implements Action<Test> {
    private final ObjectFactory objects;

    public RegisterTestingStrategyPropertyExtensionRule(ObjectFactory objects) {
        this.objects = objects;
    }

    @Override
    public void execute(Test task) {
        val testingStrategy = objects.property(GradlePluginTestingStrategy.class);
        task.getExtensions().add(TESTING_STRATEGY_PROPERTY_TYPE, TESTING_STRATEGY_EXTENSION_NAME, testingStrategy);
    }
}
