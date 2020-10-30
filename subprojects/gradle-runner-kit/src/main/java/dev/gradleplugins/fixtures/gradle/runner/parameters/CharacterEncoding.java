package dev.gradleplugins.fixtures.gradle.runner.parameters;

import com.google.common.collect.ImmutableMap;

import java.nio.charset.Charset;
import java.util.Map;

public final class CharacterEncoding extends GradleExecutionParameterImpl<Charset> implements GradleExecutionJvmSystemPropertyParameter<Charset> {
    public static CharacterEncoding of(Charset characterEncoding) {
        return fixed(CharacterEncoding.class, characterEncoding);
    }

    public static CharacterEncoding defaultCharset() {
        return fixed(CharacterEncoding.class, Charset.defaultCharset());
    }

    public static CharacterEncoding unset() {
        return noValue(CharacterEncoding.class);
    }

    @Override
    public Map<String, String> getAsJvmSystemProperties() {
        return map(CharacterEncoding::asJvmSystemProperties).orElseGet(ImmutableMap::of);
    }

    private static Map<String, String> asJvmSystemProperties(Charset characterEncoding) {
        return ImmutableMap.of("file.encoding", characterEncoding.name());
    }
}
