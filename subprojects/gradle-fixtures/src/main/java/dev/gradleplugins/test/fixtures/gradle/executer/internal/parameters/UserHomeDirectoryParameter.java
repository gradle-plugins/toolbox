package dev.gradleplugins.test.fixtures.gradle.executer.internal.parameters;

import lombok.EqualsAndHashCode;
import lombok.Value;

import java.io.File;
import java.util.Collections;
import java.util.Map;

public interface UserHomeDirectoryParameter extends JvmSystemPropertyParameter {
    static UserHomeDirectoryParameter unset() {
        return new UnsetUserHomeDirectoryParameter();
    }

    static UserHomeDirectoryParameter of(File userHomeDirectory) {
        return new DefaultUserHomeDirectoryParameter(userHomeDirectory);
    }

    @Value
    @EqualsAndHashCode(callSuper = false)
    class UnsetUserHomeDirectoryParameter extends UnsetParameter<File> implements UserHomeDirectoryParameter {}

    @Value
    class DefaultUserHomeDirectoryParameter implements UserHomeDirectoryParameter {
        File value;

        @Override
        public Map<String, String> getAsJvmSystemProperties() {
            return Collections.singletonMap("user.home", value.getAbsolutePath());
        }
    }
}
