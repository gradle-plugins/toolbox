package dev.gradleplugins.internal;

import javax.inject.Inject;

public abstract class GroovyGradlePluginSpockTestSuite extends GroovySpockFrameworkTestSuite {
    @Inject
    public GroovyGradlePluginSpockTestSuite(String name) {
        super(name);
    }
}
