package dev.gradleplugins.internal;

import org.gradle.api.Project;
import org.gradle.api.artifacts.repositories.MavenRepositoryContentDescriptor;

import javax.inject.Inject;
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

    public void gradleFixtures() {
        project.afterEvaluate(DeferredRepositoryFactory::createGradleFixturesRepository);
    }

    public void gradleApi() {
        project.afterEvaluate(DeferredRepositoryFactory::createGradleApiRepository);
        project.afterEvaluate(DeferredRepositoryFactory::createGroovyForGradleApiRepository);
    }

    public static void createGradleApiRepository(Project project) {
        project.getRepositories().maven(repository -> {
            repository.setName("Gradle Plugin Development - Gradle APIs");
            repository.setUrl(project.uri("https://dl.bintray.com/gradle-plugins/distributions"));
            repository.mavenContent(content -> {
                content.includeModule("dev.gradleplugins", "gradle-api");
            });
        });
    }

    private static void createGroovyForGradleApiRepository(Project project) {
        project.getRepositories().mavenCentral(repo -> {
            repo.setName("Gradle Plugin Development - Gradle APIs (Groovy)");
            repo.mavenContent(content -> content.includeModule("org.codehaus.groovy", "groovy"));
        });
    }

    public static void createGradleFixturesRepository(Project project) {
        project.getRepositories().maven(repository -> {
            repository.setName("Gradle Plugins Development - Gradle Fixtures");
            repository.setUrl(project.uri("https://dl.bintray.com/gradle-plugins/distributions"));
            repository.mavenContent(content -> content.includeModule("dev.gradleplugins", "gradle-fixtures"));
        });
    }

    private static void createGroovyRepository(Project project) {
        project.getRepositories().mavenCentral(repo -> {
            repo.setName("Gradle Plugin Development - Groovy");
            repo.mavenContent(content -> allowGroovy().accept(content));
        });
    }

    private static void createSpockRepository(Project project) {
        project.getRepositories().mavenCentral(repository -> {
            repository.setName("Gradle Plugin Development - Spock Framework");
            repository.mavenContent(content -> allowSpock().andThen(allowGroovy()).accept(content));
        });
    }

    public static Consumer<MavenRepositoryContentDescriptor> allowSpock() {
        return content -> {
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
        };
    }
}
