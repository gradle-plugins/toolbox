package dev.gradleplugins.fixtures.gradle.runner.parameters;

import com.google.common.collect.ImmutableMap;

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
        return map(Locale::asJvmSystemProperties).orElseGet(ImmutableMap::of);
    }

    private static Map<String, String> asJvmSystemProperties(java.util.Locale locale) {
        return ImmutableMap.<String, String>builder()
                .put("user.language", locale.getLanguage())
                .put("user.country", locale.getCountry())
                .put("user.variant", locale.getVariant())
                .build();
    }
}
