package dev.gradleplugins.runnerkit.providers;

import org.apache.commons.io.output.WriterOutputStream;

import java.io.OutputStream;
import java.io.Writer;
import java.nio.charset.Charset;

public final class StandardStreamProvider extends AbstractGradleExecutionProvider<OutputStream> {
    public static StandardStreamProvider forwardToStandardOutput() {
        return fixed(StandardStreamProvider.class, System.out);
    }

    public static StandardStreamProvider forwardToStandardError() {
        return fixed(StandardStreamProvider.class, System.err);
    }

    public static StandardStreamProvider of(Writer writer) {
        return fixed(StandardStreamProvider.class, new WriterOutputStream(writer, Charset.defaultCharset()));
    }
}
