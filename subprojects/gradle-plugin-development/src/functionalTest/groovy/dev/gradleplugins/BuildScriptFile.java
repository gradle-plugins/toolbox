package dev.gradleplugins;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public final class BuildScriptFile {
    private Path location;

    public BuildScriptFile(Path location) {
        this.location = location;
    }

    public BuildScriptFile useKotlinDsl() throws IOException {
        if (location.getFileName().toString().endsWith(".gradle")) {
            final Path src = location;
            final Path dst = location.getParent().resolve(location.getFileName() + ".kts");
            Files.move(src, dst);
            location = dst;
            Files.write(location, new String(Files.readAllBytes(location)).replace("def ", "val ").getBytes(StandardCharsets.UTF_8));
        }
        return this;
    }

    public BuildScriptFile useGroovyDsl() throws IOException {
        if (location.getFileName().toString().endsWith(".gradle.kts")) {
            final Path src = location;
            final Path dst = location.getParent().resolve(location.getFileName().toString().substring(0, location.getFileName().toString().length() - ".kts".length()));
            Files.move(src, dst);
            location = dst;
        }
        return this;
    }

    public Path getLocation() {
        return location;
    }

    public BuildScriptFile append(String content) throws IOException {
        if (location.getFileName().toString().endsWith(".kts")) {
            Files.write(location, content.replace("def ", "val ").getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND, StandardOpenOption.CREATE);
        } else {
            Files.write(location, content.getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND, StandardOpenOption.CREATE);
        }
        return this;
    }

    public BuildScriptFile append(String... lines) throws IOException {
        return append(String.join("\n", lines) + '\n');
    }
}
