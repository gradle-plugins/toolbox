package dev.gradleplugins;

import org.gradle.api.JavaVersion;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.util.GradleVersion;

import java.util.Optional;

/**
 * Query Gradle runtime compatibility information for specific Gradle version.
 * It supports the Groovy and Kotlin version packaged for each Gradle version.
 * It also supports the minimum Java version supported by each Gradle version.
 */
public final class GradleRuntimeCompatibility {
    private static final Logger LOGGER = Logging.getLogger(GradleRuntimeCompatibility.class);
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
    @Deprecated
    public static String groovyVersionOf(org.gradle.util.VersionNumber gradleVersion) {
        return groovyVersionOf(VersionNumber.parse(gradleVersion.toString()));
    }

    private static String groovyVersionOf(VersionNumber gradleVersion) {
        // Use `find ~/.gradle/wrapper -name "groovy-all-*"` once the distribution was downloaded locally
        switch (String.format("%d.%d", gradleVersion.getMajor(), gradleVersion.getMinor())) {
            case "1.12":
                return "1.8.6";
            case "2.0":
                return "2.3.3";
            case "2.1":
            case "2.2":
                return "2.3.6";
            case "2.3":
                return "2.3.9";
            case "2.4":
            case "2.5":
            case "2.6":
            case "2.7":
                return "2.3.10";
            case "2.8":
            case "2.9":
            case "2.10":
            case "2.11":
            case "2.12":
            case "2.13":
            case "2.14":
                return "2.4.4";
            case "3.0":
            case "3.1":
            case "3.2":
            case "3.3":
            case "3.4":
                return "2.4.7";
            case "3.5":
                return "2.4.10";
            case "4.0":
            case "4.1":
                return "2.4.11";
            case "4.2":
            case "4.3":
            case "4.4":
            case "4.5":
            case "4.6":
            case "4.7":
            case "4.8":
            case "4.9":
                return "2.4.12";
            case "4.10":
                return "2.4.15";
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
            case "6.6":
            case "6.7":
            case "6.8":
            case "6.9":
                return "2.5.12"; //"org.gradle.groovy:groovy-all:1.3-2.5.12";
            case "7.0":
            case "7.1":
                return "3.0.7";
            case "7.2":
                return "3.0.8";
            case "7.3":
            case "7.4":
                return "3.0.9";
            case "7.5":
                return "3.0.10";
            default:
                LOGGER.warn(String.format("Unknown Groovy version for Gradle '%s', please open an issue on https://github.com/gradle-plugins/toolbox. Assuming value of the latest known version.", gradleVersion.toString()));
            case "7.6":
            case "8.0":
                return "3.0.13";
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
    @Deprecated
    public static JavaVersion minimumJavaVersionFor(org.gradle.util.VersionNumber gradleVersion) {
        return minimumJavaVersionFor(VersionNumber.parse(gradleVersion.toString()));
    }

    private static JavaVersion minimumJavaVersionFor(VersionNumber gradleVersion) {
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
            default:
                LOGGER.warn(String.format("Unknown minimum Java version for Gradle '%s', please open an issue on https://github.com/gradle-plugins/toolbox. Assuming value of the latest known version.", gradleVersion.toString()));
            case 5:
            case 6:
            case 7:
            case 8:
                return JavaVersion.VERSION_1_8;
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
    @Deprecated
    public static Optional<String> kotlinVersionOf(org.gradle.util.VersionNumber gradleVersion) {
        return kotlinVersionOf(VersionNumber.parse(gradleVersion.toString()));
    }

    private static Optional<String> kotlinVersionOf(VersionNumber gradleVersion) {
        // Use `find ~/.gradle/wrapper -name "kotlin-stdlib-*"` once the distribution was downloaded locally
        switch (String.format("%d.%d", gradleVersion.getMajor(), gradleVersion.getMinor())) {
            case "2.0":
            case "2.1":
            case "2.2":
            case "2.3":
            case "2.4":
            case "2.5":
            case "2.6":
            case "2.7":
            case "2.8":
            case "2.9":
            case "2.10":
            case "2.11":
            case "2.12":
            case "2.13":
            case "2.14":
                return Optional.empty();
            case "3.0":
                return Optional.of("1.1-M01"); // Does not exists
            case "3.1":
            case "3.2":
                return Optional.of("1.1.0-dev-2053"); // Found in https://dl.bintray.com/kotlin/kotlin-dev/
            case "3.3":
            case "3.4":
                return Optional.of("1.1-M02-8"); // Technically, the version in Gradle distribution is 1.1-M02, but only 1.1.-M02-8 in https://dl.bintray.com/kotlin/kotlin-dev/ can be found.
            case "3.5":
            case "4.0":
                return Optional.of("1.1.0");
            case "4.1":
                return Optional.of("1.1.3-2");
            case "4.2":
                return Optional.of("1.1.4-3");
            case "4.3":
            case "4.4":
                return Optional.of("1.1.51");
            case "4.5":
                return Optional.of("1.2.0");
            case "4.6":
                return Optional.of("1.2.21");
            case "4.7":
                return Optional.of("1.2.31");
            case "4.8":
            case "4.9":
                return Optional.of("1.2.41");
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
            case "6.6":
            case "6.7":
                return Optional.of("1.3.72");
            case "6.8":
            case "6.9":
                return Optional.of("1.4.20");
            case "7.0":
            case "7.1":
                return Optional.of("1.4.31");
            case "7.2":
                return Optional.of("1.5.21");
            case "7.3":
            case "7.4":
                return Optional.of("1.5.31");
            case "7.5":
                return Optional.of("1.6.21");
            case "7.6":
                return Optional.of("1.7.10");
            default:
                LOGGER.warn(String.format("Unknown Kotlin version for Gradle '%s', please open an issue on https://github.com/gradle-plugins/toolbox. Assuming value of the latest known version.", gradleVersion.toString()));
            case "8.0":
                return Optional.of("1.8.10");
        }
    }

    /**
     * Returns the last patched Gradle version for the specified Gradle version.
     * For example, passing Gradle version {@literal 6.2} would return {@literal 6.2.2}, the last patched version of {@literal 6.2.x}.
     *
     * @param gradleVersion  the Gradle version to find the last patched version, must not be null.
     * @return the latest patched version for the specified version, never null
     */
    public static String lastPatchedVersionOf(String gradleVersion) {
        return lastPatchedVersionOf(VersionNumber.parse(gradleVersion));
    }

    private static String lastPatchedVersionOf(VersionNumber gradleVersion) {
        switch (String.format("%d.%d", gradleVersion.getMajor(), gradleVersion.getMinor())) {
            case "0.9": return "0.9.2";
            case "2.2": return "2.2.1";
            case "2.14": return "2.14.1";
            case "3.2": return "3.2.1";
            case "3.4": return "3.4.1";
            case "3.5": return "3.5.1";
            case "4.0": return "4.0.2";
            case "4.2": return "4.2.1";
            case "4.3": return "4.3.1";
            case "4.4": return "4.4.1";
            case "4.5": return "4.5.1";
            case "4.8": return "4.8.1";
            case "4.10": return "4.10.3";
            case "5.1": return "5.1.1";
            case "5.2": return "5.2.1";
            case "5.3": return "5.3.1";
            case "5.4": return "5.4.1";
            case "5.5": return "5.5.1";
            case "5.6": return "5.6.4";
            case "6.0": return "6.0.1";
            case "6.1": return "6.1.1";
            case "6.2": return "6.2.2";
            case "6.4": return "6.4.1";
            case "6.5": return "6.5.1";
            case "6.6": return "6.6.1";
            case "6.7": return "6.7.1";
            case "6.8": return "6.8.3";
            case "6.9": return "6.9.3";
            case "7.0": return "7.0.2";
            case "7.1": return "7.1.1";
            case "7.3": return "7.3.3";
            case "7.4": return "7.4.2";
            case "7.5": return "7.5.1";
            case "8.0": return "8.0.1";
            default:
                if (gradleVersion.getPatch() == 0) {
                    return String.format("%d.%d%s", gradleVersion.getMajor(), gradleVersion.getMinor(), gradleVersion.getQualifier() == null ? "" : "-" + gradleVersion.getQualifier());
                }
                return gradleVersion.toString();
        }
    }
}
