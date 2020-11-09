package dev.gradleplugins.test.fixtures.gradle.executer.internal.parameters;

import lombok.val;
import org.gradle.launcher.cli.DefaultCommandLineActionFactory;
import org.gradle.util.GradleVersion;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.Map;

@Deprecated
public final class WelcomeMessageParameter extends GradleExecutionParameterImpl<WelcomeMessage> implements JvmSystemPropertyParameter<WelcomeMessage>, GradleExecutionParameter<WelcomeMessage> {
    public void apply(GradleUserHomeDirectory gradleUserHomeDirectory, GradleVersion version) {
        if (get().equals(WelcomeMessage.ENABLED)) {
            val welcomeMessageFile = gradleUserHomeDirectory.file("notifications/" + version.getVersion() + "/release-features.rendered");
            welcomeMessageFile.delete();
        } else {
            val welcomeMessageFile = gradleUserHomeDirectory.file("notifications/" + version.getVersion() + "/release-features.rendered");
            try {
                welcomeMessageFile.touch();
            } catch (IOException e) {
                throw new UncheckedIOException("Could not ensure render message is properly rendered", e);
            }
        }
    }

    public static WelcomeMessageParameter enabled() {
        return fixed(WelcomeMessageParameter.class, WelcomeMessage.ENABLED);
    }

    public static WelcomeMessageParameter disabled() {
        return fixed(WelcomeMessageParameter.class, WelcomeMessage.DISABLED);
    }

    @Override
    public Map<String, String> getAsJvmSystemProperties() {
        if (get().equals(WelcomeMessage.ENABLED)) {
            return Collections.singletonMap(DefaultCommandLineActionFactory.WELCOME_MESSAGE_ENABLED_SYSTEM_PROPERTY, Boolean.TRUE.toString());
        }
        return Collections.singletonMap(DefaultCommandLineActionFactory.WELCOME_MESSAGE_ENABLED_SYSTEM_PROPERTY, Boolean.FALSE.toString());
    }
}
