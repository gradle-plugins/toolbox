package dev.gradleplugins.test.fixtures.gradle.executer.internal.parameters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class InitScriptsParameter extends GradleExecutionParameterImpl<List<RegularFile>> implements CommandLineGradleExecutionParameter<List<RegularFile>>, GradleExecutionParameter<List<RegularFile>> {
    public InitScriptsParameter plus(RegularFile initScript) {
        return fixed(InitScriptsParameter.class, Collections.unmodifiableList(new ArrayList<RegularFile>() {{
            addAll(InitScriptsParameter.this.get());
            add(initScript);
        }}));
    }

    public static InitScriptsParameter empty() {
        return fixed(InitScriptsParameter.class, Collections.emptyList());
    }

    @Override
    public List<String> getAsArguments() {
        if (isPresent()) {
            return Collections.unmodifiableList(get().stream().flatMap(initScript -> Stream.of("--init-script", initScript.getAbsolutePath())).collect(Collectors.toList()));
        }
        return Collections.emptyList();
    }
}
