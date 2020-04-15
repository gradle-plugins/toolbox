package dev.gradleplugins

import dev.gradleplugins.integtests.fixtures.AbstractGradleSpecification

class AbstractGradlePluginDevelopmentFunctionalSpec extends AbstractGradleSpecification {
    def setup() {
        executer = executer.beforeExecute {
            buildFile << """
                allprojects {
                    repositories {
                        maven { url = '${System.getProperty('localRepository')}' }
                    }
                }
            """
        }
    }
}
