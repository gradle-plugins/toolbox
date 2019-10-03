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

package dev.gradleplugins.internal;

import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.util.VersionNumber;

public class DummyPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        String javaVersion = System.getProperty("java.version");
        String[] versionTokens = javaVersion.split("\\.");  // String#split is Java 1.4 but Gradle always only worked for 1.5+
        int majorVersionValue = Integer.parseInt(versionTokens[0]);
        if (majorVersionValue == 1) {
            majorVersionValue = Integer.parseInt(versionTokens[1]);
        }
        if (majorVersionValue < getMinimumSupportedJavaVersion()) {
            throw new GradleException("Plugin '" + getPluginId() + "' does not support the current JVM (" + javaVersion + "). Please use at least JVM 8.");
        }
        if (VersionNumber.parse(project.getGradle().getGradleVersion()).compareTo(VersionNumber.parse(getMinimumSupportedGradleVersion())) < 0) {
            throw new GradleException("Plugin '" + getPluginId() + "' does not support the current version of Gradle (" + project.getGradle().getGradleVersion() + "). Please use at least Gradle " + getMinimumSupportedGradleVersion() + ".");
        }
        try {
            project.getPluginManager().apply(Class.forName(getPluginClass()));
        } catch (ClassNotFoundException e) {
            throw new GradleException("Could not find class", e);
        }
    }

    public String getPluginId() {
        return "<plugin-id>";
    }

    public String getMinimumSupportedGradleVersion() {
        return "<minimum-supported-gradle-version>";
    }

    public int getMinimumSupportedJavaVersion() {
        return Integer.parseInt("<minimum-supported-java-version>");
    }

    public String getPluginClass() {
        return "<plugin-class>";
    }
}
