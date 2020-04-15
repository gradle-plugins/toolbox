package dev.gradleplugins.test.fixtures.gradle;

import dev.gradleplugins.test.fixtures.file.TestFile;
import dev.gradleplugins.test.fixtures.gradle.executer.GradleExecuter;
import dev.gradleplugins.test.fixtures.gradle.executer.internal.GradleRunnerExecuter;
import dev.gradleplugins.test.fixtures.gradle.executer.internal.GradleWrapperExecuter;

public class GradleExecuterFactory {
    public GradleExecuter testKit(TestFile testDirectory) {
        return new GradleRunnerExecuter(null, testDirectory);
    }

    public GradleExecuter wrapper(TestFile testDirectory) {
        return new GradleWrapperExecuter(testDirectory);
    }

//    public GradleExecuter toolingApi(TestFile testDirectory) {
//        return new GradleToolingApiExecuter(testDirectory);
//    }

//    public GradleExecuter forking(TestFile testDirectory) {
//        return new GradleForkingExecuter(testDirectory);
//    }
}
