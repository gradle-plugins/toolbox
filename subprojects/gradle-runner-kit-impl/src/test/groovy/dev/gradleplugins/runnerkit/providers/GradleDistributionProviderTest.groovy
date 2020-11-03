package dev.gradleplugins.runnerkit.providers
//package dev.gradleplugins.fixtures.gradle.runner
//
//import dev.gradleplugins.fixtures.file.FileSystemFixture
//import dev.gradleplugins.test.fixtures.gradle.executer.internal.WrapperGradleDistribution
//import org.junit.Rule
//import org.junit.rules.TemporaryFolder
//import spock.lang.Specification
//import spock.lang.Subject
//
//import static dev.gradleplugins.fixtures.gradle.runner.GradleDistributionProvider.executorDefault
//
//@Subject(GradleDistributionProvider)
//class GradleDistributionProviderTest extends Specification implements FileSystemFixture, GradleWrapperFixture {
//    @Rule
//    TemporaryFolder temporaryFolder = new TemporaryFolder()
//
//    @Override
//    File getTestDirectory() {
//        return temporaryFolder.root
//    }
//
//    def "can provide executor default distribution for Gradle Test Kit"() {
//        def context = Stub(GradleExecutionContext) {
//            getExecutorType() >> GradleExecutionContext.ExecutorType.GRADLE_TEST_KIT
//        }
//
//        expect:
//        def subject = executorDefault()
//        subject.isPresent()
//
//        and:
//        subject.calculateValue(context)
//        subject.get() == GradleDistributionProvider.findGradleInstallFromGradleRunner()
//    }
//
//    def "can provide executor default distribution for Gradle Wrapper"() {
//        def context = Stub(GradleExecutionContext) {
//            getExecutorType() >> GradleExecutionContext.ExecutorType.GRADLE_WRAPPER
//            getWorkingDirectory() >> WorkingDirectoryProvider.of(testDirectory)
//        }
//        writeGradleWrapperTo(testDirectory)
//
//        expect:
//        def subject = executorDefault()
//        subject.isPresent()
//
//        and:
//        subject.calculateValue(context)
//        subject.get() == new WrapperGradleDistribution(testDirectory)
//    }
//}
