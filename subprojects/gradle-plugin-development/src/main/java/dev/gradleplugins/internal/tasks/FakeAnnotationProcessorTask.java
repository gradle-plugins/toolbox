/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.gradleplugins.internal.tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.tasks.TaskAction;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.util.Optional;

public abstract class FakeAnnotationProcessorTask extends DefaultTask {
    public abstract ConfigurableFileCollection getSource();
    public abstract DirectoryProperty getPluginDescriptorDirectory();

    @TaskAction
    private void doGenerate() throws IOException {
        getSource().getFiles().stream().map(this::processFile).filter(Optional::isPresent).map(Optional::get).forEach(it -> {
            writePluginStub(it.pluginClass);
            writePluginDescriptor(it.pluginId, it.pluginClass);
        });
    }

    private Optional<PluginInfo> processFile(File file) {
        // The fake processor doesn't support its own class, so we are adding a shortcut here
        if (file.getAbsolutePath().endsWith("/dev/gradleplugins/internal/tasks/" + FakeAnnotationProcessorTask.class.getSimpleName() + ".java")) {
            return Optional.empty();
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String classPackage = null;
            String pluginId = null;
            for (String line = reader.readLine(); line != null && (classPackage == null || pluginId == null); line = reader.readLine()) {
                if (classPackage == null && line.contains("package")) {
                    classPackage = line.replaceAll("package", "").replace(";", "").trim();
                } else if (pluginId == null && line.contains("@GradlePlugin")) {
                    int startQuote = line.indexOf('"');
                    int endQuote = line.lastIndexOf('"');
                    pluginId = line.substring(startQuote + 1, endQuote);
                }
            }

            if (classPackage != null && pluginId != null) {
                PluginInfo info = new PluginInfo();
                info.pluginClass = classPackage + "." + removeExtension(file.getName());
                info.pluginId = pluginId;
                return Optional.of(info);
            }
            return Optional.empty();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static String removeExtension(String fileName) {
        // Poor man extension removal
        return fileName.replace(".groovy", "").replace(".java", "").replace(".kt", "");
    }

    private static class PluginInfo {
        String pluginId;
        String pluginClass;
    }

    private void writePluginStub(String pluginClass) {
        try {
            InputStream dummyPluginStream = this.getClass().getResourceAsStream("/dev/gradleplugins/internal/DummyPlugin.class");
            ClassReader classReader = new ClassReader(dummyPluginStream);
            ClassWriter classWriter = new ClassWriter(0);

            String nameSlashOwnerStubClass = pluginClass.replaceAll("\\.", "/") + "Stub";

            classReader.accept(new ClassVisitor(Opcodes.ASM6, classWriter) {
                @Override
                public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                    super.visit(version, access, nameSlashOwnerStubClass, signature, superName, interfaces);
                }

                @Override
                public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                    MethodVisitor mv = classWriter.visitMethod(access, name, desc, signature, exceptions);
                    return new MethodVisitor(Opcodes.ASM6, mv) {
                        @Override
                        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
                            if (opcode == Opcodes.INVOKEVIRTUAL && owner.equals("dev/gradleplugins/internal/DummyPlugin")) {
                                owner = nameSlashOwnerStubClass;
                            }
                            super.visitMethodInsn(opcode, owner, name, desc, itf);
                        }

                        @Override
                        public void visitLdcInsn(Object cst) {
                            if (cst instanceof String) {
                                String s = (String) cst;
                                switch (s) {
                                    case "<plugin-id>":
                                        cst = "com.example.hello";
                                        break;
                                    case "<minimum-supported-gradle-version>":
                                        cst = "5.6.2";
                                        break;
                                    case "<minimum-supported-java-version>":
                                        cst = "8";
                                        break;
                                    case "<plugin-class>":
                                        cst = "com.example.BasicPlugin";
                                        break;
                                }
                            }
                            super.visitLdcInsn(cst);
                        }
                    };
                }
            }, 0);

            File f = getPluginDescriptorDirectory().file(nameSlashOwnerStubClass + ".class").get().getAsFile();
            f.getParentFile().mkdirs();
            try (OutputStream classStream = new FileOutputStream(f)) {
                classStream.write(classWriter.toByteArray());
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void writePluginDescriptor(String pluginId, String pluginClass) {
        try {
            File pluginDescriptorFile = getPluginDescriptorDirectory().file("META-INF/gradle-plugins/" + pluginId + ".properties").get().getAsFile();
            pluginDescriptorFile.getParentFile().mkdirs();
            try (PrintWriter out = new PrintWriter(pluginDescriptorFile)) {
                out.println("implementation-class=" + pluginClass + "Stub");
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
