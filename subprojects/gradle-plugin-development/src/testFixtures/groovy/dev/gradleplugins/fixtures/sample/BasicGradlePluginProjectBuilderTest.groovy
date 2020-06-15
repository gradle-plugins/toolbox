package dev.gradleplugins.fixtures.sample

import dev.gradleplugins.test.fixtures.sources.SourceElement
import dev.gradleplugins.test.fixtures.sources.SourceFile
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class BasicGradlePluginProjectBuilderTest extends SourceElement {
    @Override
    List<SourceFile> getFiles() {
        return Collections.singletonList(sourceFile('groovy', 'com/example/BasicPluginTest.groovy', """package com.example

import ${ProjectBuilder.canonicalName}
import ${Specification.canonicalName}

class BasicPluginTest extends ${Specification.simpleName} {
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
