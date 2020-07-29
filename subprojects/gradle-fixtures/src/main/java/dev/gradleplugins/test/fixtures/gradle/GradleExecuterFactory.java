package dev.gradleplugins.test.fixtures.gradle;

import dev.gradleplugins.test.fixtures.file.TestFile;
import dev.gradleplugins.test.fixtures.gradle.executer.GradleDistribution;
import dev.gradleplugins.test.fixtures.gradle.executer.GradleDistributionFactory;
import dev.gradleplugins.test.fixtures.gradle.executer.GradleExecuter;
import dev.gradleplugins.test.fixtures.gradle.executer.internal.GradleRunnerExecuter;
import dev.gradleplugins.test.fixtures.gradle.executer.internal.GradleWrapperExecuter;
import dev.gradleplugins.test.fixtures.gradle.executer.internal.OutOfProcessGradleExecuter;
import lombok.RequiredArgsConstructor;

import java.io.File;

@RequiredArgsConstructor
public class GradleExecuterFactory {
    private final File testDirectory;

    public GradleExecuter testKit() {
        return new GradleRunnerExecuter(GradleDistributionFactory.current(), TestFile.of(testDirectory));
    }

    public GradleExecuter testKit(GradleDistribution distribution) {
        return new GradleRunnerExecuter(distribution, TestFile.of(testDirectory));
    }

    @Deprecated
    public GradleExecuter testKit(TestFile testDirectory) {
        return new GradleRunnerExecuter(GradleDistributionFactory.current(), testDirectory);
    }

    public GradleExecuter wrapper(File rootProjectDirectory) {
        return new GradleWrapperExecuter(GradleDistributionFactory.wrapper(rootProjectDirectory), TestFile.of(testDirectory)).inDirectory(rootProjectDirectory);
    }

    @Deprecated
    public GradleExecuter wrapper(TestFile testDirectory) {
        return new GradleWrapperExecuter(GradleDistributionFactory.wrapper(testDirectory), testDirectory).inDirectory(testDirectory);
    }

    public GradleExecuter forking() {
        return new OutOfProcessGradleExecuter(GradleDistributionFactory.current(), TestFile.of(testDirectory));
    }

    public GradleExecuter forking(GradleDistribution distribution) {
        return new OutOfProcessGradleExecuter(distribution, TestFile.of(testDirectory));
    }

//    public GradleExecuter toolingApi(TestFile testDirectory) {
//        return new GradleToolingApiExecuter(testDirectory);
//    }

//    public GradleExecuter forking(TestFile testDirectory) {
//        return new GradleForkingExecuter(testDirectory);
//    }
}
