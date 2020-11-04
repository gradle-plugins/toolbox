import dev.gradleplugins.GradlePluginDevelopmentDependencyExtension
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.plugins.ExtensionAware

fun DependencyHandler.gradleApi(version: String): Dependency = ExtensionAware::class.java.cast(this).extensions.getByType(GradlePluginDevelopmentDependencyExtension::class.java).gradleApi(version)

fun DependencyHandler.gradleFixtures(): Dependency = ExtensionAware::class.java.cast(this).extensions.getByType(GradlePluginDevelopmentDependencyExtension::class.java).gradleFixtures()

fun DependencyHandler.gradleRunnerKit(): Dependency = ExtensionAware::class.java.cast(this).extensions.getByType(GradlePluginDevelopmentDependencyExtension::class.java).gradleRunnerKit()

fun DependencyHandler.gradleTestKit(version: String): Dependency = ExtensionAware::class.java.cast(this).extensions.getByType(GradlePluginDevelopmentDependencyExtension::class.java).gradleTestKit(version)