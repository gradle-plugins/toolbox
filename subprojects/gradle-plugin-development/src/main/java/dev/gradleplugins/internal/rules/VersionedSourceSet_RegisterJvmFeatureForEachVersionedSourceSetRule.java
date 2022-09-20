/*
 * Copyright 2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.gradleplugins.internal.rules;

import dev.gradleplugins.internal.RuleGroup;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.jvm.tasks.Jar;

import static dev.gradleplugins.internal.rules.VersionedSourceSetUtils.gradleVersionClassifier;
import static dev.gradleplugins.internal.util.DomainObjectCollectionUtils.matching;
import static dev.gradleplugins.internal.util.GradlePluginDevelopmentUtils.java;
import static dev.gradleplugins.internal.util.SourceSetUtils.sourceSets;

@RuleGroup(VersionedSourceSetGroup.class)
public final class VersionedSourceSet_RegisterJvmFeatureForEachVersionedSourceSetRule implements Action<Project> {
	@Override
	public void execute(Project project) {
		sourceSets(project, it -> it.configureEach(matching(VersionedSourceSetUtils.isVersionedSourceSet(), sourceSet -> {
			java(project).registerFeature(toFeatureName(sourceSet.getName()), spec -> {
				spec.usingSourceSet(sourceSet);
				spec.capability(project.getGroup().toString(), project.getName() + "-" + gradleVersionClassifier(sourceSet.getName()), project.getVersion().toString());
			});
			project.getTasks().named(sourceSet.getJarTaskName(), Jar.class,
					task -> task.getArchiveClassifier().set(gradleVersionClassifier(sourceSet.getName())));
		})));
	}

	private static String toFeatureName(String version) {
		return version.replace(".", "");
	}
}
