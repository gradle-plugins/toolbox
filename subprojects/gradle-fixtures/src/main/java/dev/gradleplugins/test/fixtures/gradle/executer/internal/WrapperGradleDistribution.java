package dev.gradleplugins.test.fixtures.gradle.executer.internal;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import dev.gradleplugins.test.fixtures.file.TestFile;
import dev.gradleplugins.test.fixtures.gradle.executer.GradleExecuter;
import dev.gradleplugins.test.fixtures.internal.FilePreconditions;
import lombok.val;
import org.gradle.util.GradleVersion;
import org.hamcrest.Matchers;
import org.junit.Assert;

import java.io.*;
import java.util.Optional;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class WrapperGradleDistribution extends AbstractGradleDistribution {
    private static final Pattern DISTRIBUTION_URL_VERSION_PATTERN = Pattern.compile("(\\d+.\\d+(.\\d+)?(-rc-\\d)?)");
    private final File rootProjectDirectory;

    public WrapperGradleDistribution(File rootProjectDirectory) {
        FilePreconditions.checkIsDirectory(rootProjectDirectory, "Invalid project directory");
        assertWrapperInstalled(rootProjectDirectory);
        this.rootProjectDirectory = rootProjectDirectory;
    }

    private void assertWrapperInstalled(File rootProjectDirectory) {
        try {
            val expectedWrapperFiles = ImmutableSet.of(new File(rootProjectDirectory, "gradlew"), new File(rootProjectDirectory, "gradlew.bat"), new File(rootProjectDirectory, "gradle/wrapper/gradle-wrapper.properties"), new File(rootProjectDirectory, "gradle/wrapper/gradle-wrapper.jar"));
            val actualWrapperFiles = expectedWrapperFiles.stream().filter(File::exists).collect(Collectors.toSet());
            Assert.assertThat(actualWrapperFiles, Matchers.equalTo(expectedWrapperFiles));
        } catch (AssertionError ex) {
            throw new IllegalArgumentException(String.format("Invalid wrapper distribution at '%s'.", rootProjectDirectory.getAbsolutePath()), ex);
        }
    }

    @Override
    public TestFile getGradleHomeDirectory() {
        throw new UnsupportedOperationException();
    }

    @Override
    public TestFile getBinaryDirectory() {
        throw new UnsupportedOperationException();
    }

    @Override
    public GradleVersion getVersion() {
        val wrapperPropertiesFile = new File(rootProjectDirectory, "gradle/wrapper/gradle-wrapper.properties");
        try (val inStream = new FileInputStream(wrapperPropertiesFile)) {
            val wrapperProperties = new Properties();
            wrapperProperties.load(inStream);
            val distributionUrl = Optional.ofNullable(wrapperProperties.getProperty("distributionUrl")).orElseThrow(() -> new IllegalArgumentException(String.format("Unable to retrive 'distributionUrl' property from '%s'.", wrapperPropertiesFile.getAbsolutePath())));
            val matcher = DISTRIBUTION_URL_VERSION_PATTERN.matcher(distributionUrl);
            if (matcher.find()) {
                return GradleVersion.version(matcher.group(1));
            }
            throw new UnsupportedOperationException(String.format("Unsupported distribution URL format '%s'.", distributionUrl));
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException(String.format("Unable to locate gradle-wrapper.properties inside project '%s'.", rootProjectDirectory.getAbsolutePath()));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public GradleExecuter executer(TestFile testDirectoryProvider) {
        return null;
    }
}
