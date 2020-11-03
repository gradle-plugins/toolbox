package dev.gradleplugins.runnerkit.providers;

import dev.gradleplugins.runnerkit.GradleExecutionContext;
import dev.gradleplugins.runnerkit.InvalidRunnerConfigurationException;

import java.io.*;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

public final class BuildScanProvider extends AbstractGradleExecutionProvider<GradleExecutionContext.BuildScan> implements GradleExecutionCommandLineProvider {
    public static BuildScanProvider disabled() {
        return fixed(BuildScanProvider.class, GradleExecutionContext.BuildScan.DISABLED);
    }

    public static BuildScanProvider enabled() {
        return fixed(BuildScanProvider.class, GradleExecutionContext.BuildScan.ENABLED);
    }

    private File initScriptFile;
    private List<String> getPublishBuildScanArguments() {
        if (initScriptFile == null) {
            try {
                initScriptFile = File.createTempFile("build-scan.", ".init.gradle");
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        try (PrintWriter out = new PrintWriter(new FileOutputStream(initScriptFile))) {
            out.println("import org.gradle.util.GradleVersion");
            out.println("");
            out.println("def isTopLevelBuild = gradle.getParent() == null");
            out.println("");
            out.println("if (isTopLevelBuild) {");
            out.println("    def gradleVersion = GradleVersion.current().baseVersion");
            out.println("    def atLeastGradle5 = gradleVersion >= GradleVersion.version('5.0')");
            out.println("    def atLeastGradle6 = gradleVersion >= GradleVersion.version('6.0')");
            out.println("");
            out.println("    if (atLeastGradle6) {");
            out.println("        settingsEvaluated {");
            out.println("            if (it.pluginManager.hasPlugin('com.gradle.enterprise')) {");
            out.println("               configureExtension(it.extensions['gradleEnterprise'].buildScan)");
            out.println("            }");
            out.println("        }");
            out.println("    } else if (atLeastGradle5) {");
            out.println("        rootProject {");
            out.println("            if (it.pluginManager.hasPlugin('com.gradle.build-scan')) {");
            out.println("               configureExtension(extensions['buildScan'])");
            out.println("            }");
            out.println("        }");
            out.println("    }");
            out.println("}");
            out.println("");
            out.println("void configureExtension(extension) {");
            out.println("    extension.with {");
            out.println("        termsOfServiceUrl = 'https://gradle.com/terms-of-service'");
            out.println("        termsOfServiceAgree = 'yes'");
            out.println("    }");
            out.println("}");
        } catch (FileNotFoundException e) {
            throw new UncheckedIOException(e);
        }
        return asList("--init-script", initScriptFile.getAbsolutePath(), "--scan");
    }

    @Override
    public List<String> getAsArguments() {
        if (get().equals(GradleExecutionContext.BuildScan.ENABLED)) {
            return getPublishBuildScanArguments();
        }
        return emptyList();
    }

    @Override
    public void validate(GradleExecutionContext context) {
        if (get().equals(GradleExecutionContext.BuildScan.ENABLED)) {
            if (context.getArguments().get().contains("--scan")) {
                throw new InvalidRunnerConfigurationException("Please remove command line flag enabling build scan as it was already enabled via GradleRunner#publishBuildScans().");
            } else if (context.getArguments().get().contains("--no-scan")) {
                throw new InvalidRunnerConfigurationException("Please remove command line flag disabling build scan and any call to GradleRunner#publishBuildScans() for this runner as it is disabled by default for all toolbox runner.");
            }
        } else if (get().equals(GradleExecutionContext.BuildScan.DISABLED)) {
            if (context.getArguments().get().contains("--scan")) {
                throw new InvalidRunnerConfigurationException("Please use GradleRunner#publishBuildScans() instead of using flag in command line arguments.");
            } else if (context.getArguments().get().contains("--no-scan")) {
                throw new InvalidRunnerConfigurationException("Please remove command line flag disabling build scan as it is disabled by default for all toolbox runner.");
            }
        }
    }
}
