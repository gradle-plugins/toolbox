/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.gradleplugins.test.fixtures.gradle.executer.internal;

import dev.gradleplugins.test.fixtures.file.TestFile;
import dev.gradleplugins.test.fixtures.gradle.executer.GradleExecuter;

public class CurrentGradleDistribution extends DefaultGradleDistribution {

    public CurrentGradleDistribution() {
//        this(IntegrationTestBuildContext.INSTANCE);
//    }
//
//    public CurrentGradleDistribution(IntegrationTestBuildContext buildContext) {
//        this(buildContext, buildContext.getGradleHomeDir());
//    }
//
//    public CurrentGradleDistribution(IntegrationTestBuildContext buildContext, TestFile gradleHomeDir) {
        super(
//            buildContext.getVersion(),
                null,
//            gradleHomeDir,
                null,
//            buildContext.getDistributionsDir().file(String.format("gradle-%s-bin.zip", buildContext.getVersion().getBaseVersion().getVersion()))
                null
        );
    }

    @Override
    public GradleExecuter executer(TestFile testDirectoryProvider/*, IntegrationTestBuildContext buildContext*/) {
//        return new GradleContextualExecuter(this, testDirectoryProvider, buildContext).withWarningMode(null);
        return null;
    }
}

