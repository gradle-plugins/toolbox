package dev.gradleplugins.internal.rules;

import dev.gradleplugins.GradlePluginDevelopmentTestSuite;
import dev.gradleplugins.GradlePluginDevelopmentTestSuiteFactory;
import dev.gradleplugins.GradlePluginDevelopmentTestingExtension;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.component.SoftwareComponent;
import org.gradle.api.component.SoftwareComponentContainer;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.reflect.HasPublicType;
import org.gradle.api.reflect.TypeOf;
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension;

public final class RegisterTestingExtensionOnGradleDevelExtensionRule implements Action<Project> {
    @Override
    public void execute(Project project) {
        val gradlePluginExtension = project.getExtensions().getByType(GradlePluginDevelopmentExtension.class);
        ((ExtensionAware) gradlePluginExtension).getExtensions().add("testing", new DefaultGradlePluginDevelopmentTestingExtension(GradlePluginDevelopmentTestSuiteFactory.forProject(project), project.getComponents()));
    }

    static final class DefaultGradlePluginDevelopmentTestingExtension implements GradlePluginDevelopmentTestingExtension, HasPublicType {
        private final GradlePluginDevelopmentTestSuiteFactory factory;
        private final SoftwareComponentContainer components;

        public DefaultGradlePluginDevelopmentTestingExtension(GradlePluginDevelopmentTestSuiteFactory factory, SoftwareComponentContainer components) {
            this.factory = factory;
            this.components = components;
        }

        @Override
        public GradlePluginDevelopmentTestSuite registerSuite(String name) {
            val result = factory.create(name);
            components.add((SoftwareComponent) result);
            return result;
        }

        @Override
        public TypeOf<?> getPublicType() {
            return TypeOf.typeOf(GradlePluginDevelopmentTestingExtension.class);
        }
    }
}
