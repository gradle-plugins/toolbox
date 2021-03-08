package dev.gradleplugins.runnerkit

import dev.gradleplugins.fixtures.file.FileSystemFixture
import dev.gradleplugins.runnerkit.providers.BuildCacheProvider
import dev.gradleplugins.runnerkit.providers.BuildScanProvider
import dev.gradleplugins.runnerkit.providers.CharacterEncodingProvider
import dev.gradleplugins.runnerkit.providers.ConsoleTypeProvider
import dev.gradleplugins.runnerkit.providers.DeprecationChecksProvider
import dev.gradleplugins.runnerkit.providers.GradleUserHomeDirectoryProvider
import dev.gradleplugins.runnerkit.providers.InjectedClasspathProvider
import dev.gradleplugins.runnerkit.providers.LocaleProvider
import dev.gradleplugins.runnerkit.providers.MissingSettingsFilePolicyProvider
import dev.gradleplugins.runnerkit.providers.StacktraceProvider
import dev.gradleplugins.runnerkit.providers.StandardStreamProvider
import dev.gradleplugins.runnerkit.providers.WelcomeMessageProvider
import dev.gradleplugins.runnerkit.providers.WorkingDirectoryProvider
import org.apache.commons.lang3.SystemUtils
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.Subject

import java.nio.charset.Charset
import java.util.function.UnaryOperator

@Subject(GradleRunnerImpl)
class GradleRunnerImplTest extends Specification implements FileSystemFixture {
    @Rule TemporaryFolder temporaryFolder = new TemporaryFolder()

    @Override
    File getTestDirectory() {
        return temporaryFolder.root
    }

    def "can account for all execution parameters"() {
        expect:
        executionDefaults.executionParameters.size() == 28
    }

    def "can disable stacktrace"() {
        expect:
        executionDefaults.stacktrace == StacktraceProvider.show()
        executionDefaults.allArguments.contains('--stacktrace')

        and:
        executionOf { withStacktraceDisabled() }.stacktrace == StacktraceProvider.hide()
        executionOf { withStacktraceDisabled().withStacktraceDisabled() }.stacktrace == StacktraceProvider.hide()

        and:
        !executionOf { withStacktraceDisabled() }.allArguments.contains('--stacktrace')
    }

    def "can enable build cache"() {
        expect:
        executionDefaults.buildCache == BuildCacheProvider.disabled()
        !executionDefaults.allArguments.contains('--build-cache')

        and:
        executionOf { withBuildCacheEnabled() }.buildCache == BuildCacheProvider.enabled()

        and:
        executionOf { withBuildCacheEnabled() }.allArguments.contains('--build-cache')
        // TODO: Not sure if it should be allowed or not... my feeling is we should use a linked set instead
//        executionOf { withBuildCacheEnabled().withBuildCacheEnabled() }.getArguments().get().findAll { it == '--build-cache' }.size() == 1
    }

    def "can configure arguments"() {
        expect:
        executionDefaults.arguments.get() == []

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
        executionDefaults.missingSettingsFilePolicy == MissingSettingsFilePolicyProvider.createWhenMissing()

        and:
        executionOf { ignoresMissingSettingsFile() }.missingSettingsFilePolicy == MissingSettingsFilePolicyProvider.ignoresWhenMissing()
        executionOf { ignoresMissingSettingsFile().ignoresMissingSettingsFile() }.missingSettingsFilePolicy == MissingSettingsFilePolicyProvider.ignoresWhenMissing()
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
        executionDefaults.initScripts.get() == []

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
        executionDefaults.tasks.get() == []

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
        executionDefaults.deprecationChecks == DeprecationChecksProvider.fails()
        executionDefaults.allArguments.containsAll(['--warning-mode', 'fail'])

        and:
        executionOf { withoutDeprecationChecks() }.deprecationChecks == DeprecationChecksProvider.ignores()
        executionOf { withoutDeprecationChecks().withoutDeprecationChecks() }.deprecationChecks == DeprecationChecksProvider.ignores()

        and:
        !executionOf { withoutDeprecationChecks() }.allArguments.contains('--warning-mode')
    }

    def "can set default character encoding"() {
        expect:
        executionDefaults.defaultCharacterEncoding == CharacterEncodingProvider.defaultCharset()
        executionDefaults.allArguments.any { it == "-Dfile.encoding=${Charset.defaultCharset().name()}" }

        and:
        executionOf { withDefaultCharacterEncoding(Charset.defaultCharset()) }.defaultCharacterEncoding.get() == Charset.defaultCharset()
        executionOf { withDefaultCharacterEncoding(Charset.defaultCharset()).withDefaultCharacterEncoding(Charset.forName("UTF-8")) }.defaultCharacterEncoding.get() == Charset.forName("UTF-8")

        and:
        executionOf { withDefaultCharacterEncoding(Charset.forName('UTF-8')) }.allArguments.contains('-Dfile.encoding=UTF-8')
    }

    def "can set default locale"() {
        expect:
        executionDefaults.defaultLocale == LocaleProvider.defaultLocale()
        executionDefaults.allArguments.containsAll(["-Duser.language=${Locale.default.language}", "-Duser.country=${Locale.default.country}", "-Duser.variant=${Locale.default.variant}"]*.toString())

        and:
        executionOf { withDefaultLocale(Locale.CANADA_FRENCH) }.defaultLocale.get() == Locale.CANADA_FRENCH
        executionOf { withDefaultLocale(Locale.CANADA_FRENCH).withDefaultLocale(Locale.CHINESE) }.defaultLocale.get() == Locale.CHINESE

        and:
        !executionOf { withDefaultLocale(Locale.CANADA_FRENCH) }.allArguments.containsAll(["-Duser.language=${Locale.CANADA_FRENCH.language}", "-Duser.country=${Locale.CANADA_FRENCH.country}", "-Duser.variant=${Locale.CANADA_FRENCH.variant}"])
    }

    def "can enable welcome message"() {
        expect:
        executionDefaults.welcomeMessageRendering == WelcomeMessageProvider.disabled()
        executionDefaults.allArguments.contains('-Dorg.gradle.internal.launcher.welcomeMessageEnabled=false')

        and:
        executionOf { withWelcomeMessageEnabled() }.welcomeMessageRendering == WelcomeMessageProvider.enabled()
        executionOf { withWelcomeMessageEnabled().withWelcomeMessageEnabled() }.welcomeMessageRendering == WelcomeMessageProvider.enabled()

        and:
        !executionOf { withWelcomeMessageEnabled() }.allArguments.contains('-Dorg.gradle.internal.launcher.welcomeMessageEnabled=false')
        executionOf { withWelcomeMessageEnabled() }.allArguments.contains('-Dorg.gradle.internal.launcher.welcomeMessageEnabled=true')
    }

    def "can enable rich console"() {
        expect:
        executionDefaults.consoleType == ConsoleTypeProvider.defaultConsole()
        !executionDefaults.allArguments.contains('--console')

        and:
        executionOf { withRichConsoleEnabled() }.consoleType == ConsoleTypeProvider.richConsole()
        executionOf { withRichConsoleEnabled().withRichConsoleEnabled() }.consoleType == ConsoleTypeProvider.richConsole()

        and:
        executionOf { withRichConsoleEnabled() }.allArguments.containsAll(['--console', 'rich'])
    }

    def "can configure to publish build scans"() {
        expect:
        executionDefaults.buildScan == BuildScanProvider.disabled()
        !executionDefaults.allArguments.contains('--scan')

        and:
        executionOf { publishBuildScans() }.buildScan == BuildScanProvider.enabled()
        executionOf { publishBuildScans().publishBuildScans() }.buildScan == BuildScanProvider.enabled()

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
        executionDefaults.gradleUserHomeDirectory == GradleUserHomeDirectoryProvider.of(defaultTestKitDirectory)
        executionDefaults.allArguments.containsAll(['--gradle-user-home', defaultTestKitDirectory.absolutePath])

        and:
        executionOf { withGradleUserHomeDirectory(file('user-home')) }.gradleUserHomeDirectory.get() == file('user-home')
        executionOf { withGradleUserHomeDirectory(file('a')).withGradleUserHomeDirectory(file('b')) }.gradleUserHomeDirectory.get() == file('b')

        and:
        executionOf { withGradleUserHomeDirectory(file('user-home')) }.allArguments.containsAll(['--gradle-user-home', file('user-home').absolutePath])
    }

    def "can isolate Gradle user home directory"() {
        expect:
        executionOf { inDirectory(testDirectory).requireOwnGradleUserHomeDirectory() }.gradleUserHomeDirectory.get() == file('user-home')
        executionOf { requireOwnGradleUserHomeDirectory().inDirectory(testDirectory) }.gradleUserHomeDirectory.get() == file('user-home')
        executionOf { requireOwnGradleUserHomeDirectory().withGradleUserHomeDirectory(file('foo')) }.gradleUserHomeDirectory.get() == file('foo')
        executionOf { inDirectory(testDirectory).withGradleUserHomeDirectory(file('foo')).requireOwnGradleUserHomeDirectory() }.gradleUserHomeDirectory.get() == file('user-home')
    }

    def "can set environment variables"() {
        expect:
        !executionDefaults.environmentVariables.isPresent()

        and:
        executionOf { withEnvironmentVariables([A: 'a']) }.environmentVariables.get() == [A: 'a']
        executionOf { withEnvironmentVariables([A: 'a']).withEnvironmentVariables([A: 'gg']) }.environmentVariables.get() == [A: 'gg']
        executionOf { withEnvironmentVariables([A: 'a']).withEnvironmentVariables([B: 'b']) }.environmentVariables.get() == [B: 'b']

        and:
        executionOf { withEnvironmentVariable('A', 'a') }.environmentVariables.get() == System.getenv() + [A: 'a']
        executionOf { withEnvironmentVariable('A', 'a').withEnvironmentVariable('B', 'b') }.environmentVariables.get() == System.getenv() + [A: 'a', B: 'b']

        and:
        executionOf { withEnvironmentVars([A: 'a', B: 'b']) }.environmentVariables.get() == System.getenv() + [A: 'a', B: 'b']
        executionOf { withEnvironmentVars([A: 'a', B: 'b']).withEnvironmentVars([C: 'c']) }.environmentVariables.get() == System.getenv() + [A: 'a', B: 'b', C: 'c']

        and:
        executionOf { withEnvironment([A: 'a', B: 'b']) }.environmentVariables.get() == [A: 'a', B: 'b']
        executionOf { withEnvironment([A: 'a', B: 'b']).withEnvironment([C: 'c']) }.environmentVariables.get() == [C: 'c']
    }

    def "can set standard output"() {
        given:
        def stdout = new StringWriter()

        expect:
        executionDefaults.standardOutput == StandardStreamProvider.forwardToStandardOutput()

        and:
        executionOf { forwardStandardOutput(stdout) }.standardOutput != StandardStreamProvider.forwardToStandardOutput()
    }

    def "can set standard error"() {
        given:
        def stderr = new StringWriter()

        expect:
        executionDefaults.standardError == StandardStreamProvider.forwardToStandardError()

        and:
        executionOf { forwardStandardError(stderr) }.standardError != StandardStreamProvider.forwardToStandardError()
    }

    def "can set injected classpath"() {
        expect:
        executionDefaults.injectedClasspath == InjectedClasspathProvider.empty()

        and:
        executionOf { withPluginClasspath() }.injectedClasspath == InjectedClasspathProvider.fromPluginUnderTestMetadata()
        executionOf { withPluginClasspath([file('build/classes/java/main'), file('foo.jar'), file('bar.jar')])}.injectedClasspath == InjectedClasspathProvider.of([file('build/classes/java/main'), file('foo.jar'), file('bar.jar')])
        executionOf { withPluginClasspath().withPluginClasspath([file('build/classes/java/main')]) }.injectedClasspath == InjectedClasspathProvider.of([file('build/classes/java/main')])
        executionOf { withPluginClasspath([file('build/classes/java/main')]).withPluginClasspath() }.injectedClasspath == InjectedClasspathProvider.fromPluginUnderTestMetadata()
    }

    def "throws exception when getting unset working directory"() {
        when:
        newRunner().workingDirectory

        then:
        def ex = thrown(InvalidRunnerConfigurationException)
        ex.message == "Please use GradleRunner#inDirectory(File) API to configure a working directory for this runner."
    }

    def "returns the working directory when configured"() {
        expect:
        executionOf { inDirectory(testDirectory) }.workingDirectory == WorkingDirectoryProvider.of(testDirectory)
        executionOf { inDirectory(testDirectory.toPath()) }.workingDirectory == WorkingDirectoryProvider.of(testDirectory)

        and:
        executionOf { inDirectory({ testDirectory }) }.workingDirectory.get() == testDirectory
        executionOf { inDirectory({ testDirectory.toPath() }) }.workingDirectory.get() == testDirectory
    }

    def "throws exception when supplied working directory is not a File convertable type"() {
        when:
        executionOf { inDirectory({ new Object() }) }.workingDirectory.get()

        then:
        def ex = thrown(IllegalArgumentException.class)
        ex.message == 'Supplied working directory cannot be converted to a File instance: class java.lang.Object'
    }

    def "throws exception if configuration action return null"() {
        when:
        executionOf { configure {null } }

        then:
        def ex = thrown(NullPointerException)
        ex.message == "Please return a non-null GradleRunner from the configuration action when using GradleRunner#configure(action)."
    }

    def "returns runner returned from action"() {
        given:
        def runner = Stub(GradleRunner)

        expect:
        executionOf {
            assert configure {runner } == runner
            it
        }
    }

    def "calls configuration action"() {
        given:
        def action = Mock(UnaryOperator)

        when:
        executionOf { configure(action) }

        then:
        1 * action.apply(_) >> { args -> args[0] }
    }

    def "pass current runner to action"() {
        expect:
        executionOf { runner ->
            configure {
                assert it == runner
                it
            }
        }
    }

    static GradleRunner newRunner() {
        return GradleRunner.create(new CapturingGradleExecutor())
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
            ((GradleRunnerParameters) parameters).calculateValues()
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
