package dev.gradleplugins.runnerkit;

import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@EqualsAndHashCode
public final class BuildFailure implements BuildResult.Failure {
    private final String description;
    private final List<String> causes;

    BuildFailure(String description, List<String> causes) {
        this.description = description;
        this.causes = causes;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public List<String> getCauses() {
        return causes;
    }

    public static BuildFailure describedBy(String description) {
        return new BuildFailure(description, Collections.emptyList());
    }

    public BuildFailure causedBy(String cause) {
        List<String> causes = new ArrayList<>();
        causes.addAll(this.causes);
        causes.add(cause);
        return new BuildFailure(description, Collections.unmodifiableList(causes));
    }
}
