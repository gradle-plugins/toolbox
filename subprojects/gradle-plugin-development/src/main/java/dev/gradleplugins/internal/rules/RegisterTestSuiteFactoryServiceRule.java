package dev.gradleplugins.internal.rules;

import dev.gradleplugins.GradlePluginDevelopmentTestSuite;
import dev.gradleplugins.GradlePluginDevelopmentTestSuiteFactory;
import dev.gradleplugins.internal.AttachTestTasksToCheckTaskIfPresent;
import dev.gradleplugins.internal.ConfigurePluginUnderTestMetadataTask;
import dev.gradleplugins.internal.DefaultGradlePluginDevelopmentTestSuiteFactory;
import dev.gradleplugins.internal.PluginUnderTestMetadataConfigurationSupplier;
import dev.gradleplugins.internal.TestSuiteSourceSetExtendsFromTestedSourceSetIfPresentRule;
import org.gradle.api.Action;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.Project;

import java.util.Set;

public final class RegisterTestSuiteFactoryServiceRule implements Action<Project> {
    @Override
    public void execute(Project project) {
        final DomainObjectSet<GradlePluginDevelopmentTestSuite> testSuites = project.getObjects().domainObjectSet(GradlePluginDevelopmentTestSuite.class);
        project.getExtensions().add(GradlePluginDevelopmentTestSuiteFactory.class, "testSuiteFactory", new CapturingGradlePluginDevelopmentTestSuiteFactory(testSuites, new DefaultGradlePluginDevelopmentTestSuiteFactory(project)));

        project.afterEvaluate(__ -> {
            testSuites.configureEach(new FinalizeTestSuiteProperties());
            testSuites.configureEach(testSuite -> new PluginUnderTestMetadataConfigurationSupplier(project, testSuite).get());
            testSuites.configureEach(new TestSuiteSourceSetExtendsFromTestedSourceSetIfPresentRule());
            testSuites.configureEach(new AttachTestTasksToCheckTaskIfPresent(project.getPluginManager(), project.getTasks()));

            // Register as finalized action because it adds configuration which early finalize source set property
            testSuites.configureEach(new ConfigurePluginUnderTestMetadataTask(project));
        });
    }

    private static final class CapturingGradlePluginDevelopmentTestSuiteFactory implements GradlePluginDevelopmentTestSuiteFactory {
        private final Set<GradlePluginDevelopmentTestSuite> testSuites;
        private final GradlePluginDevelopmentTestSuiteFactory delegate;

        CapturingGradlePluginDevelopmentTestSuiteFactory(Set<GradlePluginDevelopmentTestSuite> testSuites, GradlePluginDevelopmentTestSuiteFactory delegate) {
            this.testSuites = testSuites;
            this.delegate = delegate;
        }

        @Override
        public GradlePluginDevelopmentTestSuite create(String name) {
            final GradlePluginDevelopmentTestSuite result = delegate.create(name);
            testSuites.add(result);
            return result;
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    public static final class FinalizeTestSuiteProperties implements Action<GradlePluginDevelopmentTestSuite> {
        @Override
        public void execute(GradlePluginDevelopmentTestSuite testSuite) {
            testSuite.getTestedSourceSet().disallowChanges();
            testSuite.getSourceSet().disallowChanges();
            testSuite.getTestingStrategies().disallowChanges();
        }
    }
}
