package dev.gradleplugins.test.fixtures.gradle.executer.internal.parameters;

import java.util.Collections;
import java.util.Map;

@Deprecated
public final class TemporaryDirectoryParameter extends GradleExecutionParameterImpl<TemporaryDirectory> implements JvmSystemPropertyParameter<TemporaryDirectory>, DirectoryParameter<TemporaryDirectory> {
    public boolean hasWhitespace() {
        if (isPresent()) {
            return get().getAbsolutePath().contains(" ");
        }
        return false;
    }

    public static TemporaryDirectoryParameter implicit() {
        return noValue(TemporaryDirectoryParameter.class);
    }

    public static TemporaryDirectoryParameter explicit(TemporaryDirectory directory) {
        return fixed(TemporaryDirectoryParameter.class, directory);
    }

    @Override
    public Map<String, String> getAsJvmSystemProperties() {
        if (isPresent()) {
            get().mkdirs(); // ignore return code
            String temporaryDirectoryPath = get().getAbsolutePath();
            return Collections.singletonMap("java.io.tmpdir", temporaryDirectoryPath);
        }
        return Collections.emptyMap();
    }
}
