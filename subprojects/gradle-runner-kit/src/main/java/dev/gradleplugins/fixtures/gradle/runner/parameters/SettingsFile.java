package dev.gradleplugins.fixtures.gradle.runner.parameters;

import java.io.File;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;

public final class SettingsFile extends GradleExecutionParameterImpl<File> implements GradleExecutionCommandLineParameter<File> {
    public static SettingsFile unset() {
        return noValue(SettingsFile.class);
    }

    public static SettingsFile of(File settingsFile) {
        return fixed(SettingsFile.class, settingsFile);
    }

    @Override
    public List<String> getAsArguments() {
        return map(SettingsFile::asArguments).orElseGet(Collections::emptyList);
    }

    private static List<String> asArguments(File settingsFile) {
        return asList("--settings-file", settingsFile.getAbsolutePath());
    }
}
