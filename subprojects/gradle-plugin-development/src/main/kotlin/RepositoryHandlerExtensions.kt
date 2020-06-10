import dev.gradleplugins.GradlePluginDevelopmentRepositoryExtension
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.plugins.ExtensionAware

fun RepositoryHandler.gradlePluginDevelopment(): MavenArtifactRepository {
    return ExtensionAware::class.java.cast(this).extensions.getByType(GradlePluginDevelopmentRepositoryExtension::class.java).gradlePluginDevelopment()
}
