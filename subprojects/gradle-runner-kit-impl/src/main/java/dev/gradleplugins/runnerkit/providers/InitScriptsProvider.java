package dev.gradleplugins.runnerkit.providers;

import dev.gradleplugins.runnerkit.*;
import lombok.val;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;

public final class InitScriptsProvider extends AbstractGradleExecutionProvider<List<File>> implements GradleExecutionCommandLineProvider {
    public InitScriptsProvider plus(File initScript) {
        val result = new ArrayList<File>();
        result.addAll(get());
        result.add(initScript);
        return fixed(InitScriptsProvider.class, unmodifiableList(result));
    }

    public static InitScriptsProvider empty() {
        return fixed(InitScriptsProvider.class, emptyList());
    }

    @Override
    public List<String> getAsArguments() {
        return map(InitScriptsProvider::asArguments).orElseGet(Collections::emptyList);
    }

    private static List<String> asArguments(List<File> initScripts) {
        return initScripts.stream().map(File::getAbsolutePath).flatMap(InitScriptsProvider::withFlag).collect(toList());
    }

    private static Stream<String> withFlag(String initScriptPath) {
        return Stream.of("--init-script", initScriptPath);
    }

    @Override
    public void validate(GradleExecutionContext context) {
        if (context.getArguments().get().stream().anyMatch(it -> it.startsWith("--init-script") || it.equals("-I"))) {
            throw new InvalidRunnerConfigurationException("Please use GradleRunner#usingInitScript(File) instead of using the command line flags.");
        }
    }
}
