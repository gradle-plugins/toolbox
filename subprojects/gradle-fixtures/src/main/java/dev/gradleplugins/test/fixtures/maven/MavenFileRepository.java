package dev.gradleplugins.test.fixtures.maven;

import dev.gradleplugins.test.fixtures.ModuleArtifact;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

public class MavenFileRepository implements MavenRepository {
    private final Path rootDirectory;

    public MavenFileRepository(Path rootDirectory) {
        this.rootDirectory = rootDirectory;
    }

    public URI getUri() {
        return rootDirectory.toUri();
    }

    @Override
    public MavenModule module(String groupId, String artifactId, String version) {
        return new MavenModule() {
            @Override
            public boolean isPublished() {
                return Files.exists(rootDirectory.resolve(groupId.replace('.', '/')).resolve(artifactId).resolve(version));
            }

            @Override
            public ModuleArtifact getPom() {
                return new ModuleArtifact() {
                    @Override
                    public String getRelativePath() {
                        return groupId.replace('.', '/') + "/" + artifactId + "/" + version + "/" + artifactId + "-" + version + ".pom";
                    }

                    @Override
                    public URI getUri() {
                        return rootDirectory.resolve(getRelativePath()).toUri();
                    }

                    @Override
                    public String getName() {
                        return "pom-" + version + ".xml";
                    }
                };
            }

            @Override
            public String getGroupId() {
                return groupId;
            }

            @Override
            public String getArtifactId() {
                return artifactId;
            }

            @Override
            public String getVersion() {
                return version;
            }
        };
    }
}
