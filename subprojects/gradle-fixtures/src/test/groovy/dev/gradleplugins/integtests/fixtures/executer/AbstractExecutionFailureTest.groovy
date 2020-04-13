//package dev.gradleplugins.integtests.fixtures.executer
//
//abstract class AbstractExecutionFailureTest extends AbstractExecutionTest {
//
//    @Override
//    protected ExecutionFailure run(GradleExecuter executer) {
//        return executer.runWithFailure()
//    }
//
//    def setup() {
//        buildFile << """
//            tasks.create('${taskNameUnderTest}').doLast { throw new GradleException() }
//        """
//    }
//}
