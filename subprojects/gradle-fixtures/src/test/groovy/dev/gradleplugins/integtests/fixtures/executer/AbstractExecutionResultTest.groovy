//package dev.gradleplugins.integtests.fixtures.executer
//
//abstract class AbstractExecutionResultTest extends AbstractExecutionTest {
//    @Override
//    protected ExecutionResult run(GradleExecuter executer) {
//        return executer.run()
//    }
//
//    def setup() {
//        buildFile << """
//            tasks.create('${taskNameUnderTest}')
//        """
//    }
//}
