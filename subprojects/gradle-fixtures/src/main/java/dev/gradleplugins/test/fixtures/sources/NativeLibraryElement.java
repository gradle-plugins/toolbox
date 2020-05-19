package dev.gradleplugins.test.fixtures.sources;

import java.util.ArrayList;
import java.util.List;

public abstract class NativeLibraryElement extends NativeSourceElement {
    public abstract SourceElement getPublicHeaders();

    public SourceElement getPrivateHeaders() {
        return empty();
    }

    @Override
    public SourceElement getHeaders() {
        return ofElements(getPublicHeaders(), getPrivateHeaders());
    }

    /**
     * Returns a copy of this library with the public headers the 'public' headers directory.
     */
    public NativeLibraryElement asLib() {
        final NativeLibraryElement delegate = this;
        return new NativeLibraryElement() {
            @Override
            public SourceElement getPublicHeaders() {
                List<SourceFile> headers = new ArrayList<SourceFile>();
                for (SourceFile sourceFile : delegate.getPublicHeaders().getFiles()) {
                    headers.add(sourceFile("public", sourceFile.getName(), sourceFile.getContent()));
                }
                return SourceElement.ofFiles(headers);
            }

            @Override
            public SourceElement getPrivateHeaders() {
                return delegate.getPrivateHeaders();
            }

            @Override
            public SourceElement getSources() {
                return delegate.getSources();
            }
        };
    }
}
