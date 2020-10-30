package dev.gradleplugins.fixtures.gradle.runner.parameters;

import org.apache.commons.io.output.WriterOutputStream;

import java.io.OutputStream;
import java.io.Writer;
import java.nio.charset.Charset;

public final class StandardOutput extends GradleExecutionParameterImpl<OutputStream> implements GradleExecutionParameter<OutputStream> {
    public static StandardOutput forwardToStandardOutput() {
        return fixed(StandardOutput.class, System.out);
    }

    public static StandardOutput forwardToStandardError() {
        return fixed(StandardOutput.class, System.err);
    }

    public static StandardOutput of(Writer writer) {
        return fixed(StandardOutput.class, new WriterOutputStream(writer, Charset.defaultCharset()));
    }
}
