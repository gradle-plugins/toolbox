package dev.gradleplugins.fixtures.gradle.runner.parameters;

import com.google.common.collect.ImmutableList;

import java.io.File;
import java.util.List;

public final class SettingsFile extends GradleExecutionParameterImpl<File> implements GradleExecutionCommandLineParameter<File> {
    public static SettingsFile unset() {
        return noValue(SettingsFile.class);
    }

    public static SettingsFile of(File settingsFile) {
        return fixed(SettingsFile.class, settingsFile);
    }

    @Override
    public List<String> getAsArguments() {
        return map(SettingsFile::asArguments).orElseGet(ImmutableList::of);
    }

    private static List<String> asArguments(File settingsFile) {
        return ImmutableList.of("--settings-file", settingsFile.getAbsolutePath());
    }
}
