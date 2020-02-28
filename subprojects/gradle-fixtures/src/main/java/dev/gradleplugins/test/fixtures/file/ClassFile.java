/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.gradleplugins.test.fixtures.file;

import com.google.common.io.ByteStreams;
import org.gradle.api.JavaVersion;
import org.gradle.internal.classanalysis.AsmConstants;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.io.*;

public class ClassFile {
    public boolean hasSourceFile;
    public boolean hasLineNumbers;
    public boolean hasLocalVars;
    public int classFileVersion;

    public ClassFile(File file) {
        this(newInputStream(file));
    }

    private static InputStream newInputStream(File file) {
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new UncheckedIOException(e);
        }
    }

    public ClassFile(InputStream inputStream) {
        MethodVisitor methodVisitor = new MethodVisitor(AsmConstants.ASM_LEVEL) {
            @Override
            public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
                hasLocalVars = true;
            }

            @Override
            public void visitLineNumber(int line, Label start) {
                hasLineNumbers = true;
            }
        };
        ClassVisitor visitor = new ClassVisitor(AsmConstants.ASM_LEVEL) {
            @Override
            public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                classFileVersion = version;
            }

            @Override
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                return methodVisitor;
            }

            @Override
            public void visitSource(String source, String debug) {
                hasSourceFile = true;
            }
        };
        try {
            byte[] classData = ByteStreams.toByteArray(inputStream);
            new ClassReader(classData).accept(visitor, 0);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public JavaVersion getJavaVersion() {
        return JavaVersion.forClassVersion(classFileVersion);
    }

    public boolean getDebugIncludesSourceFile() {
        return hasSourceFile;
    }

    public boolean getDebugIncludesLineNumbers() {
        return hasLineNumbers;
    }

    public boolean getDebugIncludesLocalVariables() {
        return hasLocalVars;
    }
}
