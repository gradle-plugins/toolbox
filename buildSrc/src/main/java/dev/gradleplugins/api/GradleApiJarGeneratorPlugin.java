package dev.gradleplugins.api;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.gradle.api.JavaVersion;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.attributes.*;
import org.gradle.api.attributes.java.TargetJvmVersion;
import org.gradle.api.component.AdhocComponentWithVariants;
import org.gradle.api.component.SoftwareComponentFactory;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.resources.TextResourceFactory;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.util.GradleVersion;
import org.gradle.util.VersionNumber;

import javax.inject.Inject;
import java.io.UncheckedIOException;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class GradleApiJarGeneratorPlugin implements Plugin<Project> {
    private final SoftwareComponentFactory softwareComponentFactory;

    @Inject
    public GradleApiJarGeneratorPlugin(SoftwareComponentFactory softwareComponentFactory) {
        this.softwareComponentFactory = softwareComponentFactory;
    }

    @Override
    public void apply(Project project) {
        ConfigurationContainer configurations = project.getConfigurations();
        ObjectFactory objects = project.getObjects();
        TextResourceFactory textResourceFactory = project.getResources().getText();
        TaskContainer tasks = project.getTasks();
        ProjectLayout layout = project.getLayout();
        DependencyHandler dependencies = project.getDependencies();

        configurations.create("gradleApiElements", configuration -> {
            configuration.setCanBeResolved(false);
            configuration.setCanBeConsumed(true);
            configuration.getAttributes().attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.class, Usage.JAVA_API));
        });

        allGeneralAvailableVersion(textResourceFactory).forEach(availableVersion -> {
            TaskProvider<GenerateGradleApiJar> generateGradleApiJarTask = tasks.register("generateGradleApi" + availableVersion.getVersion(), GenerateGradleApiJar.class, task -> {
                task.getVersion().set(availableVersion.getVersion());
                task.getOutputFile().set(layout.getBuildDirectory().file("generated-gradle-jars/gradle-api-" + availableVersion.getVersion() + ".jar"));
            });

            Configuration api = configurations.create("apiForGradleApi" + availableVersion.getVersion(), configuration -> {
                configuration.setCanBeConsumed(false);
                configuration.setCanBeResolved(false);
            });

            Configuration gradleApiElements = configurations.create("gradleApi" + availableVersion.getVersion() + "Elements", configuration -> {
                configuration.setCanBeResolved(false);
                configuration.setCanBeConsumed(true);
                configuration.extendsFrom(api);
                configuration.getOutgoing().artifact(generateGradleApiJarTask.flatMap(it -> it.getOutputFile()));
                configuration.attributes(attributes -> {
                    attributes.attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.class, Usage.JAVA_API));
                    attributes.attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.class, Category.LIBRARY));
                    attributes.attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.class, Bundling.EXTERNAL));
                    attributes.attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements.class, LibraryElements.JAR));
                    if (availableVersion.compareTo(GradleVersion.version("3.0")) < 0) {
                        attributes.attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, Integer.parseInt(JavaVersion.VERSION_1_6.getMajorVersion()));
                    } else {
                        attributes.attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, Integer.parseInt(JavaVersion.VERSION_1_8.getMajorVersion()));
                    }
                    attributes.attribute(Attribute.of("dev.gradleplugins.gradleAbi", String.class), availableVersion.getVersion());
                });
            });

            dependencies.add(api.getName(), "org.codehaus.groovy:groovy-all:" + toGroovyVersion(availableVersion));

            AdhocComponentWithVariants adhocComponent = softwareComponentFactory.adhoc("gradleApi" + availableVersion.getVersion());
            // add it to the list of components that this project declares
            project.getComponents().add(adhocComponent);
            // and register a variant for publication
            adhocComponent.addVariantsFromConfiguration(gradleApiElements, it -> {});


            project.getPluginManager().withPlugin("maven-publish", appliedPlugin -> {
                project.getExtensions().configure(PublishingExtension.class, publishing -> {
                    publishing.getPublications().create("gradleApi" + availableVersion.getVersion(), MavenPublication.class, publication -> {
                        publication.from(adhocComponent);
                        publication.setVersion(availableVersion.getVersion());
                        publication.pom(pom -> {
                            pom.getName().set("Gradle " + availableVersion.getVersion() + " API");
                            pom.getDescription().set(project.provider(() -> project.getDescription()));
                            pom.developers(developers -> {
                                developers.developer(developer -> {
                                    developer.getId().set("gradle");
                                    developer.getName().set("Gradle Inc.");
                                    developer.getUrl().set("https://github.com/gradle");
                                });
                            });
                        });
                    });
                });
            });
        });


        AdhocComponentWithVariants adhocComponent = softwareComponentFactory.adhoc("gradleApi");
        // add it to the list of components that this project declares
        project.getComponents().add(adhocComponent);
        // and register a variant for publication
        adhocComponent.addVariantsFromConfiguration(project.getConfigurations().getByName("gradleApiElements"), it -> {});

    }

    Set<GradleVersion> allGeneralAvailableVersion(TextResourceFactory textResourceFactory) {
        try {
            String jsonText = textResourceFactory.fromUri(new URL("https://services.gradle.org/versions/all")).asString();
            Type type = new TypeToken<List<VersionDownloadInfo>>() {}.getType();
            List<VersionDownloadInfo> versionInfo = new Gson().fromJson(jsonText, type);
            Set<GradleVersion> result = versionInfo
                    .stream()
                    .filter(notSnapshot().and(notReleaseCandidate()).and(notMilestone()).and(greaterOrEqualTo("5.5.1")))
                    .map(toGradleVersion()).collect(Collectors.toSet());
            result.add(GradleVersion.version("3.5.1"));
            result.add(GradleVersion.version("2.14.1"));
            return result;
        } catch (MalformedURLException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static Predicate<VersionDownloadInfo> notSnapshot() {
        return it -> !it.snapshot;
    }

    private static Predicate<VersionDownloadInfo> notReleaseCandidate() {
        return it -> !it.version.contains("-rc-");
    }

    private static Predicate<VersionDownloadInfo> notMilestone() {
        return it -> !it.version.contains("-milestone-");
    }

    private static Predicate<VersionDownloadInfo> greaterOrEqualTo(String versionString) {
        return it -> VersionNumber.parse(versionString).compareTo(VersionNumber.parse(it.version)) <= 0;
    }

    private static Function<VersionDownloadInfo, GradleVersion> toGradleVersion() {
        return it -> GradleVersion.version(it.version);
    }

    private static class VersionDownloadInfo {
        String version;
        boolean snapshot;
    }

    private static String toGroovyVersion(GradleVersion version) {
        // Use `find ~/.gradle/wrapper -name "groovy-all-*"`
        switch (version.getVersion()) {
            case "2.14":
            case "2.14.1":
                return "2.4.4";
            case "3.5.1":
                return "2.4.10";
            case "5.4.1":
            case "5.5.1":
                return "2.5.4"; //"org.gradle.groovy:groovy-all:1.0-2.5.4";
            case "5.6":
            case "5.6.1":
            case "5.6.2":
            case "5.6.3":
            case "5.6.4":
                return "2.5.4"; //"org.gradle.groovy:groovy-all:1.3-2.5.4";
            case "6.0":
            case "6.0.1":
            case "6.1":
            case "6.1.1":
            case "6.2":
            case "6.2.1":
            case "6.2.2":
                return "2.5.8"; //"org.gradle.groovy:groovy-all:1.3-2.5.8";
            case "6.3":
                return "2.5.10"; //"org.gradle.groovy:groovy-all:1.3-2.5.10";
            default:
                throw new IllegalArgumentException("Version not known at the time, please check groovy-all version");
        }
    }
}
