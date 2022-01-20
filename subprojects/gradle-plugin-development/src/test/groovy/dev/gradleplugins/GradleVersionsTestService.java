package dev.gradleplugins;

import com.google.gson.Gson;
import dev.gradleplugins.internal.GradleVersionsService;
import dev.gradleplugins.internal.ReleasedVersionDistributions;
import org.gradle.util.GradleVersion;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class GradleVersionsTestService implements GradleVersionsService {
    private final List<ReleasedVersionDistributions.GradleRelease> releases;

    private GradleVersionsTestService(List<ReleasedVersionDistributions.GradleRelease> releases) {
        assert releases.stream().filter(ReleasedVersionDistributions.GradleRelease::isCurrent).count() <= 1;
        this.releases = releases;
    }

    @Override
    public Reader nightly() throws IOException {
        ReleasedVersionDistributions.GradleRelease result = releases.stream().filter(ReleasedVersionDistributions.GradleRelease::isSnapshot).max(Comparator.comparing(it -> GradleVersion.version(it.getVersion()))).orElseThrow(RuntimeException::new);
        return new InputStreamReader(new ByteArrayInputStream(new Gson().toJson(result).getBytes(StandardCharsets.UTF_8)));
    }

    @Override
    public Reader current() throws IOException {
        ReleasedVersionDistributions.GradleRelease result = releases.stream().filter(ReleasedVersionDistributions.GradleRelease::isCurrent).findFirst().orElseThrow(RuntimeException::new);
        return new InputStreamReader(new ByteArrayInputStream(new Gson().toJson(result).getBytes(StandardCharsets.UTF_8)));
    }

    @Override
    public Reader all() throws IOException {
        return new InputStreamReader(new ByteArrayInputStream(new Gson().toJson(releases).getBytes(StandardCharsets.UTF_8)));
    }

    public static GradleVersionsTestService empty() {
        return new GradleVersionsTestService(Collections.emptyList());
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final List<ReleasedVersionDistributions.GradleRelease> releases = new ArrayList<>();

        public Builder version(ReleasedVersionDistributions.GradleRelease release) {
            releases.add(release);
            return this;
        }

        public GradleVersionsTestService build() {
            return new GradleVersionsTestService(releases);
        }
    }
}
