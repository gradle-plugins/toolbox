package dev.gradleplugins.integtests.fixtures.executer


import spock.lang.Subject

@Subject(GradleRunnerExecuter)
class GradleRunnerExecuterTest extends AbstractGradleExecuterTest {
    GradleExecuter executerUnderTest = new GradleRunnerExecuter(temporaryFolder)
}
