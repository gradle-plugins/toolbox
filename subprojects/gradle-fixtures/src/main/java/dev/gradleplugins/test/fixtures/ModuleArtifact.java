package dev.gradleplugins.test.fixtures;

import java.net.URI;

public interface ModuleArtifact {
    /**
     * Returns the path of this artifact relative to the root of the repository.
     */
    String getRelativePath();

    /**
     * Returns the local backing file of this artifact.
     */
    URI getUri();

    /**
     * Returns the name of this artifact
     *
     * This will differ from file.name only for Maven unique snapshots
     */
    String getName();
}
