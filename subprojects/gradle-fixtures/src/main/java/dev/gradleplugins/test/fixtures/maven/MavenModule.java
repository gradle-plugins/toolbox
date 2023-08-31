package dev.gradleplugins.test.fixtures.maven;

import dev.gradleplugins.test.fixtures.ModuleArtifact;

public interface MavenModule {
    boolean isPublished();

    ModuleArtifact getPom();

    String getGroupId();

    String getArtifactId();

    String getVersion();
}
