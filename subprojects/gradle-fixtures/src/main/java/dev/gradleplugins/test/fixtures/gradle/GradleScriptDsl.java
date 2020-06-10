package dev.gradleplugins.test.fixtures.gradle;

public enum GradleScriptDsl {
    GROOVY_DSL( "gradle", ""), KOTLIN_DSL("gradle.kts", "::class.java");

    private final String extension;
    private final String classNotationSuffix;

    GradleScriptDsl(String extension, String classNotationSuffix) {
        this.extension = extension;
        this.classNotationSuffix = classNotationSuffix;
    }

    public String getSettingsFileName() {
        return "settings." + extension;
    }

    public String getBuildFileName() {
        return "build." + extension;
    }

    public String asClassNotation(String name) {
        return name + classNotationSuffix;
    }
}
