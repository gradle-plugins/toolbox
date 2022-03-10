package dev.gradleplugins.fixtures.sample

import dev.gradleplugins.fixtures.sources.SourceElement
import dev.gradleplugins.fixtures.sources.SourceFile

class BasicGradlePluginProjectBuilderTest extends SourceElement {
    @Override
    List<SourceFile> getFiles() {
        return Collections.singletonList(sourceFile('groovy', 'com/example/BasicPluginTest.groovy', """package com.example
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class BasicPluginTest extends Specification {
    def "can do basic test"() {
        given:
        def project = ProjectBuilder.builder().build()

        when:
        project.apply plugin: BasicPlugin

        then:
        noExceptionThrown()
    }
}
"""))
    }

    @Override
    String getSourceSetName() {
        return "test"
    }
}
