package dev.gradleplugins.runnerkit.providers;

import dev.gradleplugins.runnerkit.GradleExecutionContext;
import dev.gradleplugins.runnerkit.GradleExecutor;
import dev.gradleplugins.runnerkit.GradleRunner;
import dev.gradleplugins.runnerkit.InvalidRunnerConfigurationException;
import dev.gradleplugins.test.fixtures.file.TestFile;
import dev.gradleplugins.test.fixtures.gradle.executer.GradleDistribution;
import dev.gradleplugins.test.fixtures.gradle.executer.GradleDistributionFactory;
import dev.gradleplugins.test.fixtures.gradle.executer.internal.DefaultGradleDistribution;
import dev.nokee.core.exec.CommandLine;
import dev.nokee.core.exec.CommandLineToolLogContent;
import lombok.val;
import org.gradle.internal.classloader.ClasspathUtil;
import org.gradle.internal.installation.CurrentGradleInstallation;
import org.gradle.internal.installation.GradleInstallation;
import org.gradle.util.GradleVersion;

import java.io.File;
import java.util.function.Function;
import java.util.function.Supplier;

import static dev.gradleplugins.fixtures.file.FileSystemUtils.file;

public final class GradleDistributionProvider extends AbstractGradleExecutionProvider<GradleDistribution> {
//    public static GradleDistribution executorDefault() {
//        return noValue(GradleDistribution.class);
//    }

    public static GradleDistributionProvider fromGradleRunner() {
        return supplied(GradleDistributionProvider.class, () -> {
            return findGradleInstallFromGradleRunner();
        });
    }

    static DefaultGradleDistribution findGradleInstallFromGradleRunner() {
        GradleInstallation gradleInstallation = CurrentGradleInstallation.get();
        if (gradleInstallation == null) {
            String messagePrefix = "Could not find a Gradle installation to use based on the location of the GradleRunner class";
            try {
                File classpathForClass = ClasspathUtil.getClasspathForClass(GradleRunner.class);
                messagePrefix += ": " + classpathForClass.getAbsolutePath();
            } catch (Exception ignore) {
                // ignore
            }
            throw new InvalidRunnerConfigurationException(messagePrefix + ". Please specify a Gradle runtime to use via GradleRunner.withGradleVersion() or similar.");
        }
        val binDirectory = file(gradleInstallation.getGradleHome(), "bin");
        val gradleExe = file(binDirectory, "gradle");
        val gradleVersion = findVersion(CommandLine.of(gradleExe, "--version").execute().waitFor().assertNormalExitValue().getStandardOutput());
        return new DefaultGradleDistribution(gradleVersion, TestFile.of(gradleInstallation.getGradleHome()), TestFile.of(binDirectory));
//        return GradleDistributionFactory.installation(gradleInstallation.getGradleHome());
    }

    public static GradleVersion findVersion(CommandLineToolLogContent output) {
        return GradleVersion.version(output.getLines().stream().filter(it -> it.startsWith("Gradle ")).map(it -> it.replace("Gradle ", "")).findFirst().orElseThrow(RuntimeException::new));
    }

    public static GradleDistributionProvider fromWrapper(Supplier<File> baseDirectory) {
        return supplied(GradleDistributionProvider.class, () -> {
            return GradleDistributionFactory.wrapper(baseDirectory.get());
        });
    }

    public static GradleDistributionProvider fromGradleWrapper() {
        return calculated(GradleDistributionProvider.class, (GradleExecutionContext context) -> {
            return GradleDistributionFactory.wrapper(context.getWorkingDirectory().get());
        });
    }

    public static GradleDistributionProvider fromGradleWrapper(File baseDirectory) {
        return fixed(GradleDistributionProvider.class, GradleDistributionFactory.wrapper(baseDirectory));
    }

    public static GradleDistributionProvider executorDefault() {
        return calculated(GradleDistributionProvider.class, new Function<GradleExecutionContext, GradleDistribution>() {
            @Override
            public GradleDistribution apply(GradleExecutionContext context) {
                if (isGradleWrapperExecutor(context.getExecutorType())) {
                    return GradleDistributionFactory.wrapper(context.getWorkingDirectory().get());
                } else {
                    return findGradleInstallFromGradleRunner();
                }
            }

            private boolean isGradleWrapperExecutor(Class<? extends GradleExecutor> type) {
                return type.getSimpleName().contains("Wrapper");
            }
        });
    }
}
