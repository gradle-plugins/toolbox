package dev.gradleplugins.internal;

import javax.inject.Inject;

public class GroovyGradlePluginSpockTestSuite extends GroovySpockFrameworkTestSuite {
    @Inject
    public GroovyGradlePluginSpockTestSuite(String name) {
        super(name);
    }
}
