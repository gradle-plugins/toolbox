package dev.gradleplugins.runnerkit.providers;

import dev.gradleplugins.fixtures.file.FilePreconditions;
import dev.gradleplugins.runnerkit.CommandLineToolLogContent;
import dev.gradleplugins.runnerkit.GradleDistribution;
import dev.gradleplugins.runnerkit.GradleExecutionContext;
import dev.gradleplugins.runnerkit.GradleExecutor;
import dev.gradleplugins.runnerkit.GradleRunner;
import dev.gradleplugins.runnerkit.InvalidRunnerConfigurationException;
import dev.gradleplugins.runnerkit.distributions.DownloadableGradleDistribution;
import dev.gradleplugins.runnerkit.distributions.LocalGradleDistribution;
import dev.gradleplugins.runnerkit.distributions.VersionAwareGradleDistribution;
import dev.gradleplugins.runnerkit.distributions.WrapperAwareGradleDistribution;
import lombok.Value;
import lombok.val;
import org.gradle.internal.classloader.ClasspathUtil;
import org.gradle.internal.installation.CurrentGradleInstallation;
import org.gradle.internal.installation.GradleInstallation;
import org.gradle.util.GradleVersion;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class GradleDistributionProvider extends AbstractGradleExecutionProvider<GradleDistribution> {
    public static GradleDistributionProvider fromGradleRunner() {
        return supplied(GradleDistributionProvider.class, () -> {
            return findGradleInstallFromGradleRunner();
        });
    }

    static GradleDistribution findGradleInstallFromGradleRunner() {
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
        return new GradleDistributionInstallationImpl(gradleInstallation.getGradleHome());
    }

    public static GradleVersion findVersion(CommandLineToolLogContent output) {
        return GradleVersion.version(output.getLines().stream().filter(it -> it.startsWith("Gradle ")).map(it -> it.replace("Gradle ", "")).findFirst().orElseThrow(RuntimeException::new));
    }

    public static GradleDistributionProvider fromWrapper(Supplier<File> baseDirectory) {
        return supplied(GradleDistributionProvider.class, () -> {
            return new GradleDistributionWrapperImpl(baseDirectory.get());
        });
    }

    public static GradleDistributionProvider fromGradleWrapper() {
        return calculated(GradleDistributionProvider.class, (GradleExecutionContext context) -> {
            return new GradleDistributionWrapperImpl(context.getWorkingDirectory().get());
        });
    }

    public static GradleDistributionProvider fromGradleWrapper(File baseDirectory) {
        return fixed(GradleDistributionProvider.class, new GradleDistributionWrapperImpl(baseDirectory));
    }

    public static GradleDistributionProvider executorDefault() {
        return calculated(GradleDistributionProvider.class, new Function<GradleExecutionContext, GradleDistribution>() {
            @Override
            public GradleDistribution apply(GradleExecutionContext context) {
                if (isGradleWrapperExecutor(context.getExecutorType())) {
                    return new GradleDistributionWrapperImpl(context.getWorkingDirectory().get());
                } else {
                    return new GradleDistributionVersionImpl(GradleVersion.current().getVersion());
                }
            }

            private boolean isGradleWrapperExecutor(Class<? extends GradleExecutor> type) {
                return type.getSimpleName().contains("Wrapper");
            }
        });
    }

    private static GradleDistributionProvider current() {
        return version(GradleVersion.current().getVersion());
    }

    public static GradleDistributionProvider version(String versionNumber) {
        return fixed(GradleDistributionProvider.class, new GradleDistributionVersionImpl(versionNumber));
    }

    private static GradleDistribution ofVersion(String version) {
        return new GradleDistributionVersionImpl(version);
    }

    public static GradleDistributionProvider installation(File installation) {
        return fixed(GradleDistributionProvider.class, new GradleDistributionInstallationImpl(installation));
    }

    public static GradleDistributionProvider uri(URI distribution) {
        return fixed(GradleDistributionProvider.class, new GradleDistributionUriImpl(distribution));
    }

    @Value
    private static class GradleDistributionVersionImpl implements VersionAwareGradleDistribution {
        String version;
    }

    @Value
    private static class GradleDistributionInstallationImpl implements LocalGradleDistribution {
        File installationDirectory;
    }

    @Value
    private static class GradleDistributionUriImpl implements DownloadableGradleDistribution {
        URI uri;
    }

    @Value
    private static class GradleDistributionWrapperImpl implements WrapperAwareGradleDistribution {
        private static final Pattern DISTRIBUTION_URL_VERSION_PATTERN = Pattern.compile("(\\d+.\\d+(.\\d+)?(-rc-\\d)?)");
        private final File rootProjectDirectory;

        public GradleDistributionWrapperImpl(File rootProjectDirectory) {
            FilePreconditions.checkIsDirectory(rootProjectDirectory, "Invalid project directory");
            assertWrapperInstalled(rootProjectDirectory);
            this.rootProjectDirectory = rootProjectDirectory;
        }

        private void assertWrapperInstalled(File rootProjectDirectory) {
            val expectedWrapperFilesExists = Stream.of(new File(rootProjectDirectory, "gradlew"), new File(rootProjectDirectory, "gradlew.bat"), new File(rootProjectDirectory, "gradle/wrapper/gradle-wrapper.properties"), new File(rootProjectDirectory, "gradle/wrapper/gradle-wrapper.jar")).allMatch(File::exists);
            if (!expectedWrapperFilesExists) {
                throw new IllegalArgumentException(String.format("Invalid wrapper distribution at '%s'.", rootProjectDirectory.getAbsolutePath()));
            }
        }

        @Override
        public String getVersion() {
            val wrapperPropertiesFile = new File(rootProjectDirectory, "gradle/wrapper/gradle-wrapper.properties");
            try (val inStream = new FileInputStream(wrapperPropertiesFile)) {
                val wrapperProperties = new Properties();
                wrapperProperties.load(inStream);
                val distributionUrl = Optional.ofNullable(wrapperProperties.getProperty("distributionUrl")).orElseThrow(() -> new IllegalArgumentException(String.format("Unable to retrive 'distributionUrl' property from '%s'.", wrapperPropertiesFile.getAbsolutePath())));
                val matcher = DISTRIBUTION_URL_VERSION_PATTERN.matcher(distributionUrl);
                if (matcher.find()) {
                    return matcher.group(1);
                }
                throw new UnsupportedOperationException(String.format("Unsupported distribution URL format '%s'.", distributionUrl));
            } catch (FileNotFoundException e) {
                throw new IllegalArgumentException(String.format("Unable to locate gradle-wrapper.properties inside project '%s'.", rootProjectDirectory.getAbsolutePath()));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }
}
