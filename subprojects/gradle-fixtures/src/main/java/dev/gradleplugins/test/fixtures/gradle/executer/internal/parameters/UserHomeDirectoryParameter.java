package dev.gradleplugins.test.fixtures.gradle.executer.internal.parameters;

import java.util.Collections;
import java.util.Map;

@Deprecated
public final class UserHomeDirectoryParameter extends GradleExecutionParameterImpl<UserHomeDirectory> implements JvmSystemPropertyParameter<UserHomeDirectory>, GradleExecutionParameter<UserHomeDirectory> {
    public static UserHomeDirectoryParameter unset() {
        return noValue(UserHomeDirectoryParameter.class);
    }

    public static UserHomeDirectoryParameter of(UserHomeDirectory userHomeDirectory) {
        return fixed(UserHomeDirectoryParameter.class, userHomeDirectory);
    }

    @Override
    public Map<String, String> getAsJvmSystemProperties() {
        if (isPresent()) {
            return Collections.singletonMap("user.home", get().getAbsolutePath());
        }
        return Collections.emptyMap();
    }
}
