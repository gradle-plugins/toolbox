package dev.gradleplugins.fixtures.gradle.runner.parameters;

import dev.gradleplugins.fixtures.gradle.runner.GradleExecutionContext;
import dev.gradleplugins.fixtures.gradle.runner.GradleRunner;
import dev.gradleplugins.fixtures.gradle.runner.InvalidRunnerConfigurationException;
import dev.gradleplugins.test.fixtures.file.TestFile;
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
import java.util.function.Supplier;

import static dev.gradleplugins.fixtures.file.FileSystemUtils.file;

public final class GradleDistribution extends GradleExecutionParameterImpl<dev.gradleplugins.test.fixtures.gradle.executer.GradleDistribution> implements GradleExecutionParameter<dev.gradleplugins.test.fixtures.gradle.executer.GradleDistribution> {
//    public static GradleDistribution executorDefault() {
//        return noValue(GradleDistribution.class);
//    }

    public static GradleDistribution fromGradleRunner() {
        return supplied(GradleDistribution.class, () -> {
            return findGradleInstallFromGradleRunner();
        });
    }

    private static DefaultGradleDistribution findGradleInstallFromGradleRunner() {
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

    public static GradleDistribution fromWrapper(Supplier<File> baseDirectory) {
        return supplied(GradleDistribution.class, () -> {
            return GradleDistributionFactory.wrapper(baseDirectory.get());
        });
    }

    public static GradleDistribution executorDefault() {
        return calculated(GradleDistribution.class, (GradleExecutionContext context) -> {
            if (context.getExecutorType().isGradleTestKit() || context.getExecutorType().equals(GradleExecutorType.UNKNOWN)) {
                return findGradleInstallFromGradleRunner();
            } else if (context.getExecutorType().isGradleWrapper()) {
                return GradleDistributionFactory.wrapper(context.getWorkingDirectory().get());
            }
            throw new RuntimeException();
        });
    }
}
