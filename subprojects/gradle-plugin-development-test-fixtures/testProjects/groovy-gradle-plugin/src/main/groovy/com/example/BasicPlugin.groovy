package com.example

import org.gradle.api.Plugin;
import org.gradle.api.Project;

class BasicPlugin implements Plugin<Project> {
    void apply(Project project) {
        project.logger.lifecycle('Hello')
    }
}