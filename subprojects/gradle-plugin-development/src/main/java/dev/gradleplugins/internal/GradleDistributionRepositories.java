package dev.gradleplugins.internal;

import dev.gradleplugins.internal.util.MinimumDependencyResolutionManagement;
import org.gradle.api.Action;
import org.gradle.api.ActionConfiguration;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ComponentMetadataContext;
import org.gradle.api.artifacts.ComponentMetadataDetails;
import org.gradle.api.artifacts.ComponentMetadataListerDetails;
import org.gradle.api.artifacts.ComponentMetadataRule;
import org.gradle.api.artifacts.ComponentMetadataVersionLister;
import org.gradle.api.artifacts.DirectDependenciesMetadata;
import org.gradle.api.artifacts.ModuleVersionIdentifier;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.artifacts.repositories.ArtifactRepository;
import org.gradle.api.artifacts.repositories.IvyArtifactRepository;
import org.gradle.api.artifacts.repositories.IvyPatternRepositoryLayout;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.artifacts.repositories.MetadataSupplierAware;
import org.gradle.api.artifacts.repositories.RepositoryContentDescriptor;
import org.gradle.api.artifacts.transform.CacheableTransform;
import org.gradle.api.artifacts.transform.InputArtifact;
import org.gradle.api.artifacts.transform.TransformAction;
import org.gradle.api.artifacts.transform.TransformOutputs;
import org.gradle.api.artifacts.transform.TransformParameters;
import org.gradle.api.attributes.Attribute;
import org.gradle.api.attributes.LibraryElements;
import org.gradle.api.file.Directory;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.initialization.Settings;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.PluginAware;
import org.gradle.api.provider.Provider;
import org.gradle.api.specs.Spec;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.tooling.BuildLauncher;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.gradle.util.GradleVersion;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static dev.gradleplugins.internal.util.MinimumDependencyResolutionManagement.dependencyResolutionManagement;
import static dev.gradleplugins.internal.util.MinimumDependencyResolutionManagement.dependencyResolutionManagement;
import static dev.gradleplugins.internal.util.OnceAction.once;

public final class GradleDistributionRepositories {
    private static final Spec<ArtifactRepository> GRADLE_DISTRIBUTIONS_REPOSITORY_SPEC = repo -> repo.getName().startsWith("Gradle Distributions");
    private static final String GENERATED_JAR_ARTIFACT_TYPE = "generated-jar";

    private GradleDistributionRepositories() {}

    public static final class FileName implements Callable<String> {
        private final String baseName;
        private final String version;
        private final String classifier;
        private final String extension;

        private FileName(String baseName, String version, @Nullable String classifier, String extension) {
            this.baseName = baseName;
            this.version = version;
            this.classifier = classifier;
            this.extension = extension;
        }

        public String getExtension() {
            return extension;
        }

        public String getBaseName() {
            return baseName;
        }

        public String getClassifier() {
            return classifier;
        }

        public String getVersion() {
            return version;
        }

        public FileName withExtension(String ext) {
            return new FileName(baseName, version, classifier, ext);
        }

        public String toString() {
            final StringBuilder builder = new StringBuilder();
            builder.append(baseName);
            builder.append("-").append(version);
            if (classifier != null) {
                builder.append("-").append(classifier);
            }
            builder.append(".").append(extension);
            return builder.toString();
        }

        private static final Pattern FILE_NAME_PATTERN = Pattern.compile("^(.*?)-(\\d.*\\d)(?:-([^-]*))?\\.(.+)$");
        public static FileName parse(String name) {
            Matcher matcher = FILE_NAME_PATTERN.matcher(name);

            if (matcher.matches()) {
                String baseName = matcher.group(1);
                String version = matcher.group(2);
                String classifier = null;
                String extension = null;
                if (matcher.groupCount() == 5) {
                    classifier = matcher.group(4);
                    extension = matcher.group(5);
                } else {
                    extension = matcher.group(4);
                }
                return new FileName(baseName, version, classifier, extension);
            } else {
                throw new RuntimeException("Invalid file name");
            }
        }

        @Override
        public String call() throws Exception {
            return toString();
        }
    }

    private static final class Filenames {
        public Filename file(ModuleVersionIdentifier id) {
            return new Filename() {
                private final FileName name = new FileName(id.getName(), id.getVersion(), null, null);

                @Override
                public String generatedJar() {
                    return name.withExtension(GENERATED_JAR_ARTIFACT_TYPE).toString();
                }

                @Override
                public String module() {
                    return name.withExtension("module").toString();
                }

                @Override
                public String jar() {
                    return name.withExtension("jar").toString();
                }
            };
        }

        public Filename file(ModuleVersionIdentifier id, String classifier) {
            return new Filename() {
                private final FileName name = new FileName(id.getName(), id.getVersion(), classifier, null);

                @Override
                public String generatedJar() {
                    return name.withExtension(GENERATED_JAR_ARTIFACT_TYPE).toString();
                }

                @Override
                public String module() {
                    return name.withExtension("module").toString();
                }

                @Override
                public String jar() {
                    return name.withExtension("jar").toString();
                }
            };
        }

        public interface Filename {
            String generatedJar();
            String module();
            String jar();
        }
    }

    private static final class ModuleGroupIdentifier {
        private final String value;

        private ModuleGroupIdentifier(String value) {
            this.value = value;
        }

        public static ModuleGroupIdentifier of(String value) {
            return new ModuleGroupIdentifier(value);
        }

        public String asMavenPath() {
            return value.replace('.', '/');
        }

        @Override
        public String toString() {
            return value;
        }
    }

    public static void writeModuleFile(File moduleFile, Filenames names, ModuleGroupIdentifier group, ModuleVersionIdentifier id) {
        moduleFile.getParentFile().mkdirs();
        try (PrintStream out = new PrintStream(moduleFile)) {
            out.println("{");
            out.println("  \"formatVersion\": \"1.1\",");
            out.println("  \"component\": {");
            out.println("    \"group\": \"" + group + "\",");
            out.println("    \"module\": \"" + id.getName() + "\",");
            out.println("    \"version\": \"" + id.getVersion() + "\",");
            out.println("    \"attributes\": {");
            out.println("      \"org.gradle.status\": \"release\"");
            out.println("    }");
            out.println("  },");
            out.println("  \"createdBy\": {");
            out.println("    \"gradle\": {");
            out.println("      \"version\": \"6.8.1\"");
            out.println("    }");
            out.println("  },");
            out.println("  \"variants\": [");
            out.println("    {");
            out.println("      \"name\": \"apiElements\",");
            out.println("      \"attributes\": {");
            out.println("        \"org.gradle.category\": \"library\",");
            out.println("        \"org.gradle.dependency.bundling\": \"external\",");
            out.println("        \"org.gradle.jvm.version\": 8,");
            out.println("        \"org.gradle.libraryelements\": \"jar\",");
            out.println("        \"org.gradle.usage\": \"java-api\"");
            out.println("      },");
            out.println("      \"dependencies\": [");
            out.println("        {");
            out.println("          \"group\": \"org.codehaus.groovy\",");
            out.println("          \"module\": \"groovy\",");
            out.println("          \"version\": {");
            out.println("            \"requires\": \"3.0.21\"");
            out.println("          }");
            out.println("        }");
            out.println("      ],");
            out.println("      \"files\": [");
            out.println("        {");
            out.println("          \"name\": \"" + names.file(id).generatedJar() + "\",");
            out.println("          \"url\": \"" + names.file(id).generatedJar() + "\"");
            out.println("        }");
            out.println("      ]");
            out.println("    },");
            out.println("    {");
            out.println("      \"name\": \"runtimeElements\",");
            out.println("      \"attributes\": {");
            out.println("        \"org.gradle.category\": \"library\",");
            out.println("        \"org.gradle.dependency.bundling\": \"external\",");
            out.println("        \"org.gradle.jvm.version\": 8,");
            out.println("        \"org.gradle.libraryelements\": \"jar\",");
            out.println("        \"org.gradle.usage\": \"java-runtime\"");
            out.println("      },");
            out.println("      \"dependencies\": [");
            out.println("        {");
            out.println("          \"group\": \"org.codehaus.groovy\",");
            out.println("          \"module\": \"groovy-all\",");
            out.println("          \"version\": {");
            out.println("            \"requires\": \"3.0.21\"");
            out.println("          },");
            out.println("          \"thirdPartyCompatibility\": {");
            out.println("            \"artifactSelector\": {");
            out.println("              \"name\": \"groovy-all\",");
            out.println("              \"type\": \"pom\",");
            out.println("              \"extension\": \"pom\"");
            out.println("            }");
            out.println("          }");
            out.println("        },");
            out.println("        {");
            out.println("          \"group\": \"org.jetbrains.kotlin\",");
            out.println("          \"module\": \"kotlin-stdlib\",");
            out.println("          \"version\": {");
            out.println("            \"requires\": \"1.9.23\"");
            out.println("          }");
            out.println("        }");
            out.println("      ],");
            out.println("      \"files\": [");
            out.println("        {");
            out.println("          \"name\": \"" + names.file(id).generatedJar() + "\",");
            out.println("          \"url\": \"" + names.file(id).generatedJar() + "\"");
            out.println("        }");
            out.println("      ]");
            out.println("    },");
            out.println("    {");
            out.println("      \"name\": \"sourcesElements\",");
            out.println("      \"attributes\": {");
            out.println("        \"org.gradle.category\": \"documentation\",");
            out.println("        \"org.gradle.dependency.bundling\": \"external\",");
            out.println("        \"org.gradle.docstype\": \"sources\",");
            out.println("        \"org.gradle.jvm.version\": 8,");
            out.println("        \"org.gradle.usage\": \"java-runtime\"");
            out.println("      },");
            out.println("      \"files\": [");
            out.println("        {");
            out.println("          \"name\": \"" + names.file(id, "sources").generatedJar() + "\",");
            out.println("          \"url\": \"" + names.file(id, "sources").generatedJar() + "\"");
            out.println("        }");
            out.println("      ]");
            out.println("    },");
            out.println("    {");
            out.println("      \"name\": \"javadocElements\",");
            out.println("      \"attributes\": {");
            out.println("        \"org.gradle.category\": \"documentation\",");
            out.println("        \"org.gradle.dependency.bundling\": \"external\",");
            out.println("        \"org.gradle.docstype\": \"javadoc\",");
            out.println("        \"org.gradle.jvm.version\": 8,");
            out.println("        \"org.gradle.usage\": \"java-runtime\"");
            out.println("      },");
            out.println("      \"files\": [");
            out.println("        {");
            out.println("          \"name\": \"" + names.file(id, "javadoc").generatedJar() + "\",");
            out.println("          \"url\": \"" + names.file(id, "javadoc").generatedJar() + "\"");
            out.println("        }");
            out.println("      ]");
            out.println("    }");
            out.println("  ]");
            out.println("}");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /*private*/ static abstract /*final*/ class ProjectRule implements Plugin<Project> {
        @Inject
        public ProjectRule() {}

        @Override
        public void apply(Project project) {
            Attribute<String> att = Attribute.of("my-att", String.class);
            project.getDependencies().attributesSchema(it -> it.attribute(att));

            project.getDependencies().getArtifactTypes().create(GENERATED_JAR_ARTIFACT_TYPE).getAttributes().attribute(att, "foo");

            project.afterEvaluate(__ -> {
                project.getConfigurations().all(config -> {
                    if (config.isCanBeResolved()) {
                        config.attributes(attributes -> {
                            attributes.attribute(att, "bar");
                        });
                    }
                });
            });

            project.getDependencies().getAttributesSchema().attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE);
            project.getDependencies().registerTransform(GeneratedGradleJarTransform.class, spec -> {
                spec.getFrom()
                        .attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, project.getObjects().named(LibraryElements.class, LibraryElements.JAR))
                        .attribute(att, "foo")
                ;
                spec.getTo()
                        .attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, project.getObjects().named(LibraryElements.class, LibraryElements.JAR))
                        .attribute(att, "bar")
                ;
                spec.parameters(parameters -> {
                    parameters.getGradleUserHomeDirectory().set(project.getGradle().getGradleHomeDir());
                });
            });
        }

        @CacheableTransform
        /*private*/ static abstract /*final*/ class GeneratedGradleJarTransform implements TransformAction<GeneratedGradleJarTransform.Parameters> {
            public interface Parameters extends TransformParameters {
                @Internal
                DirectoryProperty getGradleUserHomeDirectory();
            }

            @PathSensitive(PathSensitivity.NAME_ONLY)
            @InputArtifact
            public abstract Provider<FileSystemLocation> getInputArtifact();

            @Inject
            public GeneratedGradleJarTransform() {}

            @Override
            public void transform(TransformOutputs outputs) {
                withWorkingDirectory(workingDirectory -> {
                    forGradleJar(FileName.parse(getInputArtifact().map(FileSystemLocation::getAsFile).get().getName()), gradleJar -> {
                        if (gradleJar.getName().getClassifier() == null) {
                            try {
                                Files.write(workingDirectory.resolve("build.gradle"), Arrays.asList(
                                        "def configuration = configurations.create('generator')",
                                        "dependencies {",
                                        "  generator " + gradleJar.asGroovyDsl(),
                                        "}",
                                        "tasks.create('generate') {",
                                        "  doLast {",
                                        "    configuration.resolve()",
                                        "  }",
                                        "}"
                                ));
                                Files.createFile(workingDirectory.resolve("settings.gradle"));

                                final GradleConnector connector = GradleConnector.newConnector();
                                connector.useGradleVersion(gradleJar.getGradleVersion());
                                connector.useGradleUserHomeDir(getParameters().getGradleUserHomeDirectory().getAsFile().get());
                                connector.forProjectDirectory(workingDirectory.toFile());

                                withOutput(workingDirectory.resolve("output.txt"), outStream -> {
                                    try (ProjectConnection connection = connector.connect()) {
                                        final BuildLauncher launcher = connection.newBuild().forTasks("generate");
                                        launcher.setStandardOutput(outStream);
                                        launcher.setStandardError(outStream);
                                        launcher.run();
                                    }
                                });

                                File jarFile = outputs.file(gradleJar.getName());
                                File generatedJarFile = getParameters().getGradleUserHomeDirectory().file("caches/" + gradleJar.getGradleVersion() + "/generated-gradle-jars/" + gradleJar.getName()).map(FileSystemLocation::getAsFile).get();
                                Files.copy(generatedJarFile.toPath(), jarFile.toPath());
                            } catch (
                                    IOException e) {
                                throw new RuntimeException(e);
                            }
                        } else if (gradleJar.getName().getClassifier().equals("javadoc")) {
                            throw new UnsupportedOperationException("Javadoc generation not supported");
                        } else if (gradleJar.getName().getClassifier().equals("sources")) {
                            throw new UnsupportedOperationException("Sources generation not supported");
                        }
                    });

                });
            }

            private static void withOutput(Path outputFile, Consumer<? super OutputStream> action) {
                try (OutputStream outStream = Files.newOutputStream(outputFile)) {
                    action.accept(outStream);
                } catch (Throwable ex) {
                    throw new RuntimeException("See " + outputFile, ex);
                }
            }

            private static void withWorkingDirectory(Consumer<? super Path> action) {
                try {
                    Path workingDirectory = Files.createTempDirectory("generated-gradle-jars");
                    action.accept(workingDirectory);
                    // TODO: Clear directory
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            private static void forGradleJar(FileName artifactName, Consumer<? super GeneratedGradleJar> action) {
                assert artifactName.getExtension().equals(GENERATED_JAR_ARTIFACT_TYPE);

                action.accept(new GeneratedGradleJar() {
                    @Override
                    public String getGradleVersion() {
                        return artifactName.getVersion();
                    }

                    private String getBaseName() {
                        return artifactName.getBaseName();
                    }

                    @Override
                    public String asGroovyDsl() {
                        if (artifactName.getBaseName().equals("gradle-api")) {
                            return "gradleApi()";
                        } else if (artifactName.getBaseName().equals("gradle-test-kit")) {
                            return "gradleTestKit()";
                        } else {
                            throw new UnsupportedOperationException();
                        }
                    }

                    @Override
                    public FileName getName() {
                        return artifactName.withExtension("jar");
                    }
                });
            }

            private interface GeneratedGradleJar {
                String getGradleVersion();
                String asGroovyDsl();

                FileName getName();
            }
        }
    }

    /*private*/ static abstract /*final*/ class Rule implements Plugin<PluginAware> {
        private final ObjectFactory objects;
        private final ProjectLayout layout;

        @Inject
        public Rule(ObjectFactory objects, ProjectLayout layout) {
            this.objects = objects;
            this.layout = layout;
        }

        @Override
        public void apply(PluginAware target) {
            if (target instanceof Settings) {
                applyTo((Settings) target);
            } else if (target instanceof Project) {
                applyTo((Project) target);
            } else {
                throw new UnsupportedOperationException();
            }
        }

        private void applyTo(Settings settings) {
            dependencyResolutionManagement(settings, it -> {
                // TODO: Add support for dynamic version
                settings.getExtensions().add(Factory.class, "$factory", new BaseFactory(it.getRepositories()));
            });
            dependencyResolutionManagement(settings, configureDependencyResolution(a -> settings.getGradle().allprojects(a)));
        }

        private void applyTo(Project project) {
            project.getExtensions().add(Factory.class, "$factory", new DynamicVersionSupportFactory(new GradleDistributionVersions(project.getResources().getText().fromUri("https://services.gradle.org/versions/all").asFile()), new BaseFactory(project.getRepositories())));
            dependencyResolutionManagement(project, configureDependencyResolution(a -> a.execute(project)));
        }

        private Action<MinimumDependencyResolutionManagement> configureDependencyResolution(Consumer<Action<Project>> action) {
            return dependencyResolutionManagement -> {
                dependencyResolutionManagement.repositories(repositories -> {
                    repositories.matching(GRADLE_DISTRIBUTIONS_REPOSITORY_SPEC).all(once(() -> {
                        // TODO: Potential cache collision, differentiate between settings vs project cache dir
                        Bob cacheDir = objects.newInstance(Bob.class, layout.getBuildDirectory().dir("repo"));
                        dependencyResolutionManagement.repositories(repo ->repo.maven(cacheDir.asMavenRepository())); // required

                        dependencyResolutionManagement.component(it -> {
                            it.withModule("dev.gradleplugins:gradle-api", GradleDistributionToGeneratedGradleJarsMetadataRule.class, cacheDir.asMetadataRuleParameters());
                            it.withModule("dev.gradleplugins:gradle-test-kit", GradleDistributionToGeneratedGradleJarsMetadataRule.class, cacheDir.asMetadataRuleParameters());
                        });

                        action.accept(project -> {
                            project.getPluginManager().apply(ProjectRule.class);
                        });
                    }));
                });
            };
        }

        public interface GeneratedJarCache {
            Action<DirectDependenciesMetadata> forGeneratedModuleOf(ModuleVersionIdentifier id);
        }

        public static abstract class Bob implements GeneratedJarCache {
            private static final ModuleGroupIdentifier GENERATED_GROUP = ModuleGroupIdentifier.of("dev.gradleplugins.generated");
            private static final Filenames names = new Filenames();
            private final Provider<Directory> cacheDir;

            @Inject
            public Bob(Provider<Directory> cacheDir) {
                this.cacheDir = cacheDir;
            }

            @Override
            public Action<DirectDependenciesMetadata> forGeneratedModuleOf(ModuleVersionIdentifier t) {
                return dependency -> {
                    dependency.add(GENERATED_GROUP + ":" + t.getName() + ":" + t.getVersion());
                    File moduleFile = cacheDir.get().file(GENERATED_GROUP.asMavenPath() + "/" + t.getName() + "/" + t.getVersion() + "/" + names.file(t).module()).getAsFile();
                    writeModuleFile(moduleFile, names, GENERATED_GROUP, t);
                    try {
                        new File(moduleFile.getParentFile(), names.file(t).generatedJar()).createNewFile();
                        new File(moduleFile.getParentFile(), names.file(t, "sources").generatedJar()).createNewFile();
                        new File(moduleFile.getParentFile(), names.file(t, "javadoc").generatedJar()).createNewFile();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                };
            }

            public Action<MavenArtifactRepository> asMavenRepository() {
                return repo -> {
                    repo.setUrl(cacheDir);
                    repo.metadataSources(it -> {
                        it.gradleMetadata();
                        it.artifact();
                    });
                    repo.mavenContent(it -> {
                        it.includeGroup(GENERATED_GROUP.toString());
                    });
                };
            }

            // Because ActionConfiguration doesn't allow Gradle types
            public Action<ActionConfiguration> asMetadataRuleParameters() {
                return spec -> spec.params(cacheDir);
            }
        }


        /*private*/ static abstract /*final*/ class GradleDistributionToGeneratedGradleJarsMetadataRule implements ComponentMetadataRule {
            private final GeneratedJarCache cache;

            @Inject
            public GradleDistributionToGeneratedGradleJarsMetadataRule(ObjectFactory objects, Provider<Directory> cacheDir) {
                this(objects.newInstance(Bob.class, cacheDir));
            }

            private GradleDistributionToGeneratedGradleJarsMetadataRule(GeneratedJarCache cache) {
                this.cache = cache;
            }

            @Override
            public void execute(ComponentMetadataContext context) {
                GradleVersion version = GradleVersion.version(context.getDetails().getId().getVersion());
                if (version.isSnapshot() || !version.getBaseVersion().getVersion().equals(version.getVersion()) || GradleVersion.version("8.9").compareTo(version) < 0) {
                    ComponentMetadataDetails details = context.getDetails();
                    details.addVariant("apiElements", x -> {
                        x.withDependencies(cache.forGeneratedModuleOf(details.getId()));
                    });
                }
            }
        }
    }

    public interface Factory {
        <T extends ArtifactRepository & MetadataSupplierAware> T gradleDistributions(Action<T> action);
        <T extends ArtifactRepository & MetadataSupplierAware> T gradleDistributionsSnapshots(Action<T> action);
    }

    public static final class GradleDistributionVersions implements Iterable<GradleVersion> {
        private final File all;

        public GradleDistributionVersions(File all) {
            this.all = all;
        }

        @Override
        public Iterator<GradleVersion> iterator() {
            try (Stream<String> stream = Files.lines(all.toPath())) {
                return stream.filter(it -> it.contains("version"))
                        .map(String::trim)
                        .map(it -> it.replace("\"version\" : \"", "").replace("\",", ""))
                        .map(GradleVersion::version)
                        .collect(Collectors.toList()).iterator();
            } catch (
                    IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static final class DynamicVersionSupportFactory implements Factory {
        private final GradleDistributionVersions versions;
        private final Factory delegate;

        public DynamicVersionSupportFactory(GradleDistributionVersions versions, Factory delegate) {
            this.versions = versions;
            this.delegate = delegate;
        }

        @Override
        public <T extends ArtifactRepository & MetadataSupplierAware> T gradleDistributions(Action<T> action) {
            return delegate.gradleDistributions(withDynamicVersionSupport(it -> !it.isSnapshot(), action));
        }

        @Override
        public <T extends ArtifactRepository & MetadataSupplierAware> T gradleDistributionsSnapshots(Action<T> action) {
            return delegate.gradleDistributionsSnapshots(withDynamicVersionSupport(it -> it.isSnapshot(), action));
        }

        private <T extends ArtifactRepository & MetadataSupplierAware> Action<T> withDynamicVersionSupport(Predicate<? super GradleVersion> predicate, Action<? super T> action) {
            return repo -> {
                action.execute(repo);

                repo.setComponentVersionsLister(MyLister.class, spec -> spec.params(StreamSupport.stream(versions.spliterator(), false).filter(predicate).map(GradleVersion::getVersion).collect(Collectors.toList())));
            };
        }
    }

    /*private*/ static abstract /*final*/ class MyLister implements ComponentMetadataVersionLister {
        private final List<String> versions;

        @Inject
        public MyLister(List<String> versions) {
            this.versions = versions;
        }

        @Override
        public void execute(ComponentMetadataListerDetails details) {
            details.listed(versions);
        }
    }

    private static final class BaseFactory implements Factory {
        private final RepositoryHandler repositories;

        public BaseFactory(RepositoryHandler repositories) {
            this.repositories = repositories;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T extends ArtifactRepository & MetadataSupplierAware> T gradleDistributions(Action<T> action) {
            return (T) repositories.ivy(repo -> {
                action.execute((T) repo);

                repo.setName("Gradle Distributions");
                repo.setUrl("https://services.gradle.org/distributions/");
                repo.patternLayout(this::toVersionedBinDistribution);
                repo.metadataSources(IvyArtifactRepository.MetadataSources::artifact);
                repo.content(this::onlyGeneratedGradleJars);
            });
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T extends ArtifactRepository & MetadataSupplierAware> T gradleDistributionsSnapshots(Action<T> action) {
            return (T) repositories.ivy(repo -> {
                action.execute((T) repo);

                repo.setName("Gradle Distributions Snapshots");
                repo.setUrl("https://services.gradle.org/distributions-snapshots/");
                repo.patternLayout(this::toVersionedBinDistribution);
                repo.metadataSources(IvyArtifactRepository.MetadataSources::artifact);
                repo.content(this::onlyGeneratedGradleJars);
            });
        }

        private void toVersionedBinDistribution(IvyPatternRepositoryLayout layout) {
            layout.artifact("/gradle-[revision]-bin.zip");
        }

        private void onlyGeneratedGradleJars(RepositoryContentDescriptor content) {
            content.includeModule("dev.gradleplugins", "gradle-api");
            content.includeModule("dev.gradleplugins", "gradle-test-kit");
        }
    }
}
