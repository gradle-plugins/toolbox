package dev.gradleplugins

import dev.gradleplugins.integtests.fixtures.AbstractGradleSpecification

class AbstractGradlePluginDevelopmentFunctionalSpec extends AbstractGradleSpecification {
    protected boolean localRepositoryInjectionEnabled = true

    def setup() {
        executer = executer.beforeExecute {
            if (localRepositoryInjectionEnabled) {
                // NOTE: The script is written to be Groovy/Kotlin DSL compatible
                buildFile << """
                    allprojects {
                        repositories {
                            maven { url = uri("${System.getProperty('localRepository').replace('\\', '/')}") }
                        }
                    }
                """
            }
            it
        }
    }

    protected void disableLocalRepositoryInjection() {
        localRepositoryInjectionEnabled = false
    }
}
