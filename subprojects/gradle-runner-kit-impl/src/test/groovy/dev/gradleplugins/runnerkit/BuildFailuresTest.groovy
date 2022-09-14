package dev.gradleplugins.runnerkit

import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

import static dev.gradleplugins.runnerkit.BuildFailure.describedBy
import static dev.gradleplugins.runnerkit.BuildFailures.from
import static dev.gradleplugins.runnerkit.CommandLineToolLogContent.of

@Subject(BuildFailures)
class BuildFailuresTest extends Specification {
    @Unroll
    def "can extract single failure from output"(output) {
        expect:
        def failures = from(of(outputWithStacktrace))
        failures.get() == [describedBy("Execution failed for task ':foo'.").causedBy('Fail to execute')]
        failures.getSummary() == 'Build failed with an exception.'

        where:
        output << [outputWithStacktrace, outputWithoutStacktrace]
    }

    @Unroll
    def "same failure with and without stacktrace are equals"(outputLeft, outputRight) {
        expect:
        from(of(outputLeft)) == from(of(outputRight))

        where:
        [outputLeft, outputRight] << [[outputWithStacktrace, outputWithoutStacktrace], [outputWithMultipleFailuresAndNoStacktrace, outputWithMultipleFailuresAndStacktrace]]
    }

    @Unroll
    def "can extract multiple failures from output"(output) {
        expect:
        def failures = from(of(output))
        failures.get() == [
                describedBy("Execution failed for task ':foo'.")
                        .causedBy('Fail to execute'),
                describedBy("Execution failed for task ':bar:foo'.")
                        .causedBy('Fail to execute'),
                describedBy("Execution failed for task ':foo:foo'.")
                        .causedBy('Fail to execute'),
        ]
        failures.getSummary() == 'Build completed with 3 failures.'

        where:
        output << [outputWithMultipleFailuresAndNoStacktrace, outputWithMultipleFailuresAndStacktrace]
    }

    String getOutputWithoutStacktrace() {
        return '''> Task :foo FAILED
            |
            |FAILURE: Build failed with an exception.
            |
            |* Where:
            |Build file '/Users/daniel/gradle/tmp/build-result-test/build.gradle' line: 15
            |
            |* What went wrong:
            |Execution failed for task ':foo'.
            |> Fail to execute
            |
            |* Try:
            |Run with --stacktrace option to get the stack trace. Run with --info or --debug option to get more log output. Run with --scan to get full insights.
            |
            |* Get more help at https://help.gradle.org
            |
            |BUILD FAILED in 2s
            |1 actionable task: 1 executed
            |'''.stripMargin()
    }

    String getOutputWithStacktrace() {
        return '''> Task :foo FAILED
            |
            |FAILURE: Build failed with an exception.
            |
            |* Where:
            |Build file '/Users/daniel/gradle/tmp/build-result-test/build.gradle' line: 15
            |
            |* What went wrong:
            |Execution failed for task ':foo'.
            |> Fail to execute
            |
            |* Try:
            |Run with --info or --debug option to get more log output. Run with --scan to get full insights.
            |
            |* Exception is:
            |org.gradle.api.tasks.TaskExecutionException: Execution failed for task ':foo'.
            |        at org.gradle.api.internal.tasks.execution.ExecuteActionsTaskExecuter.lambda$executeIfValid$1(ExecuteActionsTaskExecuter.java:205)
            |        at org.gradle.internal.Try$Failure.ifSuccessfulOrElse(Try.java:263)
            |        at org.gradle.api.internal.tasks.execution.ExecuteActionsTaskExecuter.executeIfValid(ExecuteActionsTaskExecuter.java:203)
            |        at org.gradle.api.internal.tasks.execution.ExecuteActionsTaskExecuter.execute(ExecuteActionsTaskExecuter.java:184)
            |        at org.gradle.api.internal.tasks.execution.CleanupStaleOutputsExecuter.execute(CleanupStaleOutputsExecuter.java:114)
            |        at org.gradle.api.internal.tasks.execution.FinalizePropertiesTaskExecuter.execute(FinalizePropertiesTaskExecuter.java:46)
            |        at org.gradle.api.internal.tasks.execution.ResolveTaskExecutionModeExecuter.execute(ResolveTaskExecutionModeExecuter.java:62)
            |        at org.gradle.api.internal.tasks.execution.SkipTaskWithNoActionsExecuter.execute(SkipTaskWithNoActionsExecuter.java:57)
            |        at org.gradle.api.internal.tasks.execution.SkipOnlyIfTaskExecuter.execute(SkipOnlyIfTaskExecuter.java:56)
            |        at org.gradle.api.internal.tasks.execution.CatchExceptionTaskExecuter.execute(CatchExceptionTaskExecuter.java:36)
            |        at org.gradle.api.internal.tasks.execution.EventFiringTaskExecuter$1.executeTask(EventFiringTaskExecuter.java:77)
            |        at org.gradle.api.internal.tasks.execution.EventFiringTaskExecuter$1.call(EventFiringTaskExecuter.java:55)
            |        at org.gradle.api.internal.tasks.execution.EventFiringTaskExecuter$1.call(EventFiringTaskExecuter.java:52)
            |        at org.gradle.internal.operations.DefaultBuildOperationExecutor$CallableBuildOperationWorker.execute(DefaultBuildOperationExecutor.java:416)
            |        at org.gradle.internal.operations.DefaultBuildOperationExecutor$CallableBuildOperationWorker.execute(DefaultBuildOperationExecutor.java:406)
            |        at org.gradle.internal.operations.DefaultBuildOperationExecutor$1.execute(DefaultBuildOperationExecutor.java:165)
            |        at org.gradle.internal.operations.DefaultBuildOperationExecutor.execute(DefaultBuildOperationExecutor.java:250)
            |        at org.gradle.internal.operations.DefaultBuildOperationExecutor.execute(DefaultBuildOperationExecutor.java:158)
            |        at org.gradle.internal.operations.DefaultBuildOperationExecutor.call(DefaultBuildOperationExecutor.java:102)
            |        at org.gradle.internal.operations.DelegatingBuildOperationExecutor.call(DelegatingBuildOperationExecutor.java:36)
            |        at org.gradle.api.internal.tasks.execution.EventFiringTaskExecuter.execute(EventFiringTaskExecuter.java:52)
            |        at org.gradle.execution.plan.LocalTaskNodeExecutor.execute(LocalTaskNodeExecutor.java:41)
            |        at org.gradle.execution.taskgraph.DefaultTaskExecutionGraph$InvokeNodeExecutorsAction.execute(DefaultTaskExecutionGraph.java:372)
            |        at org.gradle.execution.taskgraph.DefaultTaskExecutionGraph$InvokeNodeExecutorsAction.execute(DefaultTaskExecutionGraph.java:359)
            |        at org.gradle.execution.taskgraph.DefaultTaskExecutionGraph$BuildOperationAwareExecutionAction.execute(DefaultTaskExecutionGraph.java:352)
            |        at org.gradle.execution.taskgraph.DefaultTaskExecutionGraph$BuildOperationAwareExecutionAction.execute(DefaultTaskExecutionGraph.java:338)
            |        at org.gradle.execution.plan.DefaultPlanExecutor$ExecutorWorker.lambda$run$0(DefaultPlanExecutor.java:127)
            |        at org.gradle.execution.plan.DefaultPlanExecutor$ExecutorWorker.execute(DefaultPlanExecutor.java:191)
            |        at org.gradle.execution.plan.DefaultPlanExecutor$ExecutorWorker.executeNextNode(DefaultPlanExecutor.java:182)
            |        at org.gradle.execution.plan.DefaultPlanExecutor$ExecutorWorker.run(DefaultPlanExecutor.java:124)
            |        at org.gradle.internal.concurrent.ExecutorPolicy$CatchAndRecordFailures.onExecute(ExecutorPolicy.java:64)
            |        at org.gradle.internal.concurrent.ManagedExecutorImpl$1.run(ManagedExecutorImpl.java:48)
            |        at org.gradle.internal.concurrent.ThreadFactoryImpl$ManagedThreadRunnable.run(ThreadFactoryImpl.java:56)
            |Caused by: org.gradle.api.GradleException: Fail to execute
            |        at build_j28mkeqfxzb7clyvr5fcif5h$_run_closure1$_closure3.doCall(/Users/daniel/gradle/tmp/build-result-test/build.gradle:15)
            |        at org.gradle.api.internal.AbstractTask$ClosureTaskAction.execute(AbstractTask.java:670)
            |        at org.gradle.api.internal.AbstractTask$ClosureTaskAction.execute(AbstractTask.java:643)
            |        at org.gradle.api.internal.tasks.execution.ExecuteActionsTaskExecuter$3.run(ExecuteActionsTaskExecuter.java:568)
            |        at org.gradle.internal.operations.DefaultBuildOperationExecutor$RunnableBuildOperationWorker.execute(DefaultBuildOperationExecutor.java:402)
            |        at org.gradle.internal.operations.DefaultBuildOperationExecutor$RunnableBuildOperationWorker.execute(DefaultBuildOperationExecutor.java:394)
            |        at org.gradle.internal.operations.DefaultBuildOperationExecutor$1.execute(DefaultBuildOperationExecutor.java:165)
            |        at org.gradle.internal.operations.DefaultBuildOperationExecutor.execute(DefaultBuildOperationExecutor.java:250)
            |        at org.gradle.internal.operations.DefaultBuildOperationExecutor.execute(DefaultBuildOperationExecutor.java:158)
            |        at org.gradle.internal.operations.DefaultBuildOperationExecutor.run(DefaultBuildOperationExecutor.java:92)
            |        at org.gradle.internal.operations.DelegatingBuildOperationExecutor.run(DelegatingBuildOperationExecutor.java:31)
            |        at org.gradle.api.internal.tasks.execution.ExecuteActionsTaskExecuter.executeAction(ExecuteActionsTaskExecuter.java:553)
            |        at org.gradle.api.internal.tasks.execution.ExecuteActionsTaskExecuter.executeActions(ExecuteActionsTaskExecuter.java:536)
            |        at org.gradle.api.internal.tasks.execution.ExecuteActionsTaskExecuter.access$300(ExecuteActionsTaskExecuter.java:109)
            |        at org.gradle.api.internal.tasks.execution.ExecuteActionsTaskExecuter$TaskExecution.executeWithPreviousOutputFiles(ExecuteActionsTaskExecuter.java:276)
            |        at org.gradle.api.internal.tasks.execution.ExecuteActionsTaskExecuter$TaskExecution.execute(ExecuteActionsTaskExecuter.java:265)
            |        at org.gradle.internal.execution.steps.ExecuteStep.lambda$execute$1(ExecuteStep.java:33)
            |        at org.gradle.internal.execution.steps.ExecuteStep.execute(ExecuteStep.java:33)
            |        at org.gradle.internal.execution.steps.ExecuteStep.execute(ExecuteStep.java:26)
            |        at org.gradle.internal.execution.steps.CleanupOutputsStep.execute(CleanupOutputsStep.java:67)
            |        at org.gradle.internal.execution.steps.CleanupOutputsStep.execute(CleanupOutputsStep.java:36)
            |        at org.gradle.internal.execution.steps.ResolveInputChangesStep.execute(ResolveInputChangesStep.java:49)
            |        at org.gradle.internal.execution.steps.ResolveInputChangesStep.execute(ResolveInputChangesStep.java:34)
            |        at org.gradle.internal.execution.steps.CancelExecutionStep.execute(CancelExecutionStep.java:43)
            |        at org.gradle.internal.execution.steps.TimeoutStep.executeWithoutTimeout(TimeoutStep.java:73)
            |        at org.gradle.internal.execution.steps.TimeoutStep.execute(TimeoutStep.java:54)
            |        at org.gradle.internal.execution.steps.CatchExceptionStep.execute(CatchExceptionStep.java:34)
            |        at org.gradle.internal.execution.steps.CreateOutputsStep.execute(CreateOutputsStep.java:44)
            |        at org.gradle.internal.execution.steps.SnapshotOutputsStep.execute(SnapshotOutputsStep.java:54)
            |        at org.gradle.internal.execution.steps.SnapshotOutputsStep.execute(SnapshotOutputsStep.java:38)
            |        at org.gradle.internal.execution.steps.BroadcastChangingOutputsStep.execute(BroadcastChangingOutputsStep.java:49)
            |        at org.gradle.internal.execution.steps.CacheStep.executeWithoutCache(CacheStep.java:153)
            |        at org.gradle.internal.execution.steps.CacheStep.execute(CacheStep.java:67)
            |        at org.gradle.internal.execution.steps.CacheStep.execute(CacheStep.java:41)
            |        at org.gradle.internal.execution.steps.StoreExecutionStateStep.execute(StoreExecutionStateStep.java:44)
            |        at org.gradle.internal.execution.steps.StoreExecutionStateStep.execute(StoreExecutionStateStep.java:33)
            |        at org.gradle.internal.execution.steps.RecordOutputsStep.execute(RecordOutputsStep.java:38)
            |        at org.gradle.internal.execution.steps.RecordOutputsStep.execute(RecordOutputsStep.java:24)
            |        at org.gradle.internal.execution.steps.SkipUpToDateStep.executeBecause(SkipUpToDateStep.java:92)
            |        at org.gradle.internal.execution.steps.SkipUpToDateStep.lambda$execute$0(SkipUpToDateStep.java:85)
            |        at org.gradle.internal.execution.steps.SkipUpToDateStep.execute(SkipUpToDateStep.java:55)
            |        at org.gradle.internal.execution.steps.SkipUpToDateStep.execute(SkipUpToDateStep.java:39)
            |        at org.gradle.internal.execution.steps.ResolveChangesStep.execute(ResolveChangesStep.java:76)
            |        at org.gradle.internal.execution.steps.ResolveChangesStep.execute(ResolveChangesStep.java:37)
            |        at org.gradle.internal.execution.steps.legacy.MarkSnapshottingInputsFinishedStep.execute(MarkSnapshottingInputsFinishedStep.java:36)
            |        at org.gradle.internal.execution.steps.legacy.MarkSnapshottingInputsFinishedStep.execute(MarkSnapshottingInputsFinishedStep.java:26)
            |        at org.gradle.internal.execution.steps.ResolveCachingStateStep.execute(ResolveCachingStateStep.java:94)
            |        at org.gradle.internal.execution.steps.ResolveCachingStateStep.execute(ResolveCachingStateStep.java:49)
            |        at org.gradle.internal.execution.steps.CaptureStateBeforeExecutionStep.execute(CaptureStateBeforeExecutionStep.java:79)
            |        at org.gradle.internal.execution.steps.CaptureStateBeforeExecutionStep.execute(CaptureStateBeforeExecutionStep.java:53)
            |        at org.gradle.internal.execution.steps.ValidateStep.execute(ValidateStep.java:74)
            |        at org.gradle.internal.execution.steps.SkipEmptyWorkStep.lambda$execute$2(SkipEmptyWorkStep.java:78)
            |        at org.gradle.internal.execution.steps.SkipEmptyWorkStep.execute(SkipEmptyWorkStep.java:78)
            |        at org.gradle.internal.execution.steps.SkipEmptyWorkStep.execute(SkipEmptyWorkStep.java:34)
            |        at org.gradle.internal.execution.steps.legacy.MarkSnapshottingInputsStartedStep.execute(MarkSnapshottingInputsStartedStep.java:39)
            |        at org.gradle.internal.execution.steps.LoadExecutionStateStep.execute(LoadExecutionStateStep.java:40)
            |        at org.gradle.internal.execution.steps.LoadExecutionStateStep.execute(LoadExecutionStateStep.java:28)
            |        at org.gradle.internal.execution.impl.DefaultWorkExecutor.execute(DefaultWorkExecutor.java:33)
            |        at org.gradle.api.internal.tasks.execution.ExecuteActionsTaskExecuter.executeIfValid(ExecuteActionsTaskExecuter.java:192)
            |        ... 30 more
            |
            |
            |* Get more help at https://help.gradle.org
            |
            |BUILD FAILED in 462ms
            |1 actionable task: 1 executed
            |'''.stripMargin()
    }

    String getOutputWithMultipleFailuresAndNoStacktrace() {
        return '''> Task :foo FAILED
            |> Task :foo:foo FAILED
            |> Task :bar:foo FAILED
            |
            |FAILURE: Build completed with 3 failures.
            |
            |1: Task failed with an exception.
            |-----------
            |* Where:
            |Build file '/Users/daniel/gradle/tmp/build-result-test/build.gradle' line: 15
            |
            |* What went wrong:
            |Execution failed for task ':foo'.
            |> Fail to execute
            |
            |* Try:
            |Run with --stacktrace option to get the stack trace. Run with --info or --debug option to get more log output. Run with --scan to get full insights.
            |==============================================================================
            |
            |2: Task failed with an exception.
            |-----------
            |* Where:
            |Build file '/Users/daniel/gradle/tmp/build-result-test/build.gradle' line: 54
            |
            |* What went wrong:
            |Execution failed for task ':bar:foo'.
            |> Fail to execute
            |
            |* Try:
            |Run with --stacktrace option to get the stack trace. Run with --info or --debug option to get more log output. Run with --scan to get full insights.
            |==============================================================================
            |
            |3: Task failed with an exception.
            |-----------
            |* Where:
            |Build file '/Users/daniel/gradle/tmp/build-result-test/build.gradle' line: 44
            |
            |* What went wrong:
            |Execution failed for task ':foo:foo'.
            |> Fail to execute
            |
            |* Try:
            |Run with --stacktrace option to get the stack trace. Run with --info or --debug option to get more log output. Run with --scan to get full insights.
            |==============================================================================
            |
            |* Get more help at https://help.gradle.org
            |
            |BUILD FAILED in 827ms
            |3 actionable tasks: 3 executed'''.stripMargin()
    }

    String getOutputWithMultipleFailuresAndStacktrace() {
        return '''> Task :foo FAILED
            |> Task :foo:foo FAILED
            |> Task :bar:foo FAILED
            |
            |FAILURE: Build completed with 3 failures.
            |
            |1: Task failed with an exception.
            |-----------
            |* Where:
            |Build file '/Users/daniel/gradle/tmp/build-result-test/build.gradle' line: 15
            |
            |* What went wrong:
            |Execution failed for task ':foo'.
            |> Fail to execute
            |
            |* Try:
            |Run with --info or --debug option to get more log output. Run with --scan to get full insights.
            |
            |* Exception is:
            |org.gradle.api.tasks.TaskExecutionException: Execution failed for task ':foo'.
            |        at org.gradle.api.internal.tasks.execution.ExecuteActionsTaskExecuter.lambda$executeIfValid$1(ExecuteActionsTaskExecuter.java:205)
            |        at org.gradle.internal.Try$Failure.ifSuccessfulOrElse(Try.java:263)
            |        at org.gradle.api.internal.tasks.execution.ExecuteActionsTaskExecuter.executeIfValid(ExecuteActionsTaskExecuter.java:203)
            |        at org.gradle.api.internal.tasks.execution.ExecuteActionsTaskExecuter.execute(ExecuteActionsTaskExecuter.java:184)
            |        at org.gradle.api.internal.tasks.execution.CleanupStaleOutputsExecuter.execute(CleanupStaleOutputsExecuter.java:114)
            |        at org.gradle.api.internal.tasks.execution.FinalizePropertiesTaskExecuter.execute(FinalizePropertiesTaskExecuter.java:46)
            |        at org.gradle.api.internal.tasks.execution.ResolveTaskExecutionModeExecuter.execute(ResolveTaskExecutionModeExecuter.java:62)
            |        at org.gradle.api.internal.tasks.execution.SkipTaskWithNoActionsExecuter.execute(SkipTaskWithNoActionsExecuter.java:57)
            |        at org.gradle.api.internal.tasks.execution.SkipOnlyIfTaskExecuter.execute(SkipOnlyIfTaskExecuter.java:56)
            |        at org.gradle.api.internal.tasks.execution.CatchExceptionTaskExecuter.execute(CatchExceptionTaskExecuter.java:36)
            |        at org.gradle.api.internal.tasks.execution.EventFiringTaskExecuter$1.executeTask(EventFiringTaskExecuter.java:77)
            |        at org.gradle.api.internal.tasks.execution.EventFiringTaskExecuter$1.call(EventFiringTaskExecuter.java:55)
            |        at org.gradle.api.internal.tasks.execution.EventFiringTaskExecuter$1.call(EventFiringTaskExecuter.java:52)
            |        at org.gradle.internal.operations.DefaultBuildOperationExecutor$CallableBuildOperationWorker.execute(DefaultBuildOperationExecutor.java:416)
            |        at org.gradle.internal.operations.DefaultBuildOperationExecutor$CallableBuildOperationWorker.execute(DefaultBuildOperationExecutor.java:406)
            |        at org.gradle.internal.operations.DefaultBuildOperationExecutor$1.execute(DefaultBuildOperationExecutor.java:165)
            |        at org.gradle.internal.operations.DefaultBuildOperationExecutor.execute(DefaultBuildOperationExecutor.java:250)
            |        at org.gradle.internal.operations.DefaultBuildOperationExecutor.execute(DefaultBuildOperationExecutor.java:158)
            |        at org.gradle.internal.operations.DefaultBuildOperationExecutor.call(DefaultBuildOperationExecutor.java:102)
            |        at org.gradle.internal.operations.DelegatingBuildOperationExecutor.call(DelegatingBuildOperationExecutor.java:36)
            |        at org.gradle.api.internal.tasks.execution.EventFiringTaskExecuter.execute(EventFiringTaskExecuter.java:52)
            |        at org.gradle.execution.plan.LocalTaskNodeExecutor.execute(LocalTaskNodeExecutor.java:41)
            |        at org.gradle.execution.taskgraph.DefaultTaskExecutionGraph$InvokeNodeExecutorsAction.execute(DefaultTaskExecutionGraph.java:372)
            |        at org.gradle.execution.taskgraph.DefaultTaskExecutionGraph$InvokeNodeExecutorsAction.execute(DefaultTaskExecutionGraph.java:359)
            |        at org.gradle.execution.taskgraph.DefaultTaskExecutionGraph$BuildOperationAwareExecutionAction.execute(DefaultTaskExecutionGraph.java:352)
            |        at org.gradle.execution.taskgraph.DefaultTaskExecutionGraph$BuildOperationAwareExecutionAction.execute(DefaultTaskExecutionGraph.java:338)
            |        at org.gradle.execution.plan.DefaultPlanExecutor$ExecutorWorker.lambda$run$0(DefaultPlanExecutor.java:127)
            |        at org.gradle.execution.plan.DefaultPlanExecutor$ExecutorWorker.execute(DefaultPlanExecutor.java:191)
            |        at org.gradle.execution.plan.DefaultPlanExecutor$ExecutorWorker.executeNextNode(DefaultPlanExecutor.java:182)
            |        at org.gradle.execution.plan.DefaultPlanExecutor$ExecutorWorker.run(DefaultPlanExecutor.java:124)
            |        at org.gradle.internal.concurrent.ExecutorPolicy$CatchAndRecordFailures.onExecute(ExecutorPolicy.java:64)
            |        at org.gradle.internal.concurrent.ManagedExecutorImpl$1.run(ManagedExecutorImpl.java:48)
            |        at org.gradle.internal.concurrent.ThreadFactoryImpl$ManagedThreadRunnable.run(ThreadFactoryImpl.java:56)
            |Caused by: org.gradle.api.GradleException: Fail to execute
            |        at build_j28mkeqfxzb7clyvr5fcif5h$_run_closure1$_closure5.doCall(/Users/daniel/gradle/tmp/build-result-test/build.gradle:15)
            |        at org.gradle.api.internal.AbstractTask$ClosureTaskAction.execute(AbstractTask.java:670)
            |        at org.gradle.api.internal.AbstractTask$ClosureTaskAction.execute(AbstractTask.java:643)
            |        at org.gradle.api.internal.tasks.execution.ExecuteActionsTaskExecuter$3.run(ExecuteActionsTaskExecuter.java:568)
            |        at org.gradle.internal.operations.DefaultBuildOperationExecutor$RunnableBuildOperationWorker.execute(DefaultBuildOperationExecutor.java:402)
            |        at org.gradle.internal.operations.DefaultBuildOperationExecutor$RunnableBuildOperationWorker.execute(DefaultBuildOperationExecutor.java:394)
            |        at org.gradle.internal.operations.DefaultBuildOperationExecutor$1.execute(DefaultBuildOperationExecutor.java:165)
            |        at org.gradle.internal.operations.DefaultBuildOperationExecutor.execute(DefaultBuildOperationExecutor.java:250)
            |        at org.gradle.internal.operations.DefaultBuildOperationExecutor.execute(DefaultBuildOperationExecutor.java:158)
            |        at org.gradle.internal.operations.DefaultBuildOperationExecutor.run(DefaultBuildOperationExecutor.java:92)
            |        at org.gradle.internal.operations.DelegatingBuildOperationExecutor.run(DelegatingBuildOperationExecutor.java:31)
            |        at org.gradle.api.internal.tasks.execution.ExecuteActionsTaskExecuter.executeAction(ExecuteActionsTaskExecuter.java:553)
            |        at org.gradle.api.internal.tasks.execution.ExecuteActionsTaskExecuter.executeActions(ExecuteActionsTaskExecuter.java:536)
            |        at org.gradle.api.internal.tasks.execution.ExecuteActionsTaskExecuter.access$300(ExecuteActionsTaskExecuter.java:109)
            |        at org.gradle.api.internal.tasks.execution.ExecuteActionsTaskExecuter$TaskExecution.executeWithPreviousOutputFiles(ExecuteActionsTaskExecuter.java:276)
            |        at org.gradle.api.internal.tasks.execution.ExecuteActionsTaskExecuter$TaskExecution.execute(ExecuteActionsTaskExecuter.java:265)
            |        at org.gradle.internal.execution.steps.ExecuteStep.lambda$execute$1(ExecuteStep.java:33)
            |        at org.gradle.internal.execution.steps.ExecuteStep.execute(ExecuteStep.java:33)
            |        at org.gradle.internal.execution.steps.ExecuteStep.execute(ExecuteStep.java:26)
            |        at org.gradle.internal.execution.steps.CleanupOutputsStep.execute(CleanupOutputsStep.java:67)
            |        at org.gradle.internal.execution.steps.CleanupOutputsStep.execute(CleanupOutputsStep.java:36)
            |        at org.gradle.internal.execution.steps.ResolveInputChangesStep.execute(ResolveInputChangesStep.java:49)
            |        at org.gradle.internal.execution.steps.ResolveInputChangesStep.execute(ResolveInputChangesStep.java:34)
            |        at org.gradle.internal.execution.steps.CancelExecutionStep.execute(CancelExecutionStep.java:43)
            |        at org.gradle.internal.execution.steps.TimeoutStep.executeWithoutTimeout(TimeoutStep.java:73)
            |        at org.gradle.internal.execution.steps.TimeoutStep.execute(TimeoutStep.java:54)
            |        at org.gradle.internal.execution.steps.CatchExceptionStep.execute(CatchExceptionStep.java:34)
            |        at org.gradle.internal.execution.steps.CreateOutputsStep.execute(CreateOutputsStep.java:44)
            |        at org.gradle.internal.execution.steps.SnapshotOutputsStep.execute(SnapshotOutputsStep.java:54)
            |        at org.gradle.internal.execution.steps.SnapshotOutputsStep.execute(SnapshotOutputsStep.java:38)
            |        at org.gradle.internal.execution.steps.BroadcastChangingOutputsStep.execute(BroadcastChangingOutputsStep.java:49)
            |        at org.gradle.internal.execution.steps.CacheStep.executeWithoutCache(CacheStep.java:153)
            |        at org.gradle.internal.execution.steps.CacheStep.execute(CacheStep.java:67)
            |        at org.gradle.internal.execution.steps.CacheStep.execute(CacheStep.java:41)
            |        at org.gradle.internal.execution.steps.StoreExecutionStateStep.execute(StoreExecutionStateStep.java:44)
            |        at org.gradle.internal.execution.steps.StoreExecutionStateStep.execute(StoreExecutionStateStep.java:33)
            |        at org.gradle.internal.execution.steps.RecordOutputsStep.execute(RecordOutputsStep.java:38)
            |        at org.gradle.internal.execution.steps.RecordOutputsStep.execute(RecordOutputsStep.java:24)
            |        at org.gradle.internal.execution.steps.SkipUpToDateStep.executeBecause(SkipUpToDateStep.java:92)
            |        at org.gradle.internal.execution.steps.SkipUpToDateStep.lambda$execute$0(SkipUpToDateStep.java:85)
            |        at org.gradle.internal.execution.steps.SkipUpToDateStep.execute(SkipUpToDateStep.java:55)
            |        at org.gradle.internal.execution.steps.SkipUpToDateStep.execute(SkipUpToDateStep.java:39)
            |        at org.gradle.internal.execution.steps.ResolveChangesStep.execute(ResolveChangesStep.java:76)
            |        at org.gradle.internal.execution.steps.ResolveChangesStep.execute(ResolveChangesStep.java:37)
            |        at org.gradle.internal.execution.steps.legacy.MarkSnapshottingInputsFinishedStep.execute(MarkSnapshottingInputsFinishedStep.java:36)
            |        at org.gradle.internal.execution.steps.legacy.MarkSnapshottingInputsFinishedStep.execute(MarkSnapshottingInputsFinishedStep.java:26)
            |        at org.gradle.internal.execution.steps.ResolveCachingStateStep.execute(ResolveCachingStateStep.java:94)
            |        at org.gradle.internal.execution.steps.ResolveCachingStateStep.execute(ResolveCachingStateStep.java:49)
            |        at org.gradle.internal.execution.steps.CaptureStateBeforeExecutionStep.execute(CaptureStateBeforeExecutionStep.java:79)
            |        at org.gradle.internal.execution.steps.CaptureStateBeforeExecutionStep.execute(CaptureStateBeforeExecutionStep.java:53)
            |        at org.gradle.internal.execution.steps.ValidateStep.execute(ValidateStep.java:74)
            |        at org.gradle.internal.execution.steps.SkipEmptyWorkStep.lambda$execute$2(SkipEmptyWorkStep.java:78)
            |        at org.gradle.internal.execution.steps.SkipEmptyWorkStep.execute(SkipEmptyWorkStep.java:78)
            |        at org.gradle.internal.execution.steps.SkipEmptyWorkStep.execute(SkipEmptyWorkStep.java:34)
            |        at org.gradle.internal.execution.steps.legacy.MarkSnapshottingInputsStartedStep.execute(MarkSnapshottingInputsStartedStep.java:39)
            |        at org.gradle.internal.execution.steps.LoadExecutionStateStep.execute(LoadExecutionStateStep.java:40)
            |        at org.gradle.internal.execution.steps.LoadExecutionStateStep.execute(LoadExecutionStateStep.java:28)
            |        at org.gradle.internal.execution.impl.DefaultWorkExecutor.execute(DefaultWorkExecutor.java:33)
            |        at org.gradle.api.internal.tasks.execution.ExecuteActionsTaskExecuter.executeIfValid(ExecuteActionsTaskExecuter.java:192)
            |        ... 30 more
            |
            |==============================================================================
            |
            |2: Task failed with an exception.
            |-----------
            |* Where:
            |Build file '/Users/daniel/gradle/tmp/build-result-test/build.gradle' line: 54
            |
            |* What went wrong:
            |Execution failed for task ':bar:foo'.
            |> Fail to execute
            |
            |* Try:
            |Run with --info or --debug option to get more log output. Run with --scan to get full insights.
            |
            |* Exception is:
            |org.gradle.api.tasks.TaskExecutionException: Execution failed for task ':bar:foo'.
            |        at org.gradle.api.internal.tasks.execution.ExecuteActionsTaskExecuter.lambda$executeIfValid$1(ExecuteActionsTaskExecuter.java:205)
            |        at org.gradle.internal.Try$Failure.ifSuccessfulOrElse(Try.java:263)
            |        at org.gradle.api.internal.tasks.execution.ExecuteActionsTaskExecuter.executeIfValid(ExecuteActionsTaskExecuter.java:203)
            |        at org.gradle.api.internal.tasks.execution.ExecuteActionsTaskExecuter.execute(ExecuteActionsTaskExecuter.java:184)
            |        at org.gradle.api.internal.tasks.execution.CleanupStaleOutputsExecuter.execute(CleanupStaleOutputsExecuter.java:114)
            |        at org.gradle.api.internal.tasks.execution.FinalizePropertiesTaskExecuter.execute(FinalizePropertiesTaskExecuter.java:46)
            |        at org.gradle.api.internal.tasks.execution.ResolveTaskExecutionModeExecuter.execute(ResolveTaskExecutionModeExecuter.java:62)
            |        at org.gradle.api.internal.tasks.execution.SkipTaskWithNoActionsExecuter.execute(SkipTaskWithNoActionsExecuter.java:57)
            |        at org.gradle.api.internal.tasks.execution.SkipOnlyIfTaskExecuter.execute(SkipOnlyIfTaskExecuter.java:56)
            |        at org.gradle.api.internal.tasks.execution.CatchExceptionTaskExecuter.execute(CatchExceptionTaskExecuter.java:36)
            |        at org.gradle.api.internal.tasks.execution.EventFiringTaskExecuter$1.executeTask(EventFiringTaskExecuter.java:77)
            |        at org.gradle.api.internal.tasks.execution.EventFiringTaskExecuter$1.call(EventFiringTaskExecuter.java:55)
            |        at org.gradle.api.internal.tasks.execution.EventFiringTaskExecuter$1.call(EventFiringTaskExecuter.java:52)
            |        at org.gradle.internal.operations.DefaultBuildOperationExecutor$CallableBuildOperationWorker.execute(DefaultBuildOperationExecutor.java:416)
            |        at org.gradle.internal.operations.DefaultBuildOperationExecutor$CallableBuildOperationWorker.execute(DefaultBuildOperationExecutor.java:406)
            |        at org.gradle.internal.operations.DefaultBuildOperationExecutor$1.execute(DefaultBuildOperationExecutor.java:165)
            |        at org.gradle.internal.operations.DefaultBuildOperationExecutor.execute(DefaultBuildOperationExecutor.java:250)
            |        at org.gradle.internal.operations.DefaultBuildOperationExecutor.execute(DefaultBuildOperationExecutor.java:158)
            |        at org.gradle.internal.operations.DefaultBuildOperationExecutor.call(DefaultBuildOperationExecutor.java:102)
            |        at org.gradle.internal.operations.DelegatingBuildOperationExecutor.call(DelegatingBuildOperationExecutor.java:36)
            |        at org.gradle.api.internal.tasks.execution.EventFiringTaskExecuter.execute(EventFiringTaskExecuter.java:52)
            |        at org.gradle.execution.plan.LocalTaskNodeExecutor.execute(LocalTaskNodeExecutor.java:41)
            |        at org.gradle.execution.taskgraph.DefaultTaskExecutionGraph$InvokeNodeExecutorsAction.execute(DefaultTaskExecutionGraph.java:372)
            |        at org.gradle.execution.taskgraph.DefaultTaskExecutionGraph$InvokeNodeExecutorsAction.execute(DefaultTaskExecutionGraph.java:359)
            |        at org.gradle.execution.taskgraph.DefaultTaskExecutionGraph$BuildOperationAwareExecutionAction.execute(DefaultTaskExecutionGraph.java:352)
            |        at org.gradle.execution.taskgraph.DefaultTaskExecutionGraph$BuildOperationAwareExecutionAction.execute(DefaultTaskExecutionGraph.java:338)
            |        at org.gradle.execution.plan.DefaultPlanExecutor$ExecutorWorker.lambda$run$0(DefaultPlanExecutor.java:127)
            |        at org.gradle.execution.plan.DefaultPlanExecutor$ExecutorWorker.execute(DefaultPlanExecutor.java:191)
            |        at org.gradle.execution.plan.DefaultPlanExecutor$ExecutorWorker.executeNextNode(DefaultPlanExecutor.java:182)
            |        at org.gradle.execution.plan.DefaultPlanExecutor$ExecutorWorker.run(DefaultPlanExecutor.java:124)
            |        at org.gradle.internal.concurrent.ExecutorPolicy$CatchAndRecordFailures.onExecute(ExecutorPolicy.java:64)
            |        at org.gradle.internal.concurrent.ManagedExecutorImpl$1.run(ManagedExecutorImpl.java:48)
            |        at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1149)
            |        at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:624)
            |        at org.gradle.internal.concurrent.ThreadFactoryImpl$ManagedThreadRunnable.run(ThreadFactoryImpl.java:56)
            |        at java.lang.Thread.run(Thread.java:748)
            |Caused by: org.gradle.api.GradleException: Fail to execute
            |        at sun.reflect.NativeConstructorAccessorImpl.newInstance0(Native Method)
            |        at sun.reflect.NativeConstructorAccessorImpl.newInstance(NativeConstructorAccessorImpl.java:62)
            |        at sun.reflect.DelegatingConstructorAccessorImpl.newInstance(DelegatingConstructorAccessorImpl.java:45)
            |        at java.lang.reflect.Constructor.newInstance(Constructor.java:423)
            |        at org.codehaus.groovy.reflection.CachedConstructor.invoke(CachedConstructor.java:80)
            |        at org.codehaus.groovy.runtime.callsite.ConstructorSite$ConstructorSiteNoUnwrapNoCoerce.callConstructor(ConstructorSite.java:105)
            |        at org.codehaus.groovy.runtime.callsite.AbstractCallSite.callConstructor(AbstractCallSite.java:249)
            |        at build_j28mkeqfxzb7clyvr5fcif5h$_run_closure4$_closure9$_closure10.doCall(/Users/daniel/gradle/tmp/build-result-test/build.gradle:54)
            |        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
            |        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
            |        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
            |        at java.lang.reflect.Method.invoke(Method.java:498)
            |        at org.codehaus.groovy.reflection.CachedMethod.invoke(CachedMethod.java:101)
            |        at groovy.lang.MetaMethod.doMethodInvoke(MetaMethod.java:323)
            |        at org.codehaus.groovy.runtime.metaclass.ClosureMetaClass.invokeMethod(ClosureMetaClass.java:263)
            |        at groovy.lang.MetaClassImpl.invokeMethod(MetaClassImpl.java:1041)
            |        at groovy.lang.Closure.call(Closure.java:405)
            |        at groovy.lang.Closure.call(Closure.java:421)
            |        at org.gradle.api.internal.AbstractTask$ClosureTaskAction.execute(AbstractTask.java:670)
            |        at org.gradle.api.internal.AbstractTask$ClosureTaskAction.execute(AbstractTask.java:643)
            |        at org.gradle.api.internal.tasks.execution.ExecuteActionsTaskExecuter$3.run(ExecuteActionsTaskExecuter.java:568)
            |        at org.gradle.internal.operations.DefaultBuildOperationExecutor$RunnableBuildOperationWorker.execute(DefaultBuildOperationExecutor.java:402)
            |        at org.gradle.internal.operations.DefaultBuildOperationExecutor$RunnableBuildOperationWorker.execute(DefaultBuildOperationExecutor.java:394)
            |        at org.gradle.internal.operations.DefaultBuildOperationExecutor$1.execute(DefaultBuildOperationExecutor.java:165)
            |        at org.gradle.internal.operations.DefaultBuildOperationExecutor.execute(DefaultBuildOperationExecutor.java:250)
            |        at org.gradle.internal.operations.DefaultBuildOperationExecutor.execute(DefaultBuildOperationExecutor.java:158)
            |        at org.gradle.internal.operations.DefaultBuildOperationExecutor.run(DefaultBuildOperationExecutor.java:92)
            |        at org.gradle.internal.operations.DelegatingBuildOperationExecutor.run(DelegatingBuildOperationExecutor.java:31)
            |        at org.gradle.api.internal.tasks.execution.ExecuteActionsTaskExecuter.executeAction(ExecuteActionsTaskExecuter.java:553)
            |        at org.gradle.api.internal.tasks.execution.ExecuteActionsTaskExecuter.executeActions(ExecuteActionsTaskExecuter.java:536)
            |        at org.gradle.api.internal.tasks.execution.ExecuteActionsTaskExecuter.access$300(ExecuteActionsTaskExecuter.java:109)
            |        at org.gradle.api.internal.tasks.execution.ExecuteActionsTaskExecuter$TaskExecution.executeWithPreviousOutputFiles(ExecuteActionsTaskExecuter.java:276)
            |        at org.gradle.api.internal.tasks.execution.ExecuteActionsTaskExecuter$TaskExecution.execute(ExecuteActionsTaskExecuter.java:265)
            |        at org.gradle.internal.execution.steps.ExecuteStep.lambda$execute$1(ExecuteStep.java:33)
            |        at java.util.Optional.orElseGet(Optional.java:267)
            |        at org.gradle.internal.execution.steps.ExecuteStep.execute(ExecuteStep.java:33)
            |        at org.gradle.internal.execution.steps.ExecuteStep.execute(ExecuteStep.java:26)
            |        at org.gradle.internal.execution.steps.CleanupOutputsStep.execute(CleanupOutputsStep.java:67)
            |        at org.gradle.internal.execution.steps.CleanupOutputsStep.execute(CleanupOutputsStep.java:36)
            |        at org.gradle.internal.execution.steps.ResolveInputChangesStep.execute(ResolveInputChangesStep.java:49)
            |        at org.gradle.internal.execution.steps.ResolveInputChangesStep.execute(ResolveInputChangesStep.java:34)
            |        at org.gradle.internal.execution.steps.CancelExecutionStep.execute(CancelExecutionStep.java:43)
            |        at org.gradle.internal.execution.steps.TimeoutStep.executeWithoutTimeout(TimeoutStep.java:73)
            |        at org.gradle.internal.execution.steps.TimeoutStep.execute(TimeoutStep.java:54)
            |        at org.gradle.internal.execution.steps.CatchExceptionStep.execute(CatchExceptionStep.java:34)
            |        at org.gradle.internal.execution.steps.CreateOutputsStep.execute(CreateOutputsStep.java:44)
            |        at org.gradle.internal.execution.steps.SnapshotOutputsStep.execute(SnapshotOutputsStep.java:54)
            |        at org.gradle.internal.execution.steps.SnapshotOutputsStep.execute(SnapshotOutputsStep.java:38)
            |        at org.gradle.internal.execution.steps.BroadcastChangingOutputsStep.execute(BroadcastChangingOutputsStep.java:49)
            |        at org.gradle.internal.execution.steps.CacheStep.executeWithoutCache(CacheStep.java:153)
            |        at org.gradle.internal.execution.steps.CacheStep.execute(CacheStep.java:67)
            |        at org.gradle.internal.execution.steps.CacheStep.execute(CacheStep.java:41)
            |        at org.gradle.internal.execution.steps.StoreExecutionStateStep.execute(StoreExecutionStateStep.java:44)
            |        at org.gradle.internal.execution.steps.StoreExecutionStateStep.execute(StoreExecutionStateStep.java:33)
            |        at org.gradle.internal.execution.steps.RecordOutputsStep.execute(RecordOutputsStep.java:38)
            |        at org.gradle.internal.execution.steps.RecordOutputsStep.execute(RecordOutputsStep.java:24)
            |        at org.gradle.internal.execution.steps.SkipUpToDateStep.executeBecause(SkipUpToDateStep.java:92)
            |        at org.gradle.internal.execution.steps.SkipUpToDateStep.lambda$execute$0(SkipUpToDateStep.java:85)
            |        at java.util.Optional.map(Optional.java:215)
            |        at org.gradle.internal.execution.steps.SkipUpToDateStep.execute(SkipUpToDateStep.java:55)
            |        at org.gradle.internal.execution.steps.SkipUpToDateStep.execute(SkipUpToDateStep.java:39)
            |        at org.gradle.internal.execution.steps.ResolveChangesStep.execute(ResolveChangesStep.java:76)
            |        at org.gradle.internal.execution.steps.ResolveChangesStep.execute(ResolveChangesStep.java:37)
            |        at org.gradle.internal.execution.steps.legacy.MarkSnapshottingInputsFinishedStep.execute(MarkSnapshottingInputsFinishedStep.java:36)
            |        at org.gradle.internal.execution.steps.legacy.MarkSnapshottingInputsFinishedStep.execute(MarkSnapshottingInputsFinishedStep.java:26)
            |        at org.gradle.internal.execution.steps.ResolveCachingStateStep.execute(ResolveCachingStateStep.java:94)
            |        at org.gradle.internal.execution.steps.ResolveCachingStateStep.execute(ResolveCachingStateStep.java:49)
            |        at org.gradle.internal.execution.steps.CaptureStateBeforeExecutionStep.execute(CaptureStateBeforeExecutionStep.java:79)
            |        at org.gradle.internal.execution.steps.CaptureStateBeforeExecutionStep.execute(CaptureStateBeforeExecutionStep.java:53)
            |        at org.gradle.internal.execution.steps.ValidateStep.execute(ValidateStep.java:74)
            |        at org.gradle.internal.execution.steps.SkipEmptyWorkStep.lambda$execute$2(SkipEmptyWorkStep.java:78)
            |        at java.util.Optional.orElseGet(Optional.java:267)
            |        at org.gradle.internal.execution.steps.SkipEmptyWorkStep.execute(SkipEmptyWorkStep.java:78)
            |        at org.gradle.internal.execution.steps.SkipEmptyWorkStep.execute(SkipEmptyWorkStep.java:34)
            |        at org.gradle.internal.execution.steps.legacy.MarkSnapshottingInputsStartedStep.execute(MarkSnapshottingInputsStartedStep.java:39)
            |        at org.gradle.internal.execution.steps.LoadExecutionStateStep.execute(LoadExecutionStateStep.java:40)
            |        at org.gradle.internal.execution.steps.LoadExecutionStateStep.execute(LoadExecutionStateStep.java:28)
            |        at org.gradle.internal.execution.impl.DefaultWorkExecutor.execute(DefaultWorkExecutor.java:33)
            |        at org.gradle.api.internal.tasks.execution.ExecuteActionsTaskExecuter.executeIfValid(ExecuteActionsTaskExecuter.java:192)
            |        ... 33 more
            |
            |==============================================================================
            |
            |3: Task failed with an exception.
            |-----------
            |* Where:
            |Build file '/Users/daniel/gradle/tmp/build-result-test/build.gradle' line: 44
            |
            |* What went wrong:
            |Execution failed for task ':foo:foo'.
            |> Fail to execute
            |
            |* Try:
            |Run with --info or --debug option to get more log output. Run with --scan to get full insights.
            |
            |* Exception is:
            |org.gradle.api.tasks.TaskExecutionException: Execution failed for task ':foo:foo'.
            |        at org.gradle.api.internal.tasks.execution.ExecuteActionsTaskExecuter.lambda$executeIfValid$1(ExecuteActionsTaskExecuter.java:205)
            |        at org.gradle.internal.Try$Failure.ifSuccessfulOrElse(Try.java:263)
            |        at org.gradle.api.internal.tasks.execution.ExecuteActionsTaskExecuter.executeIfValid(ExecuteActionsTaskExecuter.java:203)
            |        at org.gradle.api.internal.tasks.execution.ExecuteActionsTaskExecuter.execute(ExecuteActionsTaskExecuter.java:184)
            |        at org.gradle.api.internal.tasks.execution.CleanupStaleOutputsExecuter.execute(CleanupStaleOutputsExecuter.java:114)
            |        at org.gradle.api.internal.tasks.execution.FinalizePropertiesTaskExecuter.execute(FinalizePropertiesTaskExecuter.java:46)
            |        at org.gradle.api.internal.tasks.execution.ResolveTaskExecutionModeExecuter.execute(ResolveTaskExecutionModeExecuter.java:62)
            |        at org.gradle.api.internal.tasks.execution.SkipTaskWithNoActionsExecuter.execute(SkipTaskWithNoActionsExecuter.java:57)
            |        at org.gradle.api.internal.tasks.execution.SkipOnlyIfTaskExecuter.execute(SkipOnlyIfTaskExecuter.java:56)
            |        at org.gradle.api.internal.tasks.execution.CatchExceptionTaskExecuter.execute(CatchExceptionTaskExecuter.java:36)
            |        at org.gradle.api.internal.tasks.execution.EventFiringTaskExecuter$1.executeTask(EventFiringTaskExecuter.java:77)
            |        at org.gradle.api.internal.tasks.execution.EventFiringTaskExecuter$1.call(EventFiringTaskExecuter.java:55)
            |        at org.gradle.api.internal.tasks.execution.EventFiringTaskExecuter$1.call(EventFiringTaskExecuter.java:52)
            |        at org.gradle.internal.operations.DefaultBuildOperationExecutor$CallableBuildOperationWorker.execute(DefaultBuildOperationExecutor.java:416)
            |        at org.gradle.internal.operations.DefaultBuildOperationExecutor$CallableBuildOperationWorker.execute(DefaultBuildOperationExecutor.java:406)
            |        at org.gradle.internal.operations.DefaultBuildOperationExecutor$1.execute(DefaultBuildOperationExecutor.java:165)
            |        at org.gradle.internal.operations.DefaultBuildOperationExecutor.execute(DefaultBuildOperationExecutor.java:250)
            |        at org.gradle.internal.operations.DefaultBuildOperationExecutor.execute(DefaultBuildOperationExecutor.java:158)
            |        at org.gradle.internal.operations.DefaultBuildOperationExecutor.call(DefaultBuildOperationExecutor.java:102)
            |        at org.gradle.internal.operations.DelegatingBuildOperationExecutor.call(DelegatingBuildOperationExecutor.java:36)
            |        at org.gradle.api.internal.tasks.execution.EventFiringTaskExecuter.execute(EventFiringTaskExecuter.java:52)
            |        at org.gradle.execution.plan.LocalTaskNodeExecutor.execute(LocalTaskNodeExecutor.java:41)
            |        at org.gradle.execution.taskgraph.DefaultTaskExecutionGraph$InvokeNodeExecutorsAction.execute(DefaultTaskExecutionGraph.java:372)
            |        at org.gradle.execution.taskgraph.DefaultTaskExecutionGraph$InvokeNodeExecutorsAction.execute(DefaultTaskExecutionGraph.java:359)
            |        at org.gradle.execution.taskgraph.DefaultTaskExecutionGraph$BuildOperationAwareExecutionAction.execute(DefaultTaskExecutionGraph.java:352)
            |        at org.gradle.execution.taskgraph.DefaultTaskExecutionGraph$BuildOperationAwareExecutionAction.execute(DefaultTaskExecutionGraph.java:338)
            |        at org.gradle.execution.plan.DefaultPlanExecutor$ExecutorWorker.lambda$run$0(DefaultPlanExecutor.java:127)
            |        at org.gradle.execution.plan.DefaultPlanExecutor$ExecutorWorker.execute(DefaultPlanExecutor.java:191)
            |        at org.gradle.execution.plan.DefaultPlanExecutor$ExecutorWorker.executeNextNode(DefaultPlanExecutor.java:182)
            |        at org.gradle.execution.plan.DefaultPlanExecutor$ExecutorWorker.run(DefaultPlanExecutor.java:124)
            |        at org.gradle.internal.concurrent.ExecutorPolicy$CatchAndRecordFailures.onExecute(ExecutorPolicy.java:64)
            |        at org.gradle.internal.concurrent.ManagedExecutorImpl$1.run(ManagedExecutorImpl.java:48)
            |        at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1149)
            |        at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:624)
            |        at org.gradle.internal.concurrent.ThreadFactoryImpl$ManagedThreadRunnable.run(ThreadFactoryImpl.java:56)
            |        at java.lang.Thread.run(Thread.java:748)
            |Caused by: org.gradle.api.GradleException: Fail to execute
            |        at sun.reflect.NativeConstructorAccessorImpl.newInstance0(Native Method)
            |        at sun.reflect.NativeConstructorAccessorImpl.newInstance(NativeConstructorAccessorImpl.java:62)
            |        at sun.reflect.DelegatingConstructorAccessorImpl.newInstance(DelegatingConstructorAccessorImpl.java:45)
            |        at java.lang.reflect.Constructor.newInstance(Constructor.java:423)
            |        at org.codehaus.groovy.reflection.CachedConstructor.invoke(CachedConstructor.java:80)
            |        at org.codehaus.groovy.runtime.callsite.ConstructorSite$ConstructorSiteNoUnwrapNoCoerce.callConstructor(ConstructorSite.java:105)
            |        at org.codehaus.groovy.runtime.callsite.AbstractCallSite.callConstructor(AbstractCallSite.java:249)
            |        at build_j28mkeqfxzb7clyvr5fcif5h$_run_closure3$_closure7$_closure8.doCall(/Users/daniel/gradle/tmp/build-result-test/build.gradle:44)
            |        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
            |        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
            |        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
            |        at java.lang.reflect.Method.invoke(Method.java:498)
            |        at org.codehaus.groovy.reflection.CachedMethod.invoke(CachedMethod.java:101)
            |        at groovy.lang.MetaMethod.doMethodInvoke(MetaMethod.java:323)
            |        at org.codehaus.groovy.runtime.metaclass.ClosureMetaClass.invokeMethod(ClosureMetaClass.java:263)
            |        at groovy.lang.MetaClassImpl.invokeMethod(MetaClassImpl.java:1041)
            |        at groovy.lang.Closure.call(Closure.java:405)
            |        at groovy.lang.Closure.call(Closure.java:421)
            |        at org.gradle.api.internal.AbstractTask$ClosureTaskAction.execute(AbstractTask.java:670)
            |        at org.gradle.api.internal.AbstractTask$ClosureTaskAction.execute(AbstractTask.java:643)
            |        at org.gradle.api.internal.tasks.execution.ExecuteActionsTaskExecuter$3.run(ExecuteActionsTaskExecuter.java:568)
            |        at org.gradle.internal.operations.DefaultBuildOperationExecutor$RunnableBuildOperationWorker.execute(DefaultBuildOperationExecutor.java:402)
            |        at org.gradle.internal.operations.DefaultBuildOperationExecutor$RunnableBuildOperationWorker.execute(DefaultBuildOperationExecutor.java:394)
            |        at org.gradle.internal.operations.DefaultBuildOperationExecutor$1.execute(DefaultBuildOperationExecutor.java:165)
            |        at org.gradle.internal.operations.DefaultBuildOperationExecutor.execute(DefaultBuildOperationExecutor.java:250)
            |        at org.gradle.internal.operations.DefaultBuildOperationExecutor.execute(DefaultBuildOperationExecutor.java:158)
            |        at org.gradle.internal.operations.DefaultBuildOperationExecutor.run(DefaultBuildOperationExecutor.java:92)
            |        at org.gradle.internal.operations.DelegatingBuildOperationExecutor.run(DelegatingBuildOperationExecutor.java:31)
            |        at org.gradle.api.internal.tasks.execution.ExecuteActionsTaskExecuter.executeAction(ExecuteActionsTaskExecuter.java:553)
            |        at org.gradle.api.internal.tasks.execution.ExecuteActionsTaskExecuter.executeActions(ExecuteActionsTaskExecuter.java:536)
            |        at org.gradle.api.internal.tasks.execution.ExecuteActionsTaskExecuter.access$300(ExecuteActionsTaskExecuter.java:109)
            |        at org.gradle.api.internal.tasks.execution.ExecuteActionsTaskExecuter$TaskExecution.executeWithPreviousOutputFiles(ExecuteActionsTaskExecuter.java:276)
            |        at org.gradle.api.internal.tasks.execution.ExecuteActionsTaskExecuter$TaskExecution.execute(ExecuteActionsTaskExecuter.java:265)
            |        at org.gradle.internal.execution.steps.ExecuteStep.lambda$execute$1(ExecuteStep.java:33)
            |        at java.util.Optional.orElseGet(Optional.java:267)
            |        at org.gradle.internal.execution.steps.ExecuteStep.execute(ExecuteStep.java:33)
            |        at org.gradle.internal.execution.steps.ExecuteStep.execute(ExecuteStep.java:26)
            |        at org.gradle.internal.execution.steps.CleanupOutputsStep.execute(CleanupOutputsStep.java:67)
            |        at org.gradle.internal.execution.steps.CleanupOutputsStep.execute(CleanupOutputsStep.java:36)
            |        at org.gradle.internal.execution.steps.ResolveInputChangesStep.execute(ResolveInputChangesStep.java:49)
            |        at org.gradle.internal.execution.steps.ResolveInputChangesStep.execute(ResolveInputChangesStep.java:34)
            |        at org.gradle.internal.execution.steps.CancelExecutionStep.execute(CancelExecutionStep.java:43)
            |        at org.gradle.internal.execution.steps.TimeoutStep.executeWithoutTimeout(TimeoutStep.java:73)
            |        at org.gradle.internal.execution.steps.TimeoutStep.execute(TimeoutStep.java:54)
            |        at org.gradle.internal.execution.steps.CatchExceptionStep.execute(CatchExceptionStep.java:34)
            |        at org.gradle.internal.execution.steps.CreateOutputsStep.execute(CreateOutputsStep.java:44)
            |        at org.gradle.internal.execution.steps.SnapshotOutputsStep.execute(SnapshotOutputsStep.java:54)
            |        at org.gradle.internal.execution.steps.SnapshotOutputsStep.execute(SnapshotOutputsStep.java:38)
            |        at org.gradle.internal.execution.steps.BroadcastChangingOutputsStep.execute(BroadcastChangingOutputsStep.java:49)
            |        at org.gradle.internal.execution.steps.CacheStep.executeWithoutCache(CacheStep.java:153)
            |        at org.gradle.internal.execution.steps.CacheStep.execute(CacheStep.java:67)
            |        at org.gradle.internal.execution.steps.CacheStep.execute(CacheStep.java:41)
            |        at org.gradle.internal.execution.steps.StoreExecutionStateStep.execute(StoreExecutionStateStep.java:44)
            |        at org.gradle.internal.execution.steps.StoreExecutionStateStep.execute(StoreExecutionStateStep.java:33)
            |        at org.gradle.internal.execution.steps.RecordOutputsStep.execute(RecordOutputsStep.java:38)
            |        at org.gradle.internal.execution.steps.RecordOutputsStep.execute(RecordOutputsStep.java:24)
            |        at org.gradle.internal.execution.steps.SkipUpToDateStep.executeBecause(SkipUpToDateStep.java:92)
            |        at org.gradle.internal.execution.steps.SkipUpToDateStep.lambda$execute$0(SkipUpToDateStep.java:85)
            |        at java.util.Optional.map(Optional.java:215)
            |        at org.gradle.internal.execution.steps.SkipUpToDateStep.execute(SkipUpToDateStep.java:55)
            |        at org.gradle.internal.execution.steps.SkipUpToDateStep.execute(SkipUpToDateStep.java:39)
            |        at org.gradle.internal.execution.steps.ResolveChangesStep.execute(ResolveChangesStep.java:76)
            |        at org.gradle.internal.execution.steps.ResolveChangesStep.execute(ResolveChangesStep.java:37)
            |        at org.gradle.internal.execution.steps.legacy.MarkSnapshottingInputsFinishedStep.execute(MarkSnapshottingInputsFinishedStep.java:36)
            |        at org.gradle.internal.execution.steps.legacy.MarkSnapshottingInputsFinishedStep.execute(MarkSnapshottingInputsFinishedStep.java:26)
            |        at org.gradle.internal.execution.steps.ResolveCachingStateStep.execute(ResolveCachingStateStep.java:94)
            |        at org.gradle.internal.execution.steps.ResolveCachingStateStep.execute(ResolveCachingStateStep.java:49)
            |        at org.gradle.internal.execution.steps.CaptureStateBeforeExecutionStep.execute(CaptureStateBeforeExecutionStep.java:79)
            |        at org.gradle.internal.execution.steps.CaptureStateBeforeExecutionStep.execute(CaptureStateBeforeExecutionStep.java:53)
            |        at org.gradle.internal.execution.steps.ValidateStep.execute(ValidateStep.java:74)
            |        at org.gradle.internal.execution.steps.SkipEmptyWorkStep.lambda$execute$2(SkipEmptyWorkStep.java:78)
            |        at java.util.Optional.orElseGet(Optional.java:267)
            |        at org.gradle.internal.execution.steps.SkipEmptyWorkStep.execute(SkipEmptyWorkStep.java:78)
            |        at org.gradle.internal.execution.steps.SkipEmptyWorkStep.execute(SkipEmptyWorkStep.java:34)
            |        at org.gradle.internal.execution.steps.legacy.MarkSnapshottingInputsStartedStep.execute(MarkSnapshottingInputsStartedStep.java:39)
            |        at org.gradle.internal.execution.steps.LoadExecutionStateStep.execute(LoadExecutionStateStep.java:40)
            |        at org.gradle.internal.execution.steps.LoadExecutionStateStep.execute(LoadExecutionStateStep.java:28)
            |        at org.gradle.internal.execution.impl.DefaultWorkExecutor.execute(DefaultWorkExecutor.java:33)
            |        at org.gradle.api.internal.tasks.execution.ExecuteActionsTaskExecuter.executeIfValid(ExecuteActionsTaskExecuter.java:192)
            |        ... 33 more
            |
            |==============================================================================
            |
            |* Get more help at https://help.gradle.org
            |
            |BUILD FAILED in 473ms
            |3 actionable tasks: 3 executed'''.stripMargin()
    }
}
