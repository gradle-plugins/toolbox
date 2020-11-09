package dev.gradleplugins.test.fixtures.gradle.executer.internal.parameters;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Deprecated
public final class LocaleParameter extends GradleExecutionParameterImpl<Locale> implements JvmSystemPropertyParameter<Locale>, GradleExecutionParameter<Locale> {
    public static LocaleParameter of(Locale locale) {
        return fixed(LocaleParameter.class, locale);
    }

    public static LocaleParameter unset() {
        return noValue(LocaleParameter.class);
    }

    @Override
    public Map<String, String> getAsJvmSystemProperties() {
        if (isPresent()) {
            return Collections.unmodifiableMap(new HashMap<String, String>() {{
                put("user.language", LocaleParameter.this.get().getLanguage());
                put("user.country", LocaleParameter.this.get().getCountry());
                put("user.variant", LocaleParameter.this.get().getVariant());
            }});
        }
        return Collections.emptyMap();
    }
}
