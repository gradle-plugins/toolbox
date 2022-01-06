package dev.gradleplugins.internal;

import org.gradle.api.artifacts.Dependency;

public interface DependencyFactory {
    Dependency create(Object notation);
}
