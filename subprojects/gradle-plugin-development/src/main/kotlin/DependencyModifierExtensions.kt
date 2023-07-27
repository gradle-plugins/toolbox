import dev.gradleplugins.GradlePluginDevelopmentDependencyModifiers.DependencyModifier
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.artifacts.ProjectDependency

operator fun <DependencyType : ModuleDependency> DependencyModifier.invoke(dependency: DependencyType) : ModuleDependency = this.modify(dependency)
operator fun DependencyModifier.invoke(dependencyNotation: String) : ExternalModuleDependency = this.modify(dependencyNotation)
operator fun DependencyModifier.invoke(project: Project) : ProjectDependency = this.modify(project)

// for convenience
operator fun DependencyModifier.invoke(dependency: Dependency) : ModuleDependency = this.modify(dependency as ModuleDependency)