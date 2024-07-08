package dev.gradleplugins.internal;

import org.gradle.api.Action;
import org.gradle.api.JavaVersion;
import org.gradle.api.Named;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.NamedDomainObjectSet;
import org.gradle.api.Project;
import org.gradle.api.Transformer;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.compile.JavaCompile;

import javax.inject.Inject;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class JvmCompatibilities {
    public abstract Property<JavaVersion> getSourceCompatibility();
    public abstract Property<JavaVersion> getTargetCompatibility();

    public static abstract class ForProjectExtension extends JvmCompatibilities {}

    private static Provider<JavaVersion> javaToolchainLanguageVersion(Project project) {
        final JavaPluginExtension java = project.getExtensions().getByType(JavaPluginExtension.class);

        try {
            Method JavaPluginExtension__getToolchain = java.getClass().getDeclaredMethod("getToolchain");
            Object toolchain = JavaPluginExtension__getToolchain.invoke(java);
            Method JavaToolchainSpec__getLanguageVersion = toolchain.getClass().getDeclaredMethod("getLanguageVersion");
            Provider<?> languageVersion = (Provider<?>) JavaToolchainSpec__getLanguageVersion.invoke(toolchain);
            return languageVersion.map(Object::toString).map(JavaVersion::toVersion);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            // ignore failure, most likely lower Gradle
        }

        return project.provider(() -> null);
    }

    private static <T> Provider<? extends Iterable<T>> asList(Provider<T> provider) {
        return provider.map(Collections::singletonList).orElse(Collections.emptyList());
    }

    private static <T> Provider<T> firstDefined(Project project, Action<? super ListProperty<T>> action) {
        @SuppressWarnings("unchecked")
        ListProperty<T> versions = (ListProperty<T>) project.getObjects().listProperty(Object.class);
        versions.finalizeValueOnRead();
        action.execute(versions);
        return versions.map(it -> {
            final Iterator<T> iter = it.iterator();
            if (!iter.hasNext()) {
                return null;
            } else {
                return iter.next();
            }
        });
    }

    private static <T> Callable<T> disconnect(Provider<T> provider) {
        return new Callable<T>() {
            private T value;
            private boolean memoized = false;

            @Override
            public T call() throws Exception {
                if (!memoized) {
                    value = provider.getOrNull();
                    memoized = true;
                }
                return value;
            }
        };
    }

    /*private*/ static abstract /*final*/ class ProjectJvmCompatibilitiesExtension extends ForProjectExtension {
        @Inject
        public ProjectJvmCompatibilitiesExtension(Project project) {
            JavaPluginExtension java = project.getExtensions().getByType(JavaPluginExtension.class);

            getTargetCompatibility().convention(firstDefined(project, (ListProperty<JavaVersion> versions) -> {
                versions.addAll(asList(javaToolchainLanguageVersion(project)));
                versions.addAll(asList(project.provider(disconnect(project.provider(java::getTargetCompatibility).map(toFinalValue(java::getTargetCompatibility, java::setTargetCompatibility, it -> {
                        // Important to restore the normal behaviour
                        //   Normally, targetCompatibility derived from sourceCompatibility when not set
                        final JavaVersion sourceCompatibility = java.getSourceCompatibility();
                        if (!sourceCompatibility.equals(JavaVersion.VERSION_1_1)) {
                            java.setTargetCompatibility(sourceCompatibility);
                        } else if (javaToolchainLanguageVersion(project).isPresent()) {
                            java.setTargetCompatibility(null);
                        } else {
                            java.setTargetCompatibility(it);
                        }
                    }))))));
            }).orElse(getSourceCompatibility()));
            getTargetCompatibility().finalizeValueOnRead();
            getTargetCompatibility().disallowChanges();
            project.afterEvaluate(__ -> getTargetCompatibility().finalizeValue());

            getSourceCompatibility().convention(firstDefined(project, (ListProperty<JavaVersion> versions) -> {
                versions.addAll(asList(javaToolchainLanguageVersion(project)));
                versions.addAll(asList(project.provider(disconnect(project.provider(java::getSourceCompatibility).map(toFinalValue(java::getSourceCompatibility, java::setSourceCompatibility, it -> {
                        // Important to restore the normal behaviour
                        java.setSourceCompatibility(it);
                    }))))));
            }));
            getSourceCompatibility().finalizeValueOnRead();
            getSourceCompatibility().disallowChanges();
            project.afterEvaluate(__ -> getSourceCompatibility().finalizeValue());
        }

        private Transformer<JavaVersion, JavaVersion> toFinalValue(Supplier<JavaVersion> getter, Consumer<JavaVersion> setter, Action<? super JavaVersion> restoreAction) {
            final JavaVersion originalValue = getter.get();
            setter.accept(JavaVersion.VERSION_1_1);
            return it -> {
                if (it.equals(JavaVersion.VERSION_1_1)) {
                    restoreAction.execute(originalValue);
                    return null;
                } else {
                    return it;
                }
            };
        }
    }

    /*private*/ static abstract /*final*/ class Rule implements org.gradle.api.Plugin<Project> {
        @Inject
        public Rule() {}

        @Override
        public void apply(Project project) {
            final ProjectJvmCompatibilitiesExtension jvmCompatibilities = project.getExtensions().create("$jvmProjectCompatibilities", ProjectJvmCompatibilitiesExtension.class, project);

            final SourceSetJvmCompatibilitiesExtension extension = project.getExtensions().create("$jvmCompatibilities", SourceSetJvmCompatibilitiesExtension.class);

            extension.configureEach(it -> {
                it.getSourceCompatibility().convention(javaToolchainLanguageVersion(project)
                        .orElse(jvmCompatibilities.getSourceCompatibility()));
                it.getTargetCompatibility().convention(javaToolchainLanguageVersion(project)
                        .orElse(jvmCompatibilities.getTargetCompatibility())
                        .orElse(it.getSourceCompatibility()));
            });

            extension.configureEach(it -> {
                project.getTasks().named(it.getSourceSet().getCompileJavaTaskName(), JavaCompile.class).configure(task -> {
                    task.getConventionMapping().map("sourceCompatibility", () -> it.getSourceCompatibility().getOrElse(JavaVersion.current()).toString());
                    task.getConventionMapping().map("targetCompatibility", () -> it.getTargetCompatibility().getOrElse(JavaVersion.current()).toString());
                    // TODO: Gradle 6.7+ configure the javaCompiler property
                });

                // TODO: Configure test task (if available)
            });
        }
    }

    public interface ForSourceSetExtension {
        NamedDomainObjectProvider<SourceSetJvmCompatibilities> forSourceSet(SourceSet sourceSet);
        void configureEach(Action<? super SourceSetJvmCompatibilities> action);

        abstract class SourceSetJvmCompatibilities extends JvmCompatibilities {
            public abstract SourceSet getSourceSet();
        }
    }

    /*private*/ static abstract /*final*/ class SourceSetJvmCompatibilitiesExtension implements ForSourceSetExtension {
        private final ObjectFactory objects;
        private final NamedDomainObjectSet<SourceSetJvmCompatibilities> compatibilities;

        @Inject
        public SourceSetJvmCompatibilitiesExtension(ObjectFactory objects) {
            this.objects = objects;
            this.compatibilities = objects.namedDomainObjectSet(SourceSetJvmCompatibilities.class);
        }

        public NamedDomainObjectProvider<SourceSetJvmCompatibilities> forSourceSet(SourceSet sourceSet) {
            if (compatibilities.findByName(sourceSet.getName()) == null) {
                compatibilities.add(objects.newInstance(DefaultSourceSetJvmCompatibilities.class, sourceSet));
            }
            return compatibilities.named(sourceSet.getName());
        }

        @Override
        public void configureEach(Action<? super SourceSetJvmCompatibilities> action) {
            compatibilities.configureEach(action);
        }
    }

    /*private*/ static abstract /*final*/ class DefaultSourceSetJvmCompatibilities extends ForSourceSetExtension.SourceSetJvmCompatibilities implements Named {
        private final SourceSet sourceSet;

        @Inject
        public DefaultSourceSetJvmCompatibilities(SourceSet sourceSet) {
            this.sourceSet = sourceSet;
        }

        @Override
        public String getName() {
            return sourceSet.getName();
        }

        public SourceSet getSourceSet() {
            return sourceSet;
        }
    }
}
