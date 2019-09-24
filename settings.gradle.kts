rootProject.name = "toolbox"

include("gradle-plugin-development")
include("gradle-testkit-fixtures")

// Gradle APIs
include("gradle-api")
project(":gradle-api").projectDir = file("subprojects/gradle-api")
