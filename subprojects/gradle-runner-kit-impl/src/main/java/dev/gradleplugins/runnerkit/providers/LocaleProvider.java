package dev.gradleplugins.runnerkit.providers;

import dev.gradleplugins.runnerkit.GradleExecutionContext;
import dev.gradleplugins.runnerkit.InvalidRunnerConfigurationException;
import lombok.val;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public final class LocaleProvider extends AbstractGradleExecutionProvider<Locale> implements GradleExecutionJvmSystemPropertyProvider {
    public static LocaleProvider of(Locale locale) {
        return fixed(LocaleProvider.class, locale);
    }

    public static LocaleProvider unset() {
        return noValue(LocaleProvider.class);
    }

    public static LocaleProvider defaultLocale() {
        return fixed(LocaleProvider.class, java.util.Locale.getDefault());
    }

    @Override
    public Map<String, String> getAsJvmSystemProperties() {
        return map(LocaleProvider::asJvmSystemProperties).orElseGet(Collections::emptyMap);
    }

    private static Map<String, String> asJvmSystemProperties(Locale locale) {
        val result = new LinkedHashMap<String, String>();
        result.put("user.language", locale.getLanguage());
        result.put("user.country", locale.getCountry());
        result.put("user.variant", locale.getVariant());
        return result;
    }

    @Override
    public void validate(GradleExecutionContext context) {
        if (context.getArguments().get().stream().anyMatch(it -> it.startsWith("-Duser.language") || it.startsWith("-Duser.country") || it.startsWith("-Duser.variant"))) {
            throw new InvalidRunnerConfigurationException("Please use GradleRunner#withDefaultLocale(Locale) instead of using the command line flags.");
        }
    }
}
