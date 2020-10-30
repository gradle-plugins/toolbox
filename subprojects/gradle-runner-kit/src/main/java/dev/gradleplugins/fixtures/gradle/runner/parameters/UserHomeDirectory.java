package dev.gradleplugins.fixtures.gradle.runner.parameters;

import com.google.common.collect.ImmutableMap;

import java.io.File;
import java.util.Map;

public final class UserHomeDirectory extends GradleExecutionParameterImpl<File> implements GradleExecutionJvmSystemPropertyParameter<File> {
    public static UserHomeDirectory unset() {
        return noValue(UserHomeDirectory.class);
    }

    public static UserHomeDirectory of(File userHomeDirectory) {
        return fixed(UserHomeDirectory.class, userHomeDirectory);
    }

    @Override
    public Map<String, String> getAsJvmSystemProperties() {
        return map(UserHomeDirectory::asJvmSystemProperties).orElseGet(ImmutableMap::of);
    }

    private static Map<String, String> asJvmSystemProperties(File userHomeDirectory) {
        return ImmutableMap.of("user.home", userHomeDirectory.getAbsolutePath());
    }
}
