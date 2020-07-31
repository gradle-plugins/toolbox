package dev.gradleplugins.test.fixtures.gradle.executer.internal.parameters;

import lombok.Value;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Value(staticConstructor = "of")
public class CharacterEncodingParameter implements JvmSystemPropertyParameter {
    Charset value;

    @Override
    public Map<String, String> getAsJvmSystemProperties() {
        return Collections.singletonMap("file.encoding", value.name());
    }

    public static CharacterEncodingParameter defaultCharset() {
        return of(Charset.defaultCharset());
    }
}
