package dev.gradleplugins.runnerkit;

import dev.gradleplugins.runnerkit.providers.BeforeExecute;

import java.util.ArrayList;
import java.util.List;

abstract class AbstractGradleExecutor implements GradleExecutor {
    @Override
    public final GradleExecutionResult run(GradleExecutionContext parameters) {
//        if (!parameters.getProjectDirectory().isPresent() && !parameters.getWorkingDirectory().isPresent()) {
//            throw new InvalidRunnerConfigurationException("Please specify a project directory before executing the build");
//        }

//        if (parameters.getEnvironmentVariables().isPresent() && debug) {
//            throw new InvalidRunnerConfigurationException("Debug mode is not allowed when environment variables are specified. " +
//                    "Debug mode runs 'in process' but we need to fork a separate process to pass environment variables. " +
//                    "To run with debug mode, please remove environment variables.");
//        }

//        File testKitDir = createTestKitDir(testKitDirProvider);
//
//        GradleProvider effectiveDistribution = gradleProvider == null ? findGradleInstallFromGradleRunner() : gradleProvider;

        return doRun(fireBeforeExecute(parameters));
    }

    private GradleExecutionContext fireBeforeExecute(GradleExecutionContext parameters) {
        GradleRunnerParameters.allExecutionParameters(parameters).stream().filter(it -> it instanceof BeforeExecute).forEach(action -> ((BeforeExecute) action).accept(parameters));
        return parameters;
    }

    protected abstract GradleExecutionResult doRun(GradleExecutionContext parameters);

    protected static List<String> getImplicitBuildJvmArgs() {
        List<String> buildJvmOpts = new ArrayList<>();
        buildJvmOpts.add("-ea");

//        if (isDebug()) {
//            buildJvmOpts.addAll(DEBUG_ARGS);
//        }
//        if (isProfile()) {
//            buildJvmOpts.add(profiler);
//        }

//        if (isSharedDaemons()) {
        buildJvmOpts.add("-Xms256m");
        buildJvmOpts.add("-Xmx1024m");
//        } else {
//            buildJvmOpts.add("-Xms256m");
//            buildJvmOpts.add("-Xmx512m");
//        }
//        if (JVM_VERSION_DETECTOR.getJavaVersion(Jvm.forHome(getJavaHome())).compareTo(JavaVersion.VERSION_1_8) < 0) {
//            // Although Gradle isn't supported on earlier versions, some tests do run it using Java 6 and 7 to verify it behaves well in this case
//            buildJvmOpts.add("-XX:MaxPermSize=320m");
//        } else {
        buildJvmOpts.add("-XX:MaxMetaspaceSize=512m");
//        }
        buildJvmOpts.add("-XX:+HeapDumpOnOutOfMemoryError");
//        buildJvmOpts.add("-XX:HeapDumpPath=" + buildContext.getGradleUserHomeDir());
        return buildJvmOpts;
    }


//    private File createTestKitDir(TestKitDirProvider testKitDirProvider) {
//        File dir = testKitDirProvider.getDir();
//        if (dir.isDirectory()) {
//            if (!dir.canWrite()) {
//                throw new InvalidRunnerConfigurationException("Unable to write to test kit directory: " + dir.getAbsolutePath());
//            }
//            return dir;
//        } else if (dir.exists() && !dir.isDirectory()) {
//            throw new InvalidRunnerConfigurationException("Unable to use non-directory as test kit directory: " + dir.getAbsolutePath());
//        } else if (dir.mkdirs() || dir.isDirectory()) {
//            return dir;
//        } else {
//            throw new InvalidRunnerConfigurationException("Unable to create test kit directory: " + dir.getAbsolutePath());
//        }
//    }
//
//    private static GradleProvider findGradleInstallFromGradleRunner() {
//        GradleInstallation gradleInstallation = CurrentGradleInstallation.get();
//        if (gradleInstallation == null) {
//            String messagePrefix = "Could not find a Gradle installation to use based on the location of the GradleRunner class";
//            try {
//                File classpathForClass = ClasspathUtil.getClasspathForClass(GradleRunner.class);
//                messagePrefix += ": " + classpathForClass.getAbsolutePath();
//            } catch (Exception ignore) {
//                // ignore
//            }
//            throw new InvalidRunnerConfigurationException(messagePrefix + ". Please specify a Gradle runtime to use via GradleRunner.withGradleVersion() or similar.");
//        }
//        return GradleProvider.installation(gradleInstallation.getGradleHome());
//    }
}
