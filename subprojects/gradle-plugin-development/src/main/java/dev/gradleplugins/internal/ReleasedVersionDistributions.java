package dev.gradleplugins.internal;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.gradle.api.resources.TextResourceFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.List;
import java.util.Objects;

public class ReleasedVersionDistributions {
    public static final ReleasedVersionDistributions GRADLE_DISTRIBUTIONS = new ReleasedVersionDistributions();
    private GradleRelease mostRecentSnapshot;
    private GradleRelease mostRecentRelease;
    private List<GradleRelease> allVersions;
    private final GradleVersionsService versions;

    public ReleasedVersionDistributions() {
        this(new HostedGradleVersionsService());
    }

    public ReleasedVersionDistributions(TextResourceFactory textResourceFactory) {
        this(new TextResourceHostedGradleVersionsService(textResourceFactory));
    }

    public ReleasedVersionDistributions(GradleVersionsService versions) {
        this.versions =  versions;
    }

    public GradleRelease getMostRecentSnapshot() {
        if (mostRecentSnapshot == null) {
            try (Reader reader = versions.nightly()) {
                mostRecentSnapshot = new Gson().fromJson(reader, GradleRelease.class);
            } catch (IOException e) {
                throw new RuntimeException("Unable to get the last snapshot version", e);
            }
        }
        return mostRecentSnapshot;
    }

    public GradleRelease getMostRecentRelease() {
        if (mostRecentRelease == null) {
            try (Reader reader = versions.current()) {
                mostRecentRelease = new Gson().fromJson(reader, GradleRelease.class);
            } catch (IOException e) {
                throw new RuntimeException("Unable to get the last version", e);
            }
        }
        return mostRecentRelease;
    }

    public List<GradleRelease> getAllVersions() {
        if (allVersions == null) {
            try (Reader reader = versions.all()) {
                allVersions = new Gson().fromJson(reader, new TypeToken<List<GradleRelease>>() {}.getType());
            } catch (IOException e) {
                throw new RuntimeException("Unable to get the last version", e);
            }
        }
        return allVersions;
    }

    public static final class GradleRelease {
        private final String version;
        private final boolean snapshot;
        private final boolean current;
        private final String rcFor;

        public GradleRelease(String version, boolean snapshot, boolean current, String rcFor) {
            this.version = version;
            this.snapshot = snapshot;
            this.current = current;
            this.rcFor = rcFor;
        }

        public String getVersion() {
            return version;
        }

        public boolean isSnapshot() {
            return snapshot;
        }

        public boolean isCurrent() {
            return current;
        }

        public String getRcFor() {
            return rcFor;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            GradleRelease that = (GradleRelease) o;
            return snapshot == that.snapshot && current == that.current && Objects.equals(version, that.version) && Objects.equals(rcFor, that.rcFor);
        }

        @Override
        public int hashCode() {
            return Objects.hash(version, snapshot, current, rcFor);
        }
    }

    private static final class HostedGradleVersionsService implements GradleVersionsService {
        @Override
        public Reader nightly() throws IOException {
            return new InputStreamReader(new URL("https://services.gradle.org/versions/nightly").openConnection().getInputStream());
        }

        @Override
        public Reader current() throws IOException {
            return new InputStreamReader(new URL("https://services.gradle.org/versions/current").openConnection().getInputStream());
        }

        @Override
        public Reader all() throws IOException {
            return new InputStreamReader(new URL("https://services.gradle.org/versions/all").openConnection().getInputStream());
        }
    }

    private static final class TextResourceHostedGradleVersionsService implements GradleVersionsService {
        private final TextResourceFactory textResourceFactory;

        public TextResourceHostedGradleVersionsService(TextResourceFactory textResourceFactory) {
            this.textResourceFactory = textResourceFactory;
        }

        @Override
        public Reader nightly() throws IOException {
            return textResourceFactory.fromUri("https://services.gradle.org/versions/nightly").asReader();
        }

        @Override
        public Reader current() throws IOException {
            return textResourceFactory.fromUri("https://services.gradle.org/versions/current").asReader();
        }

        @Override
        public Reader all() throws IOException {
            return textResourceFactory.fromUri("https://services.gradle.org/versions/all").asReader();
        }
    }
}
