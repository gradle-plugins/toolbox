package dev.gradleplugins.internal;

import dev.gradleplugins.GradlePluginDevelopmentTestSuite;
import org.gradle.api.Action;
import org.gradle.api.tasks.SourceSet;

public final class TestSuiteSourceSetExtendsFromTestedSourceSetIfPresentRule implements Action<GradlePluginDevelopmentTestSuite> {
    @Override
    public void execute(GradlePluginDevelopmentTestSuite testSuite) {
        testSuite.getTestedSourceSet().disallowChanges();
        if (testSuite.getTestedSourceSet().isPresent()) {
            SourceSet sourceSet = testSuite.getSourceSet().get();
            SourceSet testedSourceSet = testSuite.getTestedSourceSet().get();
            sourceSet.setCompileClasspath(sourceSet.getCompileClasspath().plus(testedSourceSet.getOutput()));
            sourceSet.setRuntimeClasspath(sourceSet.getRuntimeClasspath().plus(sourceSet.getOutput()).plus(sourceSet.getCompileClasspath()));
        }
    }
}
