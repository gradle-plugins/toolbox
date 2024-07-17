package dev.gradleplugins;

import org.gradle.api.JavaVersion;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;

/**
 * Query Gradle runtime compatibility information for specific Gradle version.
 * It supports the Groovy and Kotlin version packaged for each Gradle version.
 * It also supports the minimum Java version supported by each Gradle version.
 */
@Deprecated // use gradleRuntimeCompatibilities extension
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
            case "7.6":
            case "8.0":
                return "3.0.13";
            case "8.1":
                return "3.0.15";
            case "8.2":
            case "8.3":
            case "8.4":
            case "8.5":
            case "8.6":
            case "8.7":
                return "3.0.17";
            default:
                LOGGER.warn(String.format("Unknown Groovy version for Gradle '%s', please open an issue on https://github.com/gradle-plugins/toolbox. Assuming value of the latest known version.", gradleVersion.toString()));
            case "8.8":
            case "8.9":
                return "3.0.21";
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
            case "8.0":
            case "8.1":
                return Optional.of("1.8.10");
            case "8.2":
                return Optional.of("1.8.20");
            case "8.3":
                return Optional.of("1.9.0");
            case "8.4":
                return Optional.of("1.9.10");
            case "8.5":
            case "8.6":
                return Optional.of("1.9.20");
            case "8.7":
            case "8.8":
                return Optional.of("1.9.22");
            default:
                LOGGER.warn(String.format("Unknown Kotlin version for Gradle '%s', please open an issue on https://github.com/gradle-plugins/toolbox. Assuming value of the latest known version.", gradleVersion.toString()));
            case "8.9":
                return Optional.of("1.9.23");
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
            case "6.9": return "6.9.4";
            case "7.0": return "7.0.2";
            case "7.1": return "7.1.1";
            case "7.3": return "7.3.3";
            case "7.4": return "7.4.2";
            case "7.5": return "7.5.1";
            case "7.6": return "7.6.4";
            case "8.0": return "8.0.2";
            case "8.1": return "8.1.1";
            case "8.2": return "8.2.1";
            default:
                if (gradleVersion.getPatch() == 0) {
                    return String.format("%d.%d%s", gradleVersion.getMajor(), gradleVersion.getMinor(), gradleVersion.getQualifier() == null ? "" : "-" + gradleVersion.getQualifier());
                }
                return gradleVersion.toString();
        }
    }

    /**
     * Returns the last minor released Gradle version for the specified major Gradle version.
     * For example, passing Gradle version {@literal 7.3} would return {@literal 7.6}, the last minor release of {@literal 7.x}.
     * The function will return the last patched version as well.
     *
     * @param gradleVersion  the Gradle version to find the last minor release for, must not be null
     * @return the latest minor version for the specified version, never null
     */
    public static String lastMinorReleaseOf(String gradleVersion) {
        return lastMinorReleaseOf(VersionNumber.parse(gradleVersion));
    }

    private static String lastMinorReleaseOf(VersionNumber gradleVersion) {
        switch (gradleVersion.getMajor()) {
            case 0: return "0.9.2";
            case 1: return "1.14";
            case 2: return "2.14.1";
            case 3: return "3.5.1";
            case 4: return "4.10.3";
            case 5: return "5.6.4";
            case 6: return "6.9.4";
            case 7: return "7.6.4";
            case 8: return "8.9";
            default:
                throw new IllegalArgumentException(String.format("Unknown Gradle version, please open an issue on https://github.com/gradle-plugins/toolbox.", gradleVersion));
        }
    }

    /**
     * Represents, parses, and compares version numbers. Supports a couple of different schemes: <ul> <li>MAJOR.MINOR.MICRO-QUALIFIER (the default).</li> <li>MAJOR.MINOR.MICRO.PATCH-QUALIFIER.</li> </ul>
     *
     * <p>The {@link #parse} method handles missing parts and allows "." to be used instead of "-", and "_" to be used instead of "." for the patch number.
     *
     * <p>This class considers missing parts to be 0, so that "1.0" == "1.0.0" == "1.0.0_0".</p>
     *
     * <p>Note that this class considers "1.2.3-something" less than "1.2.3". Qualifiers are compared lexicographically ("1.2.3-alpha" < "1.2.3-beta") and case-insensitive ("1.2.3-alpha" <
     * "1.2.3.RELEASE").
     *
     * <p>To check if a version number is at least "1.2.3", disregarding a potential qualifier like "beta", use {@code version.getBaseVersion().compareTo(VersionNumber.parse("1.2.3")) >= 0}.
     */
    static final class VersionNumber implements Comparable<VersionNumber> {
        private static final DefaultScheme DEFAULT_SCHEME = new DefaultScheme();
        private static final SchemeWithPatchVersion PATCH_SCHEME = new SchemeWithPatchVersion();
        public static final VersionNumber UNKNOWN = version(0);

        private final int major;
        private final int minor;
        private final int micro;
        private final int patch;
        private final String qualifier;
        private final AbstractScheme scheme;

        public VersionNumber(int major, int minor, int micro, @Nullable String qualifier) {
            this(major, minor, micro, 0, qualifier, DEFAULT_SCHEME);
        }

        public VersionNumber(int major, int minor, int micro, int patch, @Nullable String qualifier) {
            this(major, minor, micro, patch, qualifier, PATCH_SCHEME);
        }

        private VersionNumber(int major, int minor, int micro, int patch, @Nullable String qualifier, AbstractScheme scheme) {
            this.major = major;
            this.minor = minor;
            this.micro = micro;
            this.patch = patch;
            this.qualifier = qualifier;
            this.scheme = scheme;
        }

        public int getMajor() {
            return major;
        }

        public int getMinor() {
            return minor;
        }

        public int getMicro() {
            return micro;
        }

        public int getPatch() {
            return patch;
        }

        public String getQualifier() {
            return qualifier;
        }

        public VersionNumber getBaseVersion() {
            return new VersionNumber(major, minor, micro, patch, null, scheme);
        }

        @Override
        public int compareTo(VersionNumber other) {
            if (major != other.major) {
                return major - other.major;
            }
            if (minor != other.minor) {
                return minor - other.minor;
            }
            if (micro != other.micro) {
                return micro - other.micro;
            }
            if (patch != other.patch) {
                return patch - other.patch;
            }
            return Comparator.nullsLast(Comparator.<String>naturalOrder()).compare(toLowerCase(qualifier), toLowerCase(other.qualifier));
        }

        public boolean equals(Object other) {
            return other instanceof VersionNumber && compareTo((VersionNumber) other) == 0;
        }

        public int hashCode() {
            int result = major;
            result = 31 * result + minor;
            result = 31 * result + micro;
            result = 31 * result + patch;
            result = 31 * result + Objects.hashCode(qualifier);
            return result;
        }

        public String toString() {
            return scheme.format(this);
        }

        public static VersionNumber version(int major) {
            return new VersionNumber(major, 0, 0, 0, null, DEFAULT_SCHEME);
        }

        /**
         * Returns the default MAJOR.MINOR.MICRO-QUALIFIER scheme.
         */
        public static Scheme scheme() {
            return DEFAULT_SCHEME;
        }

        /**
         * Returns the MAJOR.MINOR.MICRO.PATCH-QUALIFIER scheme.
         */
        public static Scheme withPatchNumber() {
            return PATCH_SCHEME;
        }

        public static VersionNumber parse(String versionString) {
            return DEFAULT_SCHEME.parse(versionString);
        }

        private String toLowerCase(@Nullable String string) {
            return string == null ? null : string.toLowerCase();
        }

        public interface Scheme {
            public VersionNumber parse(String value);

            public String format(VersionNumber versionNumber);
        }

        private abstract static class AbstractScheme implements Scheme {
            final int depth;

            protected AbstractScheme(int depth) {
                this.depth = depth;
            }

            @Override
            public VersionNumber parse(String versionString) {
                if (versionString == null || versionString.length() == 0) {
                    return UNKNOWN;
                }
                Scanner scanner = new Scanner(versionString);

                int major = 0;
                int minor = 0;
                int micro = 0;
                int patch = 0;

                if (!scanner.hasDigit()) {
                    return UNKNOWN;
                }
                major = scanner.scanDigit();
                if (scanner.isSeparatorAndDigit('.')) {
                    scanner.skipSeparator();
                    minor = scanner.scanDigit();
                    if (scanner.isSeparatorAndDigit('.')) {
                        scanner.skipSeparator();
                        micro = scanner.scanDigit();
                        if (depth > 3 && scanner.isSeparatorAndDigit('.', '_')) {
                            scanner.skipSeparator();
                            patch = scanner.scanDigit();
                        }
                    }
                }

                if (scanner.isEnd()) {
                    return new VersionNumber(major, minor, micro, patch, null, this);
                }

                if (scanner.isQualifier()) {
                    scanner.skipSeparator();
                    return new VersionNumber(major, minor, micro, patch, scanner.remainder(), this);
                }

                return UNKNOWN;
            }

            private static class Scanner {
                int pos;
                final String str;

                private Scanner(String string) {
                    this.str = string;
                }

                boolean hasDigit() {
                    return pos < str.length() && Character.isDigit(str.charAt(pos));
                }

                boolean isSeparatorAndDigit(char... separators) {
                    return pos < str.length() - 1 && oneOf(separators) && Character.isDigit(str.charAt(pos + 1));
                }

                private boolean oneOf(char... separators) {
                    char current = str.charAt(pos);
                    for (int i = 0; i < separators.length; i++) {
                        char separator = separators[i];
                        if (current == separator) {
                            return true;
                        }
                    }
                    return false;
                }

                boolean isQualifier() {
                    return pos < str.length() - 1 && oneOf('.', '-');
                }

                int scanDigit() {
                    int start = pos;
                    while (hasDigit()) {
                        pos++;
                    }
                    return Integer.parseInt(str.substring(start, pos));
                }

                public boolean isEnd() {
                    return pos == str.length();
                }

                private boolean skip(char ch) {
                    if (pos < str.length() && str.charAt(pos) == ch) {
                        pos++;
                        return true;
                    }
                    return false;
                }

                public void skipSeparator() {
                    pos++;
                }

                public String remainder() {
                    return pos == str.length() ? null : str.substring(pos);
                }
            }
        }

        private static class DefaultScheme extends AbstractScheme {
            private static final String VERSION_TEMPLATE = "%d.%d.%d%s";

            public DefaultScheme() {
                super(3);
            }

            @Override
            public String format(VersionNumber versionNumber) {
                return String.format(VERSION_TEMPLATE, versionNumber.major, versionNumber.minor, versionNumber.micro, versionNumber.qualifier == null ? "" : "-" + versionNumber.qualifier);
            }
        }

        private static class SchemeWithPatchVersion extends AbstractScheme {
            private static final String VERSION_TEMPLATE = "%d.%d.%d.%d%s";

            private SchemeWithPatchVersion() {
                super(4);
            }

            @Override
            public String format(VersionNumber versionNumber) {
                return String.format(VERSION_TEMPLATE, versionNumber.major, versionNumber.minor, versionNumber.micro, versionNumber.patch, versionNumber.qualifier == null ? "" : "-" + versionNumber.qualifier);
            }
        }

    }
}
