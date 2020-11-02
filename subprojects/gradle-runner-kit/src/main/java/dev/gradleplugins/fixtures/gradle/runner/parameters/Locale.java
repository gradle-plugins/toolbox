package dev.gradleplugins.fixtures.gradle.runner.parameters;

import lombok.val;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class Locale extends GradleExecutionParameterImpl<java.util.Locale> implements GradleExecutionJvmSystemPropertyParameter<java.util.Locale>, GradleExecutionParameter<java.util.Locale> {
    public static Locale of(java.util.Locale locale) {
        return fixed(Locale.class, locale);
    }

    public static Locale unset() {
        return noValue(Locale.class);
    }

    public static Locale defaultLocale() {
        return fixed(Locale.class, java.util.Locale.getDefault());
    }

    @Override
    public Map<String, String> getAsJvmSystemProperties() {
        return map(Locale::asJvmSystemProperties).orElseGet(Collections::emptyMap);
    }

    private static Map<String, String> asJvmSystemProperties(java.util.Locale locale) {
        val result = new LinkedHashMap<String, String>();
        result.put("user.language", locale.getLanguage());
        result.put("user.country", locale.getCountry());
        result.put("user.variant", locale.getVariant());
        return result;
    }
}
