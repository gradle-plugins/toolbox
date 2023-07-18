package dev.gradleplugins.internal;

import org.gradle.api.Task;
import org.gradle.api.artifacts.PublishArtifact;
import org.gradle.api.artifacts.type.ArtifactTypeDefinition;
import org.gradle.api.tasks.TaskDependency;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.jvm.tasks.Jar;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collections;
import java.util.Date;
import java.util.Set;

public final class JarBasedPublishArtifact implements PublishArtifact {
    private final TaskProvider<Jar> jarTaskProvider;

    public JarBasedPublishArtifact(TaskProvider<Jar> jarTaskProvider) {
        this.jarTaskProvider = jarTaskProvider;
    }

    @Override
    public String getName() {
        return jarTaskProvider.getName();
    }

    @Override
    public String getExtension() {
        return "jar";
    }

    @Override
    public String getType() {
        return ArtifactTypeDefinition.JAR_TYPE;
    }

    @Nullable
    @Override
    public String getClassifier() {
        return jarTaskProvider.get().getArchiveClassifier().getOrNull();
    }

    @Override
    public File getFile() {
        return jarTaskProvider.get().getArchiveFile().get().getAsFile();
    }

    @Nullable
    @Override
    public Date getDate() {
        return null;
    }

    @Override
    public TaskDependency getBuildDependencies() {
        return new TaskDependency() {
            @Override
            public Set<? extends Task> getDependencies(@Nullable Task task) {
                return Collections.singleton(jarTaskProvider.get());
            }
        };
    }
}
