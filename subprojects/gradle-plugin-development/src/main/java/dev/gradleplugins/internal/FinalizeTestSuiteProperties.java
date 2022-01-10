package dev.gradleplugins.internal;

import dev.gradleplugins.GradlePluginDevelopmentTestSuite;
import org.gradle.api.Action;

@SuppressWarnings("UnstableApiUsage")
public final class FinalizeTestSuiteProperties implements Action<GradlePluginDevelopmentTestSuite> {
    @Override
    public void execute(GradlePluginDevelopmentTestSuite testSuite) {
        testSuite.getTestedSourceSet().disallowChanges();
        testSuite.getSourceSet().disallowChanges();
        testSuite.getTestingStrategies().disallowChanges();
    }
}
