package dev.gradleplugins.fixtures.sources;

import com.google.common.collect.ImmutableSet;
import lombok.EqualsAndHashCode;
import lombok.val;
import org.apache.commons.io.FilenameUtils;

import java.util.Set;

@EqualsAndHashCode
public final class SourceKind {
    public static final SourceKind C = new SourceKind("c", "c");
    public static final SourceKind CPP = new SourceKind("cpp", "cpp", "cc", "cxx");
    public static final SourceKind OBJECTIVE_C = new SourceKind("objective-c", "m");
    public static final SourceKind OBJECTIVE_CPP = new SourceKind("objective-cpp", "mm");
    public static final SourceKind SWIFT = new SourceKind("swift", "swift");
    public static final SourceKind JAVA = new SourceKind("java", "java");
    public static final SourceKind GROOVY = new SourceKind("groovy", "groovy");
    public static final SourceKind KOTLIN = new SourceKind("kotlin", "kt");
    public static final SourceKind HEADER = new SourceKind("header", "h", "hpp");
    public static final SourceKind UNKNOWN = new SourceKind("unknown");
    private static final Set<SourceKind> DEFAULT_KINDS = ImmutableSet.of(C, CPP, OBJECTIVE_C, OBJECTIVE_CPP, SWIFT, JAVA, GROOVY, KOTLIN, HEADER);

    private final String identifier;
    @EqualsAndHashCode.Exclude private final Set<String> fileExtensions;

    private SourceKind(String identifier, String... fileExtensions) {
        this.identifier = identifier;
        this.fileExtensions = ImmutableSet.copyOf(fileExtensions);
    }

    public static SourceKind of(String identifier) {
        return new SourceKind(identifier);
    }

    public static SourceKind valueOf(String fileName) {
        val extension = FilenameUtils.getExtension(fileName);
        return DEFAULT_KINDS.stream().filter(it -> it.fileExtensions.contains(extension)).findFirst().orElse(UNKNOWN);
    }
}
