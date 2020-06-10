package dev.gradleplugins.internal;

import dev.gradleplugins.GroovyGradlePluginDevelopmentExtension;
import dev.gradleplugins.JavaGradlePluginDevelopmentExtension;
import org.gradle.api.plugins.JavaPluginExtension;

import javax.inject.Inject;

public abstract class GradlePluginDevelopmentExtensionInternal implements GroovyGradlePluginDevelopmentExtension, JavaGradlePluginDevelopmentExtension {
    private final JavaPluginExtension java;

    @Inject
    public GradlePluginDevelopmentExtensionInternal(JavaPluginExtension java) {
        this.java = java;
    }

    @Override
    public void withSourcesJar() {
        java.withSourcesJar();
    }

    @Override
    public void withJavadocJar() {
        java.withJavadocJar();
    }
}
