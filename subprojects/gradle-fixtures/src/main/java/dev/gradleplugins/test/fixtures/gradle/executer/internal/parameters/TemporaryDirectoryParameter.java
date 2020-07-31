package dev.gradleplugins.test.fixtures.gradle.executer.internal.parameters;

import lombok.Value;

import java.util.Collections;
import java.util.Map;

public interface TemporaryDirectoryParameter extends JvmSystemPropertyParameter {
    boolean hasWhitespace();

    static TemporaryDirectoryParameter implicit() {
        return new ImplicitTemporaryDirectoryParameter();
    }

    static TemporaryDirectoryParameter explicit(TemporaryDirectory directory) {
        return new ExplicitTemporaryDirectoryParameter(directory);
    }

    class ImplicitTemporaryDirectoryParameter implements TemporaryDirectoryParameter {

        @Override
        public Map<String, String> getAsJvmSystemProperties() {
            return Collections.emptyMap();
        }

        @Override
        public boolean hasWhitespace() {
            return false;
        }
    }

    @Value
    class ExplicitTemporaryDirectoryParameter implements TemporaryDirectoryParameter {
        TemporaryDirectory temporaryDirectory;

        @Override
        public Map<String, String> getAsJvmSystemProperties() {
            temporaryDirectory.mkdirs(); // ignore return code
            String temporaryDirectoryPath = temporaryDirectory.getAbsolutePath();
            return Collections.singletonMap("java.io.tmpdir", temporaryDirectoryPath);
        }

        @Override
        public boolean hasWhitespace() {
            return temporaryDirectory.getAbsolutePath().contains(" ");
        }
    }
}
