package dev.gradleplugins

import dev.gradleplugins.integtests.fixtures.AbstractGradleSpecification

class AbstractGradlePluginDevelopmentFunctionalSpec extends AbstractGradleSpecification {
    def setup() {
        executer = executer.beforeExecute {
            // NOTE: The script is written to be Groovy/Kotlin DSL compatible
            buildFile << """
                allprojects {
                    repositories {
                        maven { url = uri("${System.getProperty('localRepository').replace('\\', '/')}") }
                    }
                }
            """
            it
        }
    }
}
