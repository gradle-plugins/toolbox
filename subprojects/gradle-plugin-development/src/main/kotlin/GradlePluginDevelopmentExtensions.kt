import dev.gradleplugins.GradlePluginDevelopmentApiExtension
import dev.gradleplugins.GradlePluginDevelopmentCompatibilityExtension
import dev.gradleplugins.internal.util.Configurable
import org.gradle.api.Action
import org.gradle.api.plugins.ExtensionAware
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension

@Suppress("UNCHECKED_CAST")
fun GradlePluginDevelopmentExtension.compatibility(action: Action<in GradlePluginDevelopmentCompatibilityExtension>) = (ExtensionAware::class.java.cast(this).extensions.getByName("compatibility") as Configurable<GradlePluginDevelopmentCompatibilityExtension>).configure(action)

@Suppress("UNCHECKED_CAST")
fun GradlePluginDevelopmentExtension.api(action: Action<in GradlePluginDevelopmentApiExtension>) = (ExtensionAware::class.java.cast(this).extensions.getByName("api") as Configurable<GradlePluginDevelopmentApiExtension>).configure(action)