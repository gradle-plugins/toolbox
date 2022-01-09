package dev.gradleplugins.internal;

import java.io.IOException;
import java.io.InputStream;

public interface GradleVersionsService {
    InputStream nightly() throws IOException;
    InputStream current() throws IOException;
    InputStream all() throws IOException;
}
