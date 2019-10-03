rootProject.name = "toolbox"

include("gradle-testkit-fixtures")

// Gradle APIs
include("gradle-api")
project(":gradle-api").projectDir = file("subprojects/gradle-api")

// Plugins
include("license-gradle-plugin")
project(":license-gradle-plugin").projectDir = file("subprojects/license-gradle-plugin")
include("scm-github-gradle-plugin")
project(":scm-github-gradle-plugin").projectDir = file("subprojects/scm-github-gradle-plugin")

// Plugin developement
include("gradle-plugin-development")
project(":gradle-plugin-development").projectDir = file("subprojects/gradle-plugin-development")
include("gradle-plugin-development-annotation")
project(":gradle-plugin-development-annotation").projectDir = file("subprojects/gradle-plugin-development-annotation")
include("gradle-plugin-development-processor")
project(":gradle-plugin-development-processor").projectDir = file("subprojects/gradle-plugin-development-processor")
include("gradle-plugin-development-stubs")
project(":gradle-plugin-development-stubs").projectDir = file("subprojects/gradle-plugin-development-stubs")
