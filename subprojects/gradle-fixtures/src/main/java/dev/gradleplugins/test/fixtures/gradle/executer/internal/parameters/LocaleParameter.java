package dev.gradleplugins.test.fixtures.gradle.executer.internal.parameters;

import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public interface LocaleParameter extends JvmSystemPropertyParameter {
    static LocaleParameter of(Locale locale) {
        return new DefaultLocaleParameter(locale);
    }

    static LocaleParameter unset() {
        return new UnsetLocaleParameter();
    }

    @Value
    @EqualsAndHashCode(callSuper = false)
    class UnsetLocaleParameter extends UnsetParameter<Locale> implements LocaleParameter {}

    @Value
    class DefaultLocaleParameter implements LocaleParameter {
        Locale value;

        @Override
        public Map<String, String> getAsJvmSystemProperties() {
            return Collections.unmodifiableMap(new HashMap<String, String>() {{
                put("user.language", value.getLanguage());
                put("user.country", value.getCountry());
                put("user.variant", value.getVariant());
            }});
        }
    }
}
