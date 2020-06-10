package dev.gradleplugins;

import org.gradle.api.JavaVersion;
import org.gradle.util.VersionNumber;

import java.util.Optional;

/**
 * Query Gradle runtime compatibility information for specific Gradle version.
 * It support the Groovy and Kotlin version packaged for each Gradle version.
 * It also support the minimum Java version supported by each Gradle version.
 */
public class GradleRuntimeCompatibility {
    /**
     * Returns the Groovy version packaged with the specified Gradle version.
     *
     * @param gradleVersion a specific Gradle version
     * @return the Groovy version of the specified Gradle version, never null.
     */
    public static String groovyVersionOf(String gradleVersion) {
        return groovyVersionOf(VersionNumber.parse(gradleVersion));
    }

    /**
     * Returns the Groovy version packaged with the specified Gradle version.
     *
     * @param gradleVersion a specific Gradle version
     * @return the Groovy version of the specified Gradle version, never null.
     */
    public static String groovyVersionOf(VersionNumber gradleVersion) {
        // Use `find ~/.gradle/wrapper -name "groovy-all-*"` once the distribution was downloaded locally
        switch (String.format("%d.%d", gradleVersion.getMajor(), gradleVersion.getMinor())) {
            case "1.12":
                return "1.8.6";
            case "2.14":
                return "2.4.4";
            case "3.0":
                return "2.4.7";
            case "3.5":
                return "2.4.10";
            case "4.0":
                return "2.4.11";
            case "4.3":
                return "2.4.12";
            case "5.0":
            case "5.1":
            case "5.2":
            case "5.3":
            case "5.4":
            case "5.5":
                return "2.5.4"; //"org.gradle.groovy:groovy-all:1.0-2.5.4";
            case "5.6":
                return "2.5.4"; //"org.gradle.groovy:groovy-all:1.3-2.5.4";
            case "6.0":
            case "6.1":
            case "6.2":
                return "2.5.8"; //"org.gradle.groovy:groovy-all:1.3-2.5.8";
            case "6.3":
            case "6.4":
                return "2.5.10"; //"org.gradle.groovy:groovy-all:1.3-2.5.10";
            case "6.5":
                return "2.5.11"; //"org.gradle.groovy:groovy-all:1.3-2.5.11";
            default:
                throw new IllegalArgumentException(String.format("Unknown Groovy version for Gradle '%s', please open an issue on https://github.com/gradle-plugins/toolbox.", gradleVersion.toString()));
        }
    }

    /**
     * Returns the minimum Java version of the specified Gradle version.
     *
     * @param gradleVersion a specific Gradle version
     * @return the minimum Java version for the specified Gradle version, never null.
     */
    public static JavaVersion minimumJavaVersionFor(String gradleVersion) {
        return minimumJavaVersionFor(VersionNumber.parse(gradleVersion));
    }

    /**
     * Returns the minimum Java version of the specified Gradle version.
     *
     * @param gradleVersion a specific Gradle version
     * @return the minimum Java version for the specified Gradle version, never null.
     */
    public static JavaVersion minimumJavaVersionFor(VersionNumber gradleVersion) {
        switch (gradleVersion.getMajor()) {
            case 0:
                throw new UnsupportedOperationException("Minimum Java version for Gradle version below 1.0 is unavailable, please open an issue on https://github.com/gradle-plugins/toolbox.");
            case 1:
                return JavaVersion.VERSION_1_5;
            case 2:
                return JavaVersion.VERSION_1_6;
            case 3:
            case 4:
                return JavaVersion.VERSION_1_7;
            case 5:
            case 6:
                return JavaVersion.VERSION_1_8;
            default:
                throw new IllegalArgumentException(String.format("Unknown minimum Java version for Gradle '%s', please open an issue on https://github.com/gradle-plugins/toolbox.", gradleVersion.toString()));
        }
    }

    /**
     * Returns the Kotlin version packaged with the specified Gradle version.
     * All Gradle version without Kotlin DSL support will return an empty value.
     *
     * @param gradleVersion a specific Gradle version
     * @return the Kotlin version for the specified Gradle version, never null.
     */
    public static Optional<String> kotlinVersionOf(String gradleVersion) {
        return kotlinVersionOf(VersionNumber.parse(gradleVersion));
    }

    /**
     * Returns the Kotlin version packaged with the specified Gradle version.
     * All Gradle version without Kotlin DSL support will return an empty value.
     *
     * @param gradleVersion a specific Gradle version
     * @return the Kotlin version for the specified Gradle version, never null.
     */
    public static Optional<String> kotlinVersionOf(VersionNumber gradleVersion) {
        // Use `find ~/.gradle/wrapper -name "kotlin-stdlib-*"` once the distribution was downloaded locally
        switch (String.format("%d.%d", gradleVersion.getMajor(), gradleVersion.getMinor())) {
            case "2.14":
                return Optional.empty();
            case "3.5":
                return Optional.of("1.1.0");
            case "4.5":
                return Optional.of("1.2.0");
            case "4.10":
                return Optional.of("1.2.61");
            case "5.0":
                return Optional.of("1.3.10");
            case "5.1":
                return Optional.of("1.3.11");
            case "5.2":
                return Optional.of("1.3.20");
            case "5.3":
            case "5.4":
                return Optional.of("1.3.21");
            case "5.5":
                return Optional.of("1.3.31");
            case "5.6":
                return Optional.of("1.3.41");
            case "6.0":
                return Optional.of("1.3.50");
            case "6.1":
            case "6.2":
                return Optional.of("1.3.61");
            case "6.3":
                return Optional.of("1.3.70");
            case "6.4":
                return Optional.of("1.3.71");
            case "6.5":
                return Optional.of("1.3.72");
            default:
                throw new IllegalArgumentException(String.format("Unknown Kotlin version for Gradle '%s', please open an issue on https://github.com/gradle-plugins/toolbox.", gradleVersion.toString()));
        }
    }
}
