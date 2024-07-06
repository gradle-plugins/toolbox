import dev.gradleplugins.GradlePluginDevelopmentCompatibilityExtension
import org.gradle.api.Action
import org.gradle.api.plugins.ExtensionAware
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension
import dev.gradleplugins.internal.util.Configurable

@Suppress("UNCHECKED_CAST")
fun GradlePluginDevelopmentExtension.compatibility(action: Action<in GradlePluginDevelopmentCompatibilityExtension>) = (ExtensionAware::class.java.cast(this).extensions.getByName("compatibility") as Configurable<GradlePluginDevelopmentCompatibilityExtension>).configure(action)