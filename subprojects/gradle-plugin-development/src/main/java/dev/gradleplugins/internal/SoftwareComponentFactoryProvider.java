package dev.gradleplugins.internal;

import org.gradle.api.component.SoftwareComponentFactory;

import javax.inject.Inject;

public class SoftwareComponentFactoryProvider {
    private final SoftwareComponentFactory instance;

    @Inject
    public SoftwareComponentFactoryProvider(SoftwareComponentFactory instance) {
        this.instance = instance;
    }

    public SoftwareComponentFactory get() {
        return instance;
    }
}
