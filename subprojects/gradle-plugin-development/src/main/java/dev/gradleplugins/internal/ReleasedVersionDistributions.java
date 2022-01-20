package dev.gradleplugins.internal;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.Value;
import org.gradle.api.resources.TextResourceFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.List;

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

    @Value
    public static class GradleRelease {
        String version;
        boolean snapshot;
        boolean current;
        String rcFor;
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
