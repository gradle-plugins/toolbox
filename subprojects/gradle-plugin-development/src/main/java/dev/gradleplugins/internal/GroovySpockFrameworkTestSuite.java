package dev.gradleplugins.internal;

import org.gradle.api.component.SoftwareComponent;

import javax.inject.Inject;

public class GroovySpockFrameworkTestSuite implements SoftwareComponent {
    private String name;

    @Inject
    public GroovySpockFrameworkTestSuite(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}
