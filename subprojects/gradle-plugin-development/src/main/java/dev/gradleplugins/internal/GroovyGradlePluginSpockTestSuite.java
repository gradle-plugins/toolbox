package dev.gradleplugins.internal;

import org.gradle.api.component.SoftwareComponent;

import javax.inject.Inject;

public class GroovyGradlePluginSpockTestSuite implements SoftwareComponent {
    private String name;

    @Inject
    public GroovyGradlePluginSpockTestSuite(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}
