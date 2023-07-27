import dev.gradleplugins.GradlePluginDevelopmentDependencyBucket
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.Provider

operator fun GradlePluginDevelopmentDependencyBucket.invoke(dependencyNotation: CharSequence) = this.add(dependencyNotation)

operator fun GradlePluginDevelopmentDependencyBucket.invoke(dependencyNotation: CharSequence, configureAction: (ExternalModuleDependency) -> Unit) = this.add(dependencyNotation, configureAction)

operator fun GradlePluginDevelopmentDependencyBucket.invoke(project: Project) = this.add(project)

operator fun GradlePluginDevelopmentDependencyBucket.invoke(fileCollection: FileCollection) = this.add(fileCollection)

operator fun GradlePluginDevelopmentDependencyBucket.invoke(dependency: Dependency) = this.add(dependency)

operator fun <DependencyType : Dependency> GradlePluginDevelopmentDependencyBucket.invoke(dependency: DependencyType, configureAction: (DependencyType) -> Unit) = this.add(dependency, configureAction)

operator fun GradlePluginDevelopmentDependencyBucket.invoke(dependencyProvider: Provider<out Dependency>) = this.add(dependencyProvider)

operator fun <DependencyType : Dependency> GradlePluginDevelopmentDependencyBucket.invoke(dependencyProvider: Provider<DependencyType>, configureAction: (DependencyType) -> Unit) = this.add(dependencyProvider, configureAction)