package dev.gradleplugins.internal;

import java.io.IOException;
import java.io.Reader;

public interface GradleVersionsService {
    Reader nightly() throws IOException;
    Reader current() throws IOException;
    Reader all() throws IOException;
}
