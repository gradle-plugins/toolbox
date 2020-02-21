package dev.gradleplugins

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.javadoc.Groovydoc
import org.gradle.api.tasks.javadoc.Javadoc

class AdditionalArtifactsPlugin implements Plugin<Project> {
    void apply(Project project) {
        def sourceSets = project.sourceSets
        def javadoc = project.tasks.named("javadoc", Javadoc)

        project.tasks.create("sourcesJar", Jar) {
            archiveClassifier.set("sources")
            from(sourceSets.main.allSource)
        }

        // TODO: React to the plugin instead, this is order dependent and bad
        if (project.pluginManager.hasPlugin("groovy")) {
            def groovydoc = project.tasks.named("groovydoc", Groovydoc)
            project.tasks.create("groovydocJar", Jar) {
                dependsOn(groovydoc)
                archiveClassifier.set("groovydoc")
                from(groovydoc.get().destinationDir)
            }
        }

        project.tasks.create("javadocJar", Jar) {
            dependsOn(javadoc)
            archiveClassifier.set("javadoc")
            from(javadoc.get().destinationDir)
        }
    }
}