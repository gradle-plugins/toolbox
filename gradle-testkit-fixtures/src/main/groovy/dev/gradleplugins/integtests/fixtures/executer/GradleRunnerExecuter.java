/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.gradleplugins.integtests.fixtures.executer;

import dev.gradleplugins.test.fixtures.file.TestDirectoryProvider;
import dev.gradleplugins.test.fixtures.file.TestFile;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GradleRunnerExecuter implements GradleExecuter {
    private final List<String> tasks = new ArrayList<>();
    private final List<String> arguments = new ArrayList<>();
    private final TestDirectoryProvider testDirectoryProvider;
    private boolean debuggerAttached = false;
    private boolean showStacktrace = true;
    private File workingDirectory = null;
    private File settingsFile = null;

    public GradleRunnerExecuter(TestDirectoryProvider testDirectoryProvider) {
        this.testDirectoryProvider = testDirectoryProvider;
    }

    private File getWorkingDirectory() {
        return workingDirectory == null ? getTestDirectoryProvider().getTestDirectory() : workingDirectory;
    }

    @Override
    public TestDirectoryProvider getTestDirectoryProvider() {
        return testDirectoryProvider;
    }

    @Override
    public GradleExecuter inDirectory(File directory) {
        workingDirectory = directory;
        return this;
    }

    @Override
    public GradleExecuter usingSettingsFile(File settingsFile) {
        this.settingsFile = settingsFile;
        return this;
    }

    @Override
    public GradleExecuter withDebuggerAttached() {
        debuggerAttached = true;
        return this;
    }

    @Override
    public GradleExecuter withTasks(List<String> tasks) {
        this.tasks.addAll(tasks);
        return this;
    }

    @Override
    public GradleExecuter withArguments(String... args) {
        return withArguments(Arrays.asList(args));
    }

    @Override
    public GradleExecuter withArguments(List<String> args) {
        arguments.clear();
        arguments.addAll(args);
        return this;
    }

    @Override
    public GradleExecuter withArgument(String arg) {
        arguments.add(arg);
        return this;
    }

    @Override
    public GradleExecuter withBuildCacheEnabled() {
        return withArgument("--build-cache");
    }

    @Override
    public GradleExecuter withStacktraceDisabled() {
        showStacktrace = false;
        return this;
    }

    @Override
    public BuildResult run() {
        try {
            return configureExecuter().build();
        } finally {
            finished();
        }
    }

    @Override
    public BuildResult runWithFailure() {
        try {
            return configureExecuter().buildAndFail();
        } finally {
            finished();
        }
    }

    private void finished() {
        reset();
    }

    private void reset() {
        tasks.clear();
        arguments.clear();
        debuggerAttached = false;
        showStacktrace = true;
        workingDirectory = null;
        settingsFile = null;
    }

    private GradleRunner configureExecuter() {
        GradleRunner runner = GradleRunner.create().forwardOutput().withPluginClasspath();

        if (workingDirectory != null) {
            runner.withProjectDir(workingDirectory);
        }

        if (debuggerAttached) {
            System.out.println("WARNING: Gradle TestKit has some class loader issue that may result in runtime failures - such as NoClassDefFoundError for groovy/util/AntBuilder (see https://github.com/gradle/gradle/issues/1687).");
            runner.withDebug(true);
        }

        List<String> allArguments = new ArrayList<>();
        if (settingsFile != null) {
            allArguments.add("--settings-file");
            allArguments.add(settingsFile.getAbsolutePath());
        }

        if (showStacktrace) {
            allArguments.add("--stacktrace");
        }
        allArguments.addAll(arguments);
        allArguments.addAll(tasks);

        if (settingsFile == null) {
            ensureSettingsFileAvailable();
        }

        runner.withArguments(allArguments);

        return runner;
    }

    private void ensureSettingsFileAvailable() {
        TestFile workingDirectory = new TestFile(getWorkingDirectory());
        TestFile dir = workingDirectory;
        while (dir != null && getTestDirectoryProvider().getTestDirectory().isSelfOrDescendent(dir)) {
            if (hasSettingsFile(dir)) {
                return;
            }
            dir = dir.getParentFile();
        }
        workingDirectory.createFile("settings.gradle");
    }

    private boolean hasSettingsFile(TestFile dir) {
        if (dir.isDirectory()) {
            return dir.file("settings.gradle").isFile() || dir.file("settings.gradle.kts").isFile();
        }
        return false;
    }
}
