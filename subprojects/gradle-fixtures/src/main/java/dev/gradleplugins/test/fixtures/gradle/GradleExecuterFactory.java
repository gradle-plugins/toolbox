package dev.gradleplugins.test.fixtures.gradle;

import dev.gradleplugins.test.fixtures.file.TestFile;
import dev.gradleplugins.test.fixtures.gradle.executer.GradleDistribution;
import dev.gradleplugins.test.fixtures.gradle.executer.GradleDistributionFactory;
import dev.gradleplugins.test.fixtures.gradle.executer.GradleExecuter;
import dev.gradleplugins.test.fixtures.gradle.executer.internal.*;
import lombok.RequiredArgsConstructor;

import java.io.File;

@RequiredArgsConstructor
public class GradleExecuterFactory {
    private final File testDirectory;

    public GradleExecuter testKit() {
        return new GradleRunnerExecuter(GradleDistributionFactory.current(), TestFile.of(testDirectory), TestKitGradleExecuterBuildContext.INSTANCE);
    }

    public GradleExecuter testKit(GradleDistribution distribution) {
        return new GradleRunnerExecuter(distribution, TestFile.of(testDirectory), TestKitGradleExecuterBuildContext.INSTANCE);
    }

    @Deprecated
    public GradleExecuter testKit(TestFile testDirectory) {
        return new GradleRunnerExecuter(GradleDistributionFactory.current(), testDirectory, TestKitGradleExecuterBuildContext.INSTANCE);
    }

    public GradleExecuter wrapper(File rootProjectDirectory) {
        return new GradleWrapperExecuter(GradleDistributionFactory.wrapper(rootProjectDirectory), TestFile.of(testDirectory), DefaultGradleExecuterBuildContext.INSTANCE).inDirectory(rootProjectDirectory);
    }

    @Deprecated
    public GradleExecuter wrapper(TestFile testDirectory) {
        return new GradleWrapperExecuter(GradleDistributionFactory.wrapper(testDirectory), testDirectory, DefaultGradleExecuterBuildContext.INSTANCE).inDirectory(testDirectory);
    }

    public GradleExecuter forking() {
        return new OutOfProcessGradleExecuter(GradleDistributionFactory.current(), TestFile.of(testDirectory), DefaultGradleExecuterBuildContext.INSTANCE);
    }

    public GradleExecuter forking(GradleDistribution distribution) {
        return new OutOfProcessGradleExecuter(distribution, TestFile.of(testDirectory), DefaultGradleExecuterBuildContext.INSTANCE);
    }

//    public GradleExecuter toolingApi(TestFile testDirectory) {
//        return new GradleToolingApiExecuter(testDirectory);
//    }

//    public GradleExecuter forking(TestFile testDirectory) {
//        return new GradleForkingExecuter(testDirectory);
//    }
}
