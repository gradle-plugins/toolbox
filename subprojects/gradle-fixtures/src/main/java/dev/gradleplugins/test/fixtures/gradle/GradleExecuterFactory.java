package dev.gradleplugins.test.fixtures.gradle;

import dev.gradleplugins.test.fixtures.gradle.executer.GradleExecuter;
import dev.gradleplugins.test.fixtures.gradle.executer.GradleRunnerExecuter;
import dev.gradleplugins.test.fixtures.file.TestFile;

public class GradleExecuterFactory {
    public GradleExecuter testKit(TestFile testDirectory) {
        return new GradleRunnerExecuter(testDirectory);
    }

//    public GradleExecuter toolingApi(TestFile testDirectory) {
//        return new GradleToolingApiExecuter(testDirectory);
//    }

//    public GradleExecuter forking(TestFile testDirectory) {
//        return new GradleForkingExecuter(testDirectory);
//    }
}
