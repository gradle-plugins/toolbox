package dev.gradleplugins.test.fixtures.gradle.executer.internal.parameters;

import lombok.EqualsAndHashCode;
import lombok.Value;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface InitScriptsParameter extends CommandLineGradleParameter {
    List<String> getAsArguments();

    InitScriptsParameter plus(File initScript);

    static InitScriptsParameter empty() {
        return new EmptyInitScriptsParameter();
    }

    @Value
    @EqualsAndHashCode(callSuper = false)
    class EmptyInitScriptsParameter extends UnsetParameter<File> implements InitScriptsParameter {
        @Override
        public InitScriptsParameter plus(File initScript) {
            return new DefaultInitScriptsParameter(Collections.singletonList(initScript));
        }
    }

    @Value
    class DefaultInitScriptsParameter implements InitScriptsParameter {
        List<File> value;

        @Override
        public List<String> getAsArguments() {
            return Collections.unmodifiableList(value.stream().flatMap(initScript -> Stream.of("--init-script", initScript.getAbsolutePath())).collect(Collectors.toList()));
        }

        @Override
        public InitScriptsParameter plus(File initScript) {
            return new DefaultInitScriptsParameter(Collections.unmodifiableList(new ArrayList<File>() {{
                addAll(value);
                add(initScript);
            }}));
        }
    }
}
