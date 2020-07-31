package dev.gradleplugins.test.fixtures.gradle.executer.internal.parameters;

import lombok.Value;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.gradle.launcher.cli.DefaultCommandLineActionFactory;
import org.gradle.util.GradleVersion;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.Map;

public interface WelcomeMessageParameter extends JvmSystemPropertyParameter {
    void apply(GradleUserHomeDirectoryParameter gradleUserHomeDirectory, GradleVersion version);

    static WelcomeMessageParameter enabled() {
        return new ShowingWelcomeMessageParameter();
    }

    static WelcomeMessageParameter disabled() {
        return new HidingWelcomeMessageParameter();
    }

    @Value
    class ShowingWelcomeMessageParameter implements WelcomeMessageParameter {
        @Override
        public Map<String, String> getAsJvmSystemProperties() {
            return Collections.singletonMap(DefaultCommandLineActionFactory.WELCOME_MESSAGE_ENABLED_SYSTEM_PROPERTY, Boolean.TRUE.toString());
        }

        @Override
        public void apply(GradleUserHomeDirectoryParameter gradleUserHomeDirectory, GradleVersion version) {
            val welcomeMessageFile = gradleUserHomeDirectory.file("notifications/" + version.getVersion() + "/release-features.rendered");
            welcomeMessageFile.delete();
        }
    }

    @Value
    class HidingWelcomeMessageParameter implements WelcomeMessageParameter {
        @Override
        public Map<String, String> getAsJvmSystemProperties() {
            return Collections.singletonMap(DefaultCommandLineActionFactory.WELCOME_MESSAGE_ENABLED_SYSTEM_PROPERTY, Boolean.FALSE.toString());
        }

        @Override
        public void apply(GradleUserHomeDirectoryParameter gradleUserHomeDirectory, GradleVersion version) {
            val welcomeMessageFile = gradleUserHomeDirectory.file("notifications/" + version.getVersion() + "/release-features.rendered");
            try {
                FileUtils.touch(welcomeMessageFile);
            } catch (IOException e) {
                throw new UncheckedIOException("Could not ensure render message is properly rendered", e);
            }
        }
    }
}
