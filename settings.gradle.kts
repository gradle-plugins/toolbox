rootProject.name = "toolbox"

include("gradle-plugin-development")
include("gradle-testkit-fixtures")

// Gradle APIs
include("gradle-api")
project(":gradle-api").projectDir = file("subprojects/gradle-api")

// Plugins
include("license-gradle-plugin")
project(":license-gradle-plugin").projectDir = file("subprojects/license-gradle-plugin")
include("scm-github-gradle-plugin")
project(":scm-github-gradle-plugin").projectDir = file("subprojects/scm-github-gradle-plugin")