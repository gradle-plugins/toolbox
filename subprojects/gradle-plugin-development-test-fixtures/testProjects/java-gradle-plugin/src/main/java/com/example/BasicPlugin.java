package com.example;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class BasicPlugin implements Plugin<Project> {
    public void apply(Project project) {
        project.getLogger().lifecycle("Hello");
    }
}
