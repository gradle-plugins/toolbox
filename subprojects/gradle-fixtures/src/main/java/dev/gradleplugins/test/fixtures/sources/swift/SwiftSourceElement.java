package dev.gradleplugins.test.fixtures.sources.swift;

import dev.gradleplugins.test.fixtures.sources.SourceElement;
import org.apache.commons.lang3.StringUtils;

public abstract class SwiftSourceElement extends SourceElement {
    private final String projectName;

    public SwiftSourceElement(String projectName) {
        this.projectName = projectName;
    }

    public String getModuleName() {
        return StringUtils.capitalize(projectName);
    }

    public final String getProjectName() {
        return projectName;
    }
}
