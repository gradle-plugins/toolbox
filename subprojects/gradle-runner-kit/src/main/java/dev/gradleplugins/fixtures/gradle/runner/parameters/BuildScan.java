package dev.gradleplugins.fixtures.gradle.runner.parameters;

import java.io.*;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import static java.util.Arrays.asList;

public enum BuildScan implements GradleExecutionCommandLineParameter<BuildScan> {
    ENABLED(BuildScan::getPublishBuildScanArguments), DISABLED(Collections::emptyList);

    private final Supplier<List<String>> argumentSupplier;
    BuildScan(Supplier<List<String>> argumentSupplier) {
        this.argumentSupplier = argumentSupplier;
    }

    private static File initScriptFile;
    private static List<String> getPublishBuildScanArguments() {
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
        return argumentSupplier.get();
    }
}
