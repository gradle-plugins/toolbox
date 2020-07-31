package dev.gradleplugins.test.fixtures.gradle.executer.internal.parameters;

import lombok.Value;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Map;

@Value(staticConstructor = "of")
public class CharacterEncodingParameter implements JvmSystemPropertyParameter<Charset> {
    Charset value;

    @Override
    public Map<String, String> getAsJvmSystemProperties() {
        return Collections.singletonMap("file.encoding", value.name());
    }

    public static CharacterEncodingParameter defaultCharset() {
        return of(Charset.defaultCharset());
    }

    @Override
    public Charset get() {
        return value;
    }

    @Override
    public Charset orElse(Charset other) {
        return value;
    }

    @Override
    public boolean isPresent() {
        return true;
    }
}
