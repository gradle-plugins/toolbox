package dev.gradleplugins.fixtures.gradle.runner

import dev.gradleplugins.fixtures.file.FileSystemFixture
import dev.gradleplugins.fixtures.gradle.runner.parameters.*
import org.apache.commons.lang3.SystemUtils
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.Subject
import java.util.Locale

import java.nio.charset.Charset

import static dev.gradleplugins.fixtures.gradle.runner.parameters.Locale.defaultLocale

@Subject(GradleRunnerImpl)
class GradleRunnerImplTest extends Specification implements FileSystemFixture {
    @Rule TemporaryFolder temporaryFolder = new TemporaryFolder()

    @Override
    File getTestDirectory() {
        return temporaryFolder.root
    }

    def "can account for all execution parameters"() {
        expect:
        executionDefaults.executionParameters.size() == 25
    }

    def "can disable stacktrace"() {
        expect:
        executionDefaults.stacktrace == Stacktrace.SHOW
        executionDefaults.allArguments.contains('--stacktrace')

        and:
        executionOf { withStacktraceDisabled() }.stacktrace == Stacktrace.HIDE
        executionOf { withStacktraceDisabled().withStacktraceDisabled() }.stacktrace == Stacktrace.HIDE

        and:
        !executionOf { withStacktraceDisabled() }.allArguments.contains('--stacktrace')
    }

    def "can enable build cache"() {
        expect:
        executionDefaults.buildCache == BuildCache.DISABLED
        !executionDefaults.allArguments.contains('--build-cache')

        and:
        executionOf { withBuildCacheEnabled() }.buildCache == BuildCache.ENABLED

        and:
        executionOf { withBuildCacheEnabled() }.allArguments.contains('--build-cache')
        // TODO: Not sure if it should be allowed or not... my feeling is we should use a linked set instead
//        executionOf { withBuildCacheEnabled().withBuildCacheEnabled() }.getArguments().get().findAll { it == '--build-cache' }.size() == 1
    }

    def "can configure arguments"() {
        expect:
        executionDefaults.arguments.empty

        and:
        executionOf { withArguments('a', 'b').withArguments(['c', 'd']) }.arguments.get() == ['c', 'd']
        executionOf { withArguments(['a', 'b']).withArguments('c', 'd') }.arguments.get() == ['c', 'd']
        executionOf { withArguments(['a', 'b']).withArgument('c') }.arguments.get() == ['a', 'b', 'c']
        executionOf { withArguments('a', 'b').withArgument('c') }.arguments.get() == ['a', 'b', 'c']

        and:
        executionOf { withArguments('a', 'b').withArgument('c') }.allArguments.containsAll(['a', 'b', 'c'])
    }

    def "can set build script"() {
        expect:
        !executionDefaults.buildScript.present
        !executionDefaults.allArguments.contains('--build-file')

        and:
        executionOf { usingBuildScript(file('foo.gradle')) }.buildScript.get() == file('foo.gradle')
        executionOf { usingBuildScript(file('bar.gradle.kts')) }.buildScript.get() == file('bar.gradle.kts')
        executionOf { usingBuildScript(file('foo.gradle')).usingBuildScript(file('bar.gradle')) }.buildScript.get() == file('bar.gradle')

        and:
        executionOf { usingBuildScript(file('foo.gradle')) }.allArguments.containsAll(['--build-file', file('foo.gradle').absolutePath])
    }

    def "can set settings file"() {
        expect:
        !executionDefaults.settingsFile.present
        !executionDefaults.allArguments.contains('--settings-file')

        and:
        executionOf { usingSettingsFile(file('settings.gradle')) }.settingsFile.get() == file('settings.gradle')
        executionOf { usingSettingsFile(file('settings.gradle.kts')) }.settingsFile.get() == file('settings.gradle.kts')
        executionOf { usingSettingsFile(file('foo.gradle')).usingSettingsFile(file('bar.gradle')) }.settingsFile.get() == file('bar.gradle')

        and:
        executionOf { usingSettingsFile(file('settings.gradle')) }.allArguments.containsAll(['--settings-file', file('settings.gradle').absolutePath])
    }

    def "can ignore missing settings file"() {
        expect:
        executionDefaults.missingSettingsFilePolicy == MissingSettingsFilePolicy.CREATE_WHEN_MISSING

        and:
        executionOf { ignoresMissingSettingsFile() }.missingSettingsFilePolicy == MissingSettingsFilePolicy.IGNORES_WHEN_MISSING
        executionOf { ignoresMissingSettingsFile().ignoresMissingSettingsFile() }.missingSettingsFilePolicy == MissingSettingsFilePolicy.IGNORES_WHEN_MISSING
    }

    def "can set project directory"() {
        expect:
        !executionDefaults.projectDirectory.present
        !executionDefaults.allArguments.contains('--project-dir')

        and:
        executionOf { usingProjectDirectory(file('dir')) }.projectDirectory.get() == file('dir')
        executionOf { usingProjectDirectory(file('foo')).usingProjectDirectory(file('bar')) }.projectDirectory.get() == file('bar')

        and:
        executionOf { usingProjectDirectory(file('dir')) }.allArguments.containsAll(['--project-dir', file('dir').absolutePath])
    }

    def "can set init scripts"() {
        expect:
        executionDefaults.initScripts.empty

        and:
        executionOf { usingInitScript(file('init.gradle')) }.initScripts.get() == [file('init.gradle')]
        executionOf { usingInitScript(file('a.init.gradle')).usingInitScript(file('b.init.gradle')) }.initScripts.get() == [file('a.init.gradle'), file('b.init.gradle')]

        and:
        executionOf { usingInitScript(file('init.gradle')) }.allArguments.containsAll(['--init-script', file('init.gradle').absolutePath])
        executionOf { usingInitScript(file('a.init.gradle')).usingInitScript(file('b.init.gradle')) }.allArguments.containsAll(['--init-script', file('a.init.gradle').absolutePath, '--init-script', file('b.init.gradle').absolutePath])

        // TODO: Same as above...
//        executionOf { usingInitScript(file('a.init.gradle')).usingInitScript(file('a.init.gradle')) }.initScripts.get() == [file('a.init.gradle')]
    }

    def "can set working directory"() {
        expect:
        !executionDefaults.workingDirectory.present

        and:
        executionOf { inDirectory(file('dir')) }.workingDirectory.get() == file('dir')
        executionOf { inDirectory(file('a')).inDirectory(file('b')) }.workingDirectory.get() == file('b')
    }

    def "can configure tasks"() {
        expect:
        executionDefaults.tasks.empty

        and:
        executionOf { withTasks('a', 'b').withTasks(['d']) }.tasks.get() == ['a', 'b', 'd']
        executionOf { withTasks(['a']).withTasks('c', 'd') }.tasks.get() == ['a', 'c', 'd']
        executionOf { withTasks(['a', 'b']).withTasks(['c']) }.tasks.get() == ['a', 'b', 'c']
        // TODO: What to do with duplicated task name... we should probably ignore them.

        and:
        executionOf { withTasks('a', 'b').withTasks(['c']) }.allArguments.containsAll(['a', 'b', 'c'])
    }

    def "can ignore deprecation checks"() {
        expect:
        executionDefaults.deprecationChecks == DeprecationChecks.FAILS
        executionDefaults.allArguments.containsAll(['--warning-mode', 'fail'])

        and:
        executionOf { withoutDeprecationChecks() }.deprecationChecks == DeprecationChecks.IGNORES
        executionOf { withoutDeprecationChecks().withoutDeprecationChecks() }.deprecationChecks == DeprecationChecks.IGNORES

        and:
        !executionOf { withoutDeprecationChecks() }.allArguments.contains('--warning-mode')
    }

    def "can set default character encoding"() {
        expect:
        executionDefaults.defaultCharacterEncoding == CharacterEncoding.unset()
        !executionDefaults.allArguments.any { it.startsWith('-Dfile.encoding') }

        and:
        executionOf { withDefaultCharacterEncoding(Charset.defaultCharset()) }.defaultCharacterEncoding.get() == Charset.defaultCharset()
        executionOf { withDefaultCharacterEncoding(Charset.defaultCharset()).withDefaultCharacterEncoding(Charset.forName("UTF-8")) }.defaultCharacterEncoding.get() == Charset.forName("UTF-8")

        and:
        executionOf { withDefaultCharacterEncoding(Charset.forName('UTF-8')) }.allArguments.contains('-Dfile.encoding=UTF-8')
    }

    def "can set default locale"() {
        expect:
        executionDefaults.defaultLocale == defaultLocale()
        executionDefaults.allArguments.containsAll(["-Duser.language=${Locale.default.language}", "-Duser.country=${Locale.default.country}", "-Duser.variant=${Locale.default.variant}"]*.toString())

        and:
        executionOf { withDefaultLocale(Locale.CANADA_FRENCH) }.defaultLocale.get() == Locale.CANADA_FRENCH
        executionOf { withDefaultLocale(Locale.CANADA_FRENCH).withDefaultLocale(Locale.CHINESE) }.defaultLocale.get() == Locale.CHINESE

        and:
        !executionOf { withDefaultLocale(Locale.CANADA_FRENCH) }.allArguments.containsAll(["-Duser.language=${Locale.CANADA_FRENCH.language}", "-Duser.country=${Locale.CANADA_FRENCH.country}", "-Duser.variant=${Locale.CANADA_FRENCH.variant}"])
    }

    def "can enable welcome message"() {
        expect:
        executionDefaults.welcomeMessageRendering == WelcomeMessage.DISABLED
        executionDefaults.allArguments.contains('-Dorg.gradle.internal.launcher.welcomeMessageEnabled=false')

        and:
        executionOf { withWelcomeMessageEnabled() }.welcomeMessageRendering == WelcomeMessage.ENABLED
        executionOf { withWelcomeMessageEnabled().withWelcomeMessageEnabled() }.welcomeMessageRendering == WelcomeMessage.ENABLED

        and:
        !executionOf { withWelcomeMessageEnabled() }.allArguments.contains('-Dorg.gradle.internal.launcher.welcomeMessageEnabled=false')
        executionOf { withWelcomeMessageEnabled() }.allArguments.contains('-Dorg.gradle.internal.launcher.welcomeMessageEnabled=true')
    }

    def "can enable rich console"() {
        expect:
        executionDefaults.consoleType == ConsoleType.DEFAULT
        !executionDefaults.allArguments.contains('--console')

        and:
        executionOf { withRichConsoleEnabled() }.consoleType == ConsoleType.RICH
        executionOf { withRichConsoleEnabled().withRichConsoleEnabled() }.consoleType == ConsoleType.RICH

        and:
        executionOf { withRichConsoleEnabled() }.allArguments.containsAll(['--console', 'rich'])
    }

    def "can configure to publish build scans"() {
        expect:
        executionDefaults.buildScan == BuildScan.DISABLED
        !executionDefaults.allArguments.contains('--scan')

        and:
        executionOf { publishBuildScans() }.buildScan == BuildScan.ENABLED
        executionOf { publishBuildScans().publishBuildScans() }.buildScan == BuildScan.ENABLED

        and:
        executionOf { publishBuildScans() }.allArguments.contains('--scan')
    }

    def "can set user home directory"() {
        expect:
        !executionDefaults.userHomeDirectory.present
        !executionDefaults.allArguments.any {it.startsWith('-Duser.home') }

        and:
        executionOf { withUserHomeDirectory(file('user-home')) }.userHomeDirectory.get() == file('user-home')
        executionOf { withUserHomeDirectory(file('a')).withUserHomeDirectory(file('b')) }.userHomeDirectory.get() == file('b')

        and:
        executionOf { withUserHomeDirectory(file('user-home')) }.allArguments.contains("-Duser.home=${file('user-home').absolutePath}".toString())
    }

    def "can set Gradle user home directory"() {
        expect:
        def defaultTestKitDirectory = new File("${SystemUtils.JAVA_IO_TMPDIR}/.gradle-test-kit-${SystemUtils.USER_NAME}").canonicalFile
        executionDefaults.gradleUserHomeDirectory == GradleUserHomeDirectory.of(defaultTestKitDirectory)
        executionDefaults.allArguments.containsAll(['--gradle-user-home', defaultTestKitDirectory.absolutePath])

        and:
        executionOf { withGradleUserHomeDirectory(file('user-home')) }.gradleUserHomeDirectory.get() == file('user-home')
        executionOf { withGradleUserHomeDirectory(file('a')).withGradleUserHomeDirectory(file('b')) }.gradleUserHomeDirectory.get() == file('b')

        and:
        executionOf { withGradleUserHomeDirectory(file('user-home')) }.allArguments.containsAll(['--gradle-user-home', file('user-home').absolutePath])
    }

    def "can set environment variables"() {
        expect:
        !executionDefaults.environmentVariables.isPresent()

        and:
        executionOf { withEnvironmentVariables([A: 'a']) }.environmentVariables.get() == [A: 'a']
        executionOf { withEnvironmentVariables([A: 'a']).withEnvironmentVariables([A: 'gg']) }.environmentVariables.get() == [A: 'gg']
        executionOf { withEnvironmentVariables([A: 'a']).withEnvironmentVariables([B: 'b']) }.environmentVariables.get() == [A: 'a', B: 'b']
    }

    def "can set standard output"() {
        given:
        def stdout = new StringWriter()

        expect:
        executionDefaults.standardOutput == StandardOutput.forwardToStandardOutput()

        and:
        executionOf { forwardStandardOutput(stdout) }.standardOutput != StandardOutput.forwardToStandardOutput()
    }

    def "can set standard error"() {
        given:
        def stderr = new StringWriter()

        expect:
        executionDefaults.standardError == StandardOutput.forwardToStandardError()

        and:
        executionOf { forwardStandardError(stderr) }.standardError != StandardOutput.forwardToStandardError()
    }

    static GradleExecutionContext getExecutionDefaults() {
        def executor = new CapturingGradleExecutor()
        GradleRunner.create(executor).build()
        return executor.parameters
    }

    static GradleExecutionContext executionOf(@DelegatesTo(GradleRunner) Closure<GradleRunner> runnerClosure) {
        def executor = new CapturingGradleExecutor()
        def runner = GradleRunner.create(executor)
        runnerClosure.delegate = runner
        runnerClosure.call(runner).build()
        return executor.parameters
    }

    private static class CapturingGradleExecutor implements GradleExecutor {
        private GradleExecutionContext parameters
        private boolean executed = false

        GradleExecutionContext getParameters() {
            assert executed
            return parameters
        }

        @Override
        GradleExecutionResult run(GradleExecutionContext parameters) {
            this.parameters = parameters
            this.executed = true
            return new GradleExecutionResult() {
                @Override
                String getOutput() {
                    return ''
                }

                @Override
                boolean isSuccessful() {
                    return true
                }
            }
        }
    }
}
