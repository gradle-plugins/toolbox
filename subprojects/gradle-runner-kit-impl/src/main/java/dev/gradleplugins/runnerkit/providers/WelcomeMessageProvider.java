package dev.gradleplugins.runnerkit.providers;

import dev.gradleplugins.fixtures.file.FileSystemUtils;
import dev.gradleplugins.runnerkit.GradleExecutionContext;
import dev.gradleplugins.runnerkit.InvalidRunnerConfigurationException;
import dev.gradleplugins.runnerkit.distributions.VersionAwareGradleDistribution;
import lombok.val;

import java.util.Map;

import static java.util.Collections.singletonMap;

public final class WelcomeMessageProvider extends AbstractGradleExecutionProvider<GradleExecutionContext.WelcomeMessage> implements GradleExecutionJvmSystemPropertyProvider, BeforeExecute {
    // See org.gradle.launcher.cli.DefaultCommandLineActionFactory#WELCOME_MESSAGE_ENABLED_SYSTEM_PROPERTY
    static final String WELCOME_MESSAGE_ENABLED_SYSTEM_PROPERTY = "org.gradle.internal.launcher.welcomeMessageEnabled";

    private static final String WELCOME_MESSAGE_ENABLED_FLAG = "-D" + WELCOME_MESSAGE_ENABLED_SYSTEM_PROPERTY + "=true";
    private static final String WELCOME_MESSAGE_DISABLED_FLAG = "-D" + WELCOME_MESSAGE_ENABLED_SYSTEM_PROPERTY + "=false";
    public static WelcomeMessageProvider enabled() {
        return fixed(WelcomeMessageProvider.class, GradleExecutionContext.WelcomeMessage.ENABLED);
    }

    public static WelcomeMessageProvider disabled() {
        return fixed(WelcomeMessageProvider.class, GradleExecutionContext.WelcomeMessage.DISABLED);
    }

    @Override
    public Map<String, String> getAsJvmSystemProperties() {
        if (get().equals(GradleExecutionContext.WelcomeMessage.ENABLED)) {
            return singletonMap(WELCOME_MESSAGE_ENABLED_SYSTEM_PROPERTY, Boolean.TRUE.toString());
        }
        return singletonMap(WELCOME_MESSAGE_ENABLED_SYSTEM_PROPERTY, Boolean.FALSE.toString());
    }

    @Override
    public void accept(GradleExecutionContext parameters) {
        if (parameters.getDistribution().get() instanceof VersionAwareGradleDistribution) {
            if (get().equals(GradleExecutionContext.WelcomeMessage.ENABLED)) {
                val welcomeMessageFile = FileSystemUtils.file(parameters.getGradleUserHomeDirectory().get(), "notifications/" + ((VersionAwareGradleDistribution) parameters.getDistribution().get()).getVersion() + "/release-features.rendered");
                welcomeMessageFile.delete();
            } else {
                val welcomeMessageFile = FileSystemUtils.file(parameters.getGradleUserHomeDirectory().get(), "notifications/" + ((VersionAwareGradleDistribution) parameters.getDistribution().get()).getVersion() + "/release-features.rendered");
                FileSystemUtils.touch(welcomeMessageFile);
            }
        }
    }

    @Override
    public void validate(GradleExecutionContext context) {
        if (get().equals(GradleExecutionContext.WelcomeMessage.ENABLED)) {
            if (context.getArguments().get().contains(WELCOME_MESSAGE_ENABLED_FLAG)) {
                throw new InvalidRunnerConfigurationException("Please remove command line flag enabling welcome message as the it was already enabled via GradleRunner#withWelcomeMessageEnabled().");
            } else if (context.getArguments().get().contains(WELCOME_MESSAGE_DISABLED_FLAG)) {
                throw new InvalidRunnerConfigurationException("Please remove command line flag disabling welcome message and any call to GradleRunner#withWelcomeMessageEnabled() for this runner as it is disabled by default for all toolbox runner.");
            }
        } else if (get().equals(GradleExecutionContext.WelcomeMessage.DISABLED)) {
            if (context.getArguments().get().contains(WELCOME_MESSAGE_ENABLED_FLAG)) {
                throw new InvalidRunnerConfigurationException("Please use GradleRunner#withWelcomeMessageEnabled() instead of using flag in command line arguments.");
            } else if (context.getArguments().get().contains(WELCOME_MESSAGE_DISABLED_FLAG)) {
                throw new InvalidRunnerConfigurationException("Please remove command line flag disabling welcome message as the it is disabled by default for all toolbox runner.");
            }
        }
    }
}
