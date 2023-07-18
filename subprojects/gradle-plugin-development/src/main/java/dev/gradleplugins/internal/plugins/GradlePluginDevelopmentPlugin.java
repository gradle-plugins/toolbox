package dev.gradleplugins.internal.plugins;

import lombok.RequiredArgsConstructor;
import org.gradle.BuildAdapter;
import org.gradle.BuildResult;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.initialization.Settings;
import org.gradle.api.internal.plugins.DslObject;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.GroovySourceSet;
import org.gradle.api.tasks.SourceSet;
import org.gradle.internal.exceptions.DefaultMultiCauseException;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static dev.gradleplugins.internal.util.GradlePluginDevelopmentUtils.sourceSets;

public abstract class GradlePluginDevelopmentPlugin implements Plugin<Object> {
    private static final Logger LOGGER = Logging.getLogger(GradlePluginDevelopmentPlugin.class);

    @Override
    public void apply(Object target) {
        if (target instanceof Project) {
            doApply((Project) target);
        } else if (target instanceof Settings) {
            doApply((Settings)target);
        } else {
            throw new IllegalArgumentException("Please apply 'dev.gradleplugins.gradle-plugin-development' plugin inside the settings.gradle[.kts] or build.gradle[.kts] script.");
        }
    }

    private void doApply(Project project) {
        project.getPluginManager().apply(GradlePluginDevelopmentExtensionPlugin.class);
    }

    private void doApply(Settings settings) {
        settings.getGradle().addBuildListener(new BuildAdapter() {
            @Override
            public void buildFinished(BuildResult result) {
                if (result.getFailure() != null) {
                    MissingGradlePluginDevelopmentArtifactsErrorReporter missingArtifacts = new MissingGradlePluginDevelopmentArtifactsErrorReporter();
                    MissingGradleApiRuntimeDependenciesErrorReporter missingGradleApiRuntimeDependencies = new MissingGradleApiRuntimeDependenciesErrorReporter();

                    GradleFailureVisitor root = new UnwrappingGradleFailureVisitor(MultiGradleFailureVisitor.of(missingArtifacts, missingGradleApiRuntimeDependencies));
                    root.visitCause(result.getFailure());

                    missingArtifacts.report();
                    missingGradleApiRuntimeDependencies.report();
                }
            }
        });
        settings.getGradle().rootProject(rootProject -> {
            rootProject.allprojects(this::applyToProject);
        });
    }

    private void applyToProject(Project project) {
        project.getPluginManager().apply(GradlePluginDevelopmentExtensionPlugin.class);
        project.afterEvaluate(this::warnWhenUsingCoreGradlePluginDevelopment);
    }

    private void warnWhenUsingCoreGradlePluginDevelopment(Project project) {
        if (hasKotlinDslPlugin(project) || hasJavaGradlePluginDevelopment(project) || hasGroovyGradlePluginDevelopment(project)) {
            // Ignores anything to do with kotlin-dsl plugin
            return;
        }

        if (hasLegacyGradlePluginDevelopment(project) && !hasGroovyLanguageCapability(project)) {
            warnAboutUsingJavaGradlePlugin(project);
        } else if (hasLegacyGradlePluginDevelopment(project) && hasGroovyLanguageCapability(project)) {
            if (hasGroovySources(project)) {
                warnAboutUsingGroovyGradlePlugin(project);
            } else if (hasJavaSources(project)) {
                warnAboutUsingJavaGradlePlugin(project);
            } else {
                warnAboutUsingGroovyGradlePlugin(project);
            }
        }
    }

    private static void warnAboutUsingJavaGradlePlugin(Project project) {
        LOGGER.warn(String.format("The Gradle Plugin Development team recommends using 'dev.gradleplugins.java-gradle-plugin' instead of 'java-gradle-plugin' in project '%s'.", project.getPath()));
    }

    private static void warnAboutUsingGroovyGradlePlugin(Project project) {
        LOGGER.warn(String.format("The Gradle Plugin Development team recommends using 'dev.gradleplugins.groovy-gradle-plugin' instead of 'java-gradle-plugin' and 'groovy'/'groovy-base' in project '%s'.", project.getPath()));
    }

    private static boolean hasKotlinDslPlugin(Project project) {
        return project.getPluginManager().hasPlugin("org.gradle.kotlin.kotlin-dsl");
    }

    private static boolean hasJavaGradlePluginDevelopment(Project project) {
        return project.getPluginManager().hasPlugin("dev.gradleplugins.java-gradle-plugin");
    }

    private static boolean hasGroovyGradlePluginDevelopment(Project project) {
        return project.getPluginManager().hasPlugin("dev.gradleplugins.groovy-gradle-plugin");
    }

    private static boolean hasLegacyGradlePluginDevelopment(Project project) {
        return project.getPluginManager().hasPlugin("java-gradle-plugin");
    }

    private static boolean hasGroovyLanguageCapability(Project project) {
        return project.getPluginManager().hasPlugin("groovy-base");
    }

    private static boolean hasGroovySources(Project project) {
        SourceSet sourceSet = sourceSets(project).getByName("main");
        SourceDirectorySet groovySourceSet = (SourceDirectorySet) sourceSet.getExtensions().findByName("groovy");
        if (groovySourceSet == null) {
            groovySourceSet = safeDereference(new DslObject(sourceSet).getConvention().findPlugin(GroovySourceSet.class), GroovySourceSet::getAllGroovy);
        }

        if (groovySourceSet == null) {
            return false;
        }
        return groovySourceSet.getSrcDirs().stream().anyMatch(File::exists);
    }

    @Nullable
    private static <OUT, IN> OUT safeDereference(@Nullable IN reference, Function<? super IN, ? extends OUT> dereferenceFunction) {
        if (reference == null) {
            return null;
        } else {
            return dereferenceFunction.apply(reference);
        }
    }

    private static boolean hasJavaSources(Project project) {
        SourceSet sourceSet = sourceSets(project).getByName("main");
        return sourceSet.getAllJava().getSrcDirs().stream().anyMatch(File::exists);
    }

    private interface GradleFailureVisitor {
        void visitCause(Throwable cause);
    }

    @RequiredArgsConstructor
    private static class UnwrappingGradleFailureVisitor implements GradleFailureVisitor {
        private final GradleFailureVisitor delegate;

        @Override
        public void visitCause(Throwable cause) {
            while (cause != null) {
                if (cause instanceof DefaultMultiCauseException) {
                    for (Throwable c : ((DefaultMultiCauseException) cause).getCauses()) {
                        visitCause(c);
                    }
                }
                delegate.visitCause(cause);
                cause = cause.getCause();
            }
        }
    }

    @RequiredArgsConstructor
    private static class MultiGradleFailureVisitor implements GradleFailureVisitor {
        private final List<GradleFailureVisitor> delegates;

        @Override
        public void visitCause(Throwable cause) {
            for (GradleFailureVisitor delegate : delegates) {
                delegate.visitCause(cause);
            }
        }

        public static MultiGradleFailureVisitor of(GradleFailureVisitor... delegates) {
            return new MultiGradleFailureVisitor(Arrays.asList(delegates));
        }
    }

    private static class MissingGradleApiRuntimeDependenciesErrorReporter implements GradleFailureVisitor {
        private boolean missingKotlinStdlib = false;
        private boolean missingGroovyAll = false;

        @Override
        public void visitCause(Throwable cause) {
            if (cause.getMessage() == null) {
                return; // skip
            }

            if (cause.getMessage().startsWith("Could not find org.codehaus.groovy:groovy-all") && cause.getMessage().contains("dev.gradleplugins:gradle-api:")) {
                missingGroovyAll = true;
            } else if (cause.getMessage().startsWith("Could not find org.jetbrains.kotlin:kotlin-stdlib") && cause.getMessage().contains("dev.gradleplugins:gradle-api:")) {
                missingKotlinStdlib = true;
            }
        }

        public void report() {
            if (missingGroovyAll || missingKotlinStdlib) {
                LOGGER.error("Please verify Gradle API was intentionally declared for runtime usage, see https://nokee.dev/docs/current/manual/gradle-plugin-development.html#sec:gradle-dev-compileonly-vs-implementation.");
            }

            if (missingGroovyAll && missingKotlinStdlib) {
                LOGGER.error("If runtime usage of the Gradle API is expected, please declare a repository containing org.codehaus.groovy:groovy-all and org.jetbrains.kotlin:kotlin-stdlib artifacts, i.e. repositories.mavenCentral().");
            } else if (missingGroovyAll) {
                LOGGER.error("If runtime usage of the Gradle API is expected, Please declare a repository containing org.codehaus.groovy:groovy-all artifacts, i.e. repositories.mavenCentral().");
            } else if (missingKotlinStdlib) {
                LOGGER.error("If runtime usage of the Gradle API is expected, Please declare a repository containing org.jetbrains.kotlin:kotlin-stdlib artifacts, i.e. repositories.mavenCentral().");
            }
        }
    }

    private static class MissingGradlePluginDevelopmentArtifactsErrorReporter implements GradleFailureVisitor {
        private boolean shouldReport = false;
        @Override
        public void visitCause(Throwable cause) {
            if (cause.getMessage() == null) {
                return; // skip
            }

            if (cause.getMessage().startsWith("Cannot resolve external dependency dev.gradleplugins:") || cause.getMessage().startsWith("Could not find dev.gradleplugins:")) {
                shouldReport = true;
            }
        }

        public void report() {
            if (shouldReport) {
                LOGGER.error("Please declare a repository using repositories.gradlePluginDevelopment().");
            }
        }
    }
}
