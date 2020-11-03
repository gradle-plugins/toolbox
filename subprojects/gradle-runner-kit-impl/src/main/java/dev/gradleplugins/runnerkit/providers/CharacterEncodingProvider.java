package dev.gradleplugins.runnerkit.providers;

import dev.gradleplugins.runnerkit.GradleExecutionContext;
import dev.gradleplugins.runnerkit.InvalidRunnerConfigurationException;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Map;

import static java.util.Collections.singletonMap;

public final class CharacterEncodingProvider extends AbstractGradleExecutionProvider<Charset> implements GradleExecutionJvmSystemPropertyProvider {
    public static CharacterEncodingProvider of(Charset characterEncoding) {
        return fixed(CharacterEncodingProvider.class, characterEncoding);
    }

    public static CharacterEncodingProvider defaultCharset() {
        return fixed(CharacterEncodingProvider.class, Charset.defaultCharset());
    }

    public static CharacterEncodingProvider unset() {
        return noValue(CharacterEncodingProvider.class);
    }

    @Override
    public Map<String, String> getAsJvmSystemProperties() {
        return map(CharacterEncodingProvider::asJvmSystemProperties).orElseGet(Collections::emptyMap);
    }

    private static Map<String, String> asJvmSystemProperties(Charset characterEncoding) {
        return singletonMap("file.encoding", characterEncoding.name());
    }

    @Override
    public void validate(GradleExecutionContext context) {
        if (context.getArguments().get().stream().anyMatch(it -> it.startsWith("-Dfile.encoding"))) {
            throw new InvalidRunnerConfigurationException("Please use GradleRunner#withDefaultCharacterEncoding(Charset) instead of using the command line flags.");
        }
    }
}
