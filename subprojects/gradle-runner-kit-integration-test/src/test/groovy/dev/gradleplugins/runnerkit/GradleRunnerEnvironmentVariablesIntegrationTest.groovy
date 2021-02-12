package dev.gradleplugins.runnerkit

import org.apache.commons.lang3.SystemUtils

abstract class GradleRunnerEnvironmentVariablesIntegrationTest extends BaseGradleRunnerIntegrationTest {

//    @CustomEnvironmentVariables
//    @NoDebug //avoid in-process execution so that we can set the env variable
    def "user can provide env vars"() {
        given:
        buildFile << "file('env.txt') << System.getenv('dummyEnvVar')"

        when:
        runner().withEnvironment(dummyEnvVar: "env var OK").build()

        then:
        file('env.txt').text == "env var OK"
    }

    def "uses current process environment variables"() {
        given:
        buildFile << '''
            |def env = new TreeMap(System.getenv().findAll { k, v -> !k.startsWith("JAVA_MAIN_CLASS") && !k.startsWith("APP_NAME") && !k.startsWith("APP_ICON") && !k == "OLDPWD" })
            |file("env.txt").text = env.collect { k, v -> "$k=$v" }.join("\\n")
            |'''.stripMargin()

        when:
        runner().build()

        then:
        def env = new TreeMap(System.getenv().findAll { k, v -> !k.startsWith("JAVA_MAIN_CLASS") && !k.startsWith("APP_NAME") && !k.startsWith("APP_ICON") && !k == "OLDPWD" })
        file("env.txt").text == env.collect { k, v -> "$k=$v" }.join("\n")
    }

//    @Debug
//    def "debug mode is not allowed with env vars"() {
//        when:
//        runner().withEnvironment(dummyEnvVar: "env var OK").build()
//
//        then:
//        def e = thrown(InvalidRunnerConfigurationException)
//        e.message == "Debug mode is not allowed when environment variables are specified. " +
//            "Debug mode runs 'in process' but we need to fork a separate process to pass environment variables. " +
//            "To run with debug mode, please remove environment variables."
//    }
}

class TestKitGradleRunnerEnvironmentVariablesIntegrationTest extends GradleRunnerEnvironmentVariablesIntegrationTest {
    @Override
    protected GradleRunner runner(String... arguments) {
        return GradleRunner.create(GradleExecutor.gradleTestKit()).inDirectory(testDirectory).withArguments(arguments)
    }
}

class WrapperGradleRunnerEnvironmentVariablesIntegrationTest extends GradleRunnerEnvironmentVariablesIntegrationTest implements GradleWrapperFixture {
    @Override
    protected GradleRunner runner(String... arguments) {
        writeGradleWrapperToTestDirectory(gradleVersion.version)
        return GradleRunner.create(GradleExecutor.gradleWrapper()).inDirectory(testDirectory).withArguments(arguments)
    }
}