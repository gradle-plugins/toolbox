package dev.gradleplugins.fixtures.gradle.runner.parameters;

import com.google.common.collect.ImmutableList;

import java.io.File;
import java.util.List;
import java.util.stream.Stream;

public final class InitScripts extends GradleExecutionParameterImpl<List<File>> implements GradleExecutionCommandLineParameter<List<File>>, GradleExecutionCollectionParameter<List<File>> {
    public InitScripts plus(File initScript) {
        return fixed(InitScripts.class, ImmutableList.<File>builder().addAll(get()).add(initScript).build());
    }

    public static InitScripts empty() {
        return fixed(InitScripts.class, ImmutableList.of());
    }

    @Override
    public List<String> getAsArguments() {
        return map(InitScripts::asArguments).orElseGet(ImmutableList::of);
    }

    private static List<String> asArguments(List<File> initScripts) {
        return initScripts.stream().map(File::getAbsolutePath).flatMap(InitScripts::withFlag).collect(ImmutableList.toImmutableList());
    }

    private static Stream<String> withFlag(String initScriptPath) {
        return Stream.of("--init-script", initScriptPath);
    }
}
