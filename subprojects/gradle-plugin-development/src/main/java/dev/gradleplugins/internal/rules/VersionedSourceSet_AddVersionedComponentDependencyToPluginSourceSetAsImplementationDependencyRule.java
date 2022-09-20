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
import org.gradle.api.Transformer;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.tasks.SourceSet;

import java.util.Collection;
import java.util.stream.Collectors;

import static dev.gradleplugins.internal.rules.VersionedSourceSetUtils.asDependency;
import static dev.gradleplugins.internal.rules.VersionedSourceSetUtils.isVersionedSourceSet;
import static dev.gradleplugins.internal.util.GradlePluginDevelopmentUtils.gradlePlugin;
import static dev.gradleplugins.internal.util.ProviderUtils.transformEach;
import static dev.gradleplugins.internal.util.SourceSetUtils.sourceSets;

@RuleGroup(VersionedSourceSetGroup.class)
// Finalizer rule because rely on pluginSourceSet which may or may not be `main`
public final class VersionedSourceSet_AddVersionedComponentDependencyToPluginSourceSetAsImplementationDependencyRule implements Action<Project> {
	@Override
	public void execute(Project project) {
		project.getConfigurations().named(gradlePlugin(project).getPluginSourceSet().getImplementationConfigurationName(), configuration -> {
			configuration.getDependencies().addAllLater(project.getObjects().setProperty(Dependency.class)
					.value(sourceSets(project).map(onlyVersionedSourceSet())
							.map(transformEach(SourceSet::getName))
							.map(transformEach(VersionedSourceSetUtils::gradleVersionClassifier))
							.map(transformEach(asDependency(project)))));
		});
	}

	private static Transformer<Collection<SourceSet>, Collection<SourceSet>> onlyVersionedSourceSet() {
		return sourceSets -> sourceSets.stream().filter(isVersionedSourceSet()::isSatisfiedBy).collect(Collectors.toList());
	}
}
