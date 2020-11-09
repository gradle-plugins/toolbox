package dev.gradleplugins.test.fixtures.gradle.executer.internal.parameters;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Deprecated
public final class SettingsFileParameter extends GradleExecutionParameterImpl<RegularFile> implements CommandLineGradleExecutionParameter<RegularFile>, GradleExecutionParameter<RegularFile> {

    public static SettingsFileParameter unset() {
        return noValue(SettingsFileParameter.class);
    }

    public static SettingsFileParameter of(File settingsFile) {
        return fixed(SettingsFileParameter.class, new RegularFile() {
            @Override
            public File getAsFile() {
                return settingsFile;
            }
        });
    }

    @Override
    public List<String> getAsArguments() {
        if (isPresent()) {
            return Arrays.asList("--settings-file", get().getAbsolutePath());
        }
        return Collections.emptyList();
    }
}
