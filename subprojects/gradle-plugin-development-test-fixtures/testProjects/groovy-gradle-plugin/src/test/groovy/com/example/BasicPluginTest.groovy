package com.example

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