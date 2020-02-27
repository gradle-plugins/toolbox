package dev.gradleplugins.test.fixtures.sources.java;

import java.io.File;

public class JavaPackage {
    private final String name;

    public JavaPackage(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getDirectoryLayout() {
        return name.replace('.', File.separatorChar);
    }

    public String jniHeader(String className) {
        return name.replace('.', '_') + "_" + className + ".h";
    }

    public String jniMethodName(String className, String methodName) {
        return "Java_" + name.replace('.', '_') + "_" + className + "_" + methodName;
    }
}
