package dev.gradleplugins.fixtures.gradle.runner.parameters;

import lombok.val;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;

public final class InitScripts extends GradleExecutionParameterImpl<List<File>> implements GradleExecutionCommandLineParameter<List<File>>, GradleExecutionCollectionParameter<List<File>> {
    public InitScripts plus(File initScript) {
        val result = new ArrayList<File>();
        result.addAll(get());
        result.add(initScript);
        return fixed(InitScripts.class, unmodifiableList(result));
    }

    public static InitScripts empty() {
        return fixed(InitScripts.class, emptyList());
    }

    @Override
    public List<String> getAsArguments() {
        return map(InitScripts::asArguments).orElseGet(Collections::emptyList);
    }

    private static List<String> asArguments(List<File> initScripts) {
        return initScripts.stream().map(File::getAbsolutePath).flatMap(InitScripts::withFlag).collect(toList());
    }

    private static Stream<String> withFlag(String initScriptPath) {
        return Stream.of("--init-script", initScriptPath);
    }
}
