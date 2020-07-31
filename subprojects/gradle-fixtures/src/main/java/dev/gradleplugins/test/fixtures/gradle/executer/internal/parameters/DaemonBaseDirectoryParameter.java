package dev.gradleplugins.test.fixtures.gradle.executer.internal.parameters;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.gradle.launcher.daemon.configuration.DaemonBuildOptions;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public interface DaemonBaseDirectoryParameter extends JvmSystemPropertyParameter, DirectoryParameter {
    static DaemonBaseDirectoryParameter unset() {
        return new UnsetDaemonBaseDirectoryDirectoryParameter();
    }

    static DaemonBaseDirectoryParameter of(File daemonBaseDirectory) {
        return new DefaultDaemonBaseDirectoryDirectoryParameter(daemonBaseDirectory);
    }

    @Value
    @EqualsAndHashCode(callSuper = false)
    class UnsetDaemonBaseDirectoryDirectoryParameter extends UnsetParameter<File> implements DaemonBaseDirectoryParameter {}

    @Value
    class DefaultDaemonBaseDirectoryDirectoryParameter implements DaemonBaseDirectoryParameter {
        File value;

        @Override
        public File getAsFile() {
            return value;
        }

        @Override
        public Map<String, String> getAsJvmSystemProperties() {
            return Collections.singletonMap(DaemonBuildOptions.BaseDirOption.GRADLE_PROPERTY, value.getAbsolutePath());
        }
    }
}
