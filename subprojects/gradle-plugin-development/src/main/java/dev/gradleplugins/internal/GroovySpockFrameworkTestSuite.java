package dev.gradleplugins.internal;

import org.gradle.api.component.SoftwareComponent;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.SourceSet;

import javax.inject.Inject;

public abstract class GroovySpockFrameworkTestSuite implements SoftwareComponent {
    private String name;

    @Inject
    public GroovySpockFrameworkTestSuite(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    public abstract Property<SourceSet> getTestedSourceSet();

    public abstract Property<String> getSpockVersion();
}
