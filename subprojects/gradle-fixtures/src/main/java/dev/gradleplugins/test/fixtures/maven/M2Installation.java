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

package dev.gradleplugins.test.fixtures.maven;

import dev.gradleplugins.runnerkit.GradleRunner;
import dev.gradleplugins.test.fixtures.file.TestFile;

import java.io.File;
import java.util.Collections;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static dev.gradleplugins.fixtures.file.FileSystemUtils.*;
import static org.junit.Assert.assertTrue;

public class M2Installation implements UnaryOperator<GradleRunner> {
    private final File testDirectory;
    private boolean initialized = false;
    private File userHomeDirectory;
    private File userM2Directory;
    private File userSettingsFile;
    private File globalMavenDirectory;
    private File globalSettingsFile;
    private File isolatedMavenRepoForLeakageChecks;
    private boolean isolateMavenLocal = true;

    public M2Installation(File testDirectory) {
        this.testDirectory = testDirectory;
    }

    private void init() {
        if (!initialized) {
            userHomeDirectory = createDirectory(file(testDirectory, "maven_home"));
            userM2Directory = createDirectory(file(userHomeDirectory, ".m2"));
            userSettingsFile = file(userM2Directory, "settings.xml");
            globalMavenDirectory = createDirectory(file(userHomeDirectory, "m2_home"));
            globalSettingsFile = file(globalMavenDirectory, "conf/settings.xml");
            System.out.println("M2 home: " + userHomeDirectory);

            initialized = true;
        }
    }

    public TestFile getUserHomeDir() {
        init();
        return TestFile.of(userHomeDirectory);
    }

    public TestFile getUserM2Directory() {
        init();
        return TestFile.of(userM2Directory);
    }

    public TestFile getUserSettingsFile() {
        init();
        return TestFile.of(userSettingsFile);
    }

    public TestFile getGlobalMavenDirectory() {
        init();
        return TestFile.of(globalMavenDirectory);
    }

    public TestFile getGlobalSettingsFile() {
        init();
        return TestFile.of(globalSettingsFile);
    }

    public MavenLocalRepository mavenRepo() {
        init();
        return new MavenLocalRepository(TestFile.of(file(userM2Directory, "repository")));
    }

    public M2Installation generateUserSettingsFile(MavenLocalRepository userRepository) {
        init();
        TestFile.of(userSettingsFile).write("<settings>\n"
                        + "    <localRepository>${userRepository.rootDir.absolutePath}</localRepository>\n"
                        + "</settings>");
        return this;
    }

    public M2Installation generateGlobalSettingsFile() {
        return generateGlobalSettingsFile(mavenRepo());
    }

    public M2Installation generateGlobalSettingsFile(MavenLocalRepository globalRepository) {
        init();
        TestFile.of(createFile(globalSettingsFile)).write("<settings>\n"
                + "    <localRepository>${globalRepository.rootDir.absolutePath}</localRepository>\n"
                + "</settings>");
        return this;
    }

    @Override
    public GradleRunner apply(GradleRunner gradleExecuter) {
        init();
        GradleRunner result = gradleExecuter.withUserHomeDirectory(userHomeDirectory);
        // if call `using m2`, then we disable the automatic isolation of m2
        isolateMavenLocal = false;
        if (globalMavenDirectory.exists()) {
            result = result.withEnvironmentVariable("M2_HOME", globalMavenDirectory.getAbsolutePath());
        }
        return result;
    }

    public GradleRunner isolateMavenLocalRepo(GradleRunner gradleExecuter) {
        gradleExecuter = gradleExecuter.beforeExecute(executer -> {
            if (isolateMavenLocal) {
                isolatedMavenRepoForLeakageChecks = createDirectory(file(executer.getWorkingDirectory(), "m2-home-should-not-be-filled"));
                return setMavenLocalLocation(executer, isolatedMavenRepoForLeakageChecks);
            }
            return executer;
        });
        gradleExecuter = gradleExecuter.afterExecute(executer -> {
            if (isolateMavenLocal) {
                assertTrue(String.format("%s is not an empty directory.", isolatedMavenRepoForLeakageChecks), isEmptyDirectory(isolatedMavenRepoForLeakageChecks));
            }
        });

        return gradleExecuter;
    }

    private static GradleRunner setMavenLocalLocation(GradleRunner runner, File destination) {
        return runner.withArgument("-Dmaven.repo.local=" + destination.getAbsolutePath());
    }
}

