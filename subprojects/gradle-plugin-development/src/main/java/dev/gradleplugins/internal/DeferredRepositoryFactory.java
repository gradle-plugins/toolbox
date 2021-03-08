package dev.gradleplugins.internal;

import dev.gradleplugins.GroovyGradlePluginDevelopmentExtension;
import dev.gradleplugins.JavaGradlePluginDevelopmentExtension;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.artifacts.repositories.MavenRepositoryContentDescriptor;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension;

import javax.inject.Inject;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Create deferred repositories on the project.
 *
 * We defer the repository creation until after the project is evaluated because we give a chance to the users to add their own repository first.
 * We also try to limit the resolution content as much as possible to avoid users from relying on the internal implementation of the plugins.
 */
public abstract class DeferredRepositoryFactory {
    private final Project project;

    @Inject
    public DeferredRepositoryFactory(Project project) {
        this.project = project;
    }

    public void groovy() {
        project.afterEvaluate(DeferredRepositoryFactory::createGroovyRepository);
    }

    public void spock() {
        project.afterEvaluate(DeferredRepositoryFactory::createSpockRepository);
    }

    private static void mutateRepositories(Project project, Action<? super RepositoryHandler> action) {
        val extension = ((ExtensionAware) project.getExtensions().getByType(GradlePluginDevelopmentExtension.class));
        val defaultRepositoriesDisabled = ((GradlePluginDevelopmentExtensionInternal) Optional.<Object>ofNullable(extension.getExtensions().findByType(JavaGradlePluginDevelopmentExtension.class)).orElseGet(() -> extension.getExtensions().findByType(GroovyGradlePluginDevelopmentExtension.class))).isDefaultRepositoriesDisabled();
        if (!defaultRepositoriesDisabled) {
            action.execute(project.getRepositories());
        }
    }

    private static void createGroovyRepository(Project project) {
        mutateRepositories(project, repositories -> {
            repositories.withType(MavenArtifactRepository.class)
                    .getByName("Gradle Plugin Development")
                    .mavenContent(allowGroovy()::accept);
        });
    }

    private static void createSpockRepository(Project project) {
        mutateRepositories(project, repositories -> {
            repositories.withType(MavenArtifactRepository.class)
                    .getByName("Gradle Plugin Development")
                    .mavenContent(allowSpock().andThen(allowGroovy())::accept);
        });
    }

    public static Consumer<MavenRepositoryContentDescriptor> allowSpock() {
        return content -> {
            content.includeModule("org.spockframework", "spock-bom");
            content.includeModule("org.spockframework", "spock-core");
            content.includeModule("junit", "junit"); // Required by spock
            content.includeModule("org.hamcrest", "hamcrest-core"); // Required by junit
            content.includeModule("org.hamcrest", "hamcrest-parent");
        };
    }

    private static Consumer<MavenRepositoryContentDescriptor> allowGroovy() {
        return content -> {
            content.includeGroup("org.codehaus.groovy");
            content.includeModule("junit", "junit"); // Required by groovy-test
            content.includeModule("org.hamcrest", "hamcrest-core"); // Required by junit
            content.includeModule("jline", "jline"); // Required by groovy-groovysh
            content.includeModule("com.thoughtworks.qdox", "qdox"); // Required by groovy-docgenerator
            content.includeModule("commons-cli", "commons-cli"); // Required by groovy-cli-commons
            content.includeModule("info.picocli", "picocli"); // Required by groovy-cli-picocli
            content.includeModule("org.apache.ant", "ant"); // Required by groovy-ant

            // Transitive dependencies
            content.includeModule("org.apache.ant", "ant-parent");
            content.includeModule("org.apache.ant", "ant-antlr");
            content.includeModule("org.apache.ant", "ant-junit");
            content.includeModule("org.apache.ant", "ant-launcher");
            content.includeModule("org.apache.commons", "commons-parent");
            content.includeModule("org.codehaus", "codehaus-parent");
            content.includeModule("org.sonatype.oss", "oss-parent");
            content.includeModule("org.hamcrest", "hamcrest-parent");
            content.includeModule("org.apache", "apache");
            content.includeModule("org.apiguardian", "apiguardian-api");
            content.includeModule("org.junit.platform", "junit-platform-launcher");
            content.includeModule("org.junit.platform", "junit-platform-engine");
            content.includeModule("org.opentest4j", "opentest4j");
            content.includeModule("org.junit.platform", "junit-platform-commons");

            // Groovy 2.5.8
            content.includeModule("org.testng", "testng");
            content.includeModule("com.beust", "jcommander");
            content.includeModule("org.junit.jupiter", "junit-jupiter-api");
            content.includeModule("org.junit.jupiter", "junit-jupiter-engine");
        };
    }
}
