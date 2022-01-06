package dev.gradleplugins.internal;

import org.gradle.api.Action;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.provider.Provider;

public final class AddDependency implements Action<Configuration> {
    private final Object notation;
    private final DependencyFactory factory;

    public AddDependency(Object notation, DependencyFactory factory) {
        this.notation = notation;
        this.factory = factory;
    }

    @Override
    public void execute(Configuration configuration) {
        if (notation instanceof Provider) {
            configuration.getDependencies().addLater(((Provider<?>) notation).map(factory::create));
        } else {
            configuration.getDependencies().add(factory.create(notation));
        }
    }
}
