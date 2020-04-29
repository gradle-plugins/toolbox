package dev.gradleplugins.internal;

import dev.gradleplugins.GradlePluginSpockFrameworkTestSuite;
import dev.gradleplugins.GradlePluginTestingStrategyFactory;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.gradle.api.component.SoftwareComponent;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskContainer;

import javax.inject.Inject;

@RequiredArgsConstructor(onConstructor_={@Inject})
public abstract class GradlePluginSpockFrameworkTestSuiteInternal implements GradlePluginSpockFrameworkTestSuite, SoftwareComponent {
    private final GradlePluginTestingStrategyFactory strategyFactory = getObjects().newInstance(GradlePluginTestingStrategyFactoryInternal.class);
    @Getter private final String name;
    @Getter private final SourceSet sourceSet;

    @Inject
    protected abstract ObjectFactory getObjects();

    @Inject
    protected abstract TaskContainer getTasks();

    @Override
    public GradlePluginTestingStrategyFactory getStrategies() {
        return strategyFactory;
    }

    public abstract Property<GradlePluginDevelopmentExtensionInternal> getTestedGradlePlugin();
}
