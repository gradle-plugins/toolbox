package dev.gradleplugins.fixtures.gradle.runner.parameters;

import dev.gradleplugins.fixtures.file.FileSystemUtils;
import dev.gradleplugins.fixtures.gradle.runner.GradleExecutionContext;
import lombok.val;

import java.util.Map;

import static dev.gradleplugins.fixtures.file.FileSystemUtils.file;
import static java.util.Collections.singletonMap;
import static org.gradle.launcher.cli.DefaultCommandLineActionFactory.WELCOME_MESSAGE_ENABLED_SYSTEM_PROPERTY;

public enum WelcomeMessage implements GradleExecutionJvmSystemPropertyParameter<WelcomeMessage>, BeforeExecute {
    ENABLED(singletonMap(WELCOME_MESSAGE_ENABLED_SYSTEM_PROPERTY, Boolean.TRUE.toString())),
    DISABLED(singletonMap(WELCOME_MESSAGE_ENABLED_SYSTEM_PROPERTY, Boolean.FALSE.toString()));

    private final Map<String, String> properties;

    WelcomeMessage(Map<String, String> properties) {
        this.properties = properties;
    }

    @Override
    public Map<String, String> getAsJvmSystemProperties() {
        return properties;
    }

    @Override
    public void accept(GradleExecutionContext parameters) {
        if (this.equals(WelcomeMessage.ENABLED)) {
            val welcomeMessageFile = file(parameters.getGradleUserHomeDirectory().get(), "notifications/" + parameters.getDistribution().get().getVersion() + "/release-features.rendered");
            welcomeMessageFile.delete();
        } else {
            val welcomeMessageFile = file(parameters.getGradleUserHomeDirectory().get(), "notifications/" + parameters.getDistribution().get().getVersion() + "/release-features.rendered");
            FileSystemUtils.touch(welcomeMessageFile);
        }
    }
}
