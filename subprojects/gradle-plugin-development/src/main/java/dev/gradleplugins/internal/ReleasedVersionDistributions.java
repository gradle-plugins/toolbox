package dev.gradleplugins.internal;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.Value;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.List;

public class ReleasedVersionDistributions {
    private final GradleVersionsService versions;

    public ReleasedVersionDistributions() {
        this(new HostedGradleVersionsService());
    }

    public ReleasedVersionDistributions(GradleVersionsService versions) {
        this.versions =  versions;
    }

    public GradleRelease getMostRecentSnapshot() {
        try (Reader reader = new InputStreamReader(versions.nightly())) {
            return new Gson().fromJson(reader, GradleRelease.class);
        } catch (IOException e) {
            throw new RuntimeException("Unable to get the last snapshot version", e);
        }
    }

    public GradleRelease getMostRecentRelease() {
        try (Reader reader = new InputStreamReader(versions.current())) {
            return new Gson().fromJson(reader, GradleRelease.class);
        } catch (IOException e) {
            throw new RuntimeException("Unable to get the last version", e);
        }
    }

    public List<GradleRelease> getAllVersions() {
        try (Reader reader = new InputStreamReader(versions.all())) {
            return new Gson().fromJson(reader, new TypeToken<List<GradleRelease>>() {}.getType());
        } catch (IOException e) {
            throw new RuntimeException("Unable to get the last version", e);
        }
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
        public InputStream nightly() throws IOException {
            return new URL("https://services.gradle.org/versions/nightly").openConnection().getInputStream();
        }

        @Override
        public InputStream current() throws IOException {
            return new URL("https://services.gradle.org/versions/current").openConnection().getInputStream();
        }

        @Override
        public InputStream all() throws IOException {
            return new URL("https://services.gradle.org/versions/all").openConnection().getInputStream();
        }
    }
}
