package dev.gradleplugins.fixtures.sources;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class NativeLibraryElement extends NativeSourceElement {
    public abstract SourceElement getPublicHeaders();

    public SourceElement getPrivateHeaders() {
        return empty();
    }

    @Override
    public SourceElement getHeaders() {
        return ofElements(getPublicHeaders(), getPrivateHeaders());
    }

    public static SourceElement ofPublicHeaders(SourceElement element) {
        return ofFiles(element.getFiles().stream().filter(it -> it.getKind().equals(SourceKind.HEADER) && it.getPath().equals("public")).collect(Collectors.toList()));
    }

    public static SourceElement ofPrivateHeaders(SourceElement element) {
        return ofFiles(element.getFiles().stream().filter(it -> it.getKind().equals(SourceKind.HEADER) && !it.getPath().equals("public")).collect(Collectors.toList()));
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
