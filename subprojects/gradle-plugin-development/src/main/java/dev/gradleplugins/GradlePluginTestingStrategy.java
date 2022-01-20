package dev.gradleplugins;

import org.gradle.api.Named;
import org.gradle.api.UnknownDomainObjectException;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.testing.Test;

/**
 * A testing strategy for a Gradle plugin project test suite.
 *
 * @see GradlePluginDevelopmentTestSuite
 */
public interface GradlePluginTestingStrategy extends Named {
    /**
     * Type-safe accessor to the {@literal testingStrategy} extension on {@code Test} task.
     *
     * @param task  the test task, must not be null
     * @return a {@code Provider} for the testing strategy of the specified {@code Test} task.
     * @throws UnknownDomainObjectException when {@literal testingStrategy} extension is not present
     */
    @SuppressWarnings("unchecked")
    static Provider<GradlePluginTestingStrategy> testingStrategy(Test task) throws UnknownDomainObjectException {
        return (Provider<GradlePluginTestingStrategy>) task.getExtensions().getByName("testingStrategy");
    }
}
