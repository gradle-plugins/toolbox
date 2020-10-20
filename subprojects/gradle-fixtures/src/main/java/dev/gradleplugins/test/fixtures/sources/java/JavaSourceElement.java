package dev.gradleplugins.test.fixtures.sources.java;

import dev.gradleplugins.test.fixtures.sources.SourceElement;
import dev.gradleplugins.test.fixtures.sources.SourceFile;

import java.util.ArrayList;
import java.util.List;

@Deprecated
public abstract class JavaSourceElement extends SourceElement {
    public abstract SourceElement getSources();

    @Override
    public List<SourceFile> getFiles() {
        List<SourceFile> files = new ArrayList<>();
        files.addAll(getSources().getFiles());
        return files;
    }

    public static JavaPackage ofPackage(String name) {
        return new JavaPackage(name);
    }
}
