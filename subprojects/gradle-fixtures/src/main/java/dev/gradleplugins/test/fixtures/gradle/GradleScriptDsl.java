package dev.gradleplugins.test.fixtures.gradle;

public enum GradleScriptDsl {
    GROOVY_DSL( "gradle"), KOTLIN_DSL("gradle.kts");

    private final String extension;

    GradleScriptDsl(String extension) {
        this.extension = extension;
    }

    public String getSettingsFileName() {
        return "settings." + extension;
    }
}
