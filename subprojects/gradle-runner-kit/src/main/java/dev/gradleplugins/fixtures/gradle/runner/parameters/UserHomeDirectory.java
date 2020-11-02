package dev.gradleplugins.fixtures.gradle.runner.parameters;

import java.io.File;
import java.util.Collections;
import java.util.Map;

import static java.util.Collections.singletonMap;

public final class UserHomeDirectory extends GradleExecutionParameterImpl<File> implements GradleExecutionJvmSystemPropertyParameter<File> {
    public static UserHomeDirectory unset() {
        return noValue(UserHomeDirectory.class);
    }

    public static UserHomeDirectory of(File userHomeDirectory) {
        return fixed(UserHomeDirectory.class, userHomeDirectory);
    }

    @Override
    public Map<String, String> getAsJvmSystemProperties() {
        return map(UserHomeDirectory::asJvmSystemProperties).orElseGet(Collections::emptyMap);
    }

    private static Map<String, String> asJvmSystemProperties(File userHomeDirectory) {
        return singletonMap("user.home", userHomeDirectory.getAbsolutePath());
    }
}
