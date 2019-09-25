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

package dev.gradleplugins.internal;

import dev.gradleplugins.GradlePlugin;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GradlePluginProcessor extends AbstractProcessor {
    private final Map<String, String> plugins = new HashMap<>();

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> result = new HashSet<>();
        result.add(GradlePlugin.class.getName());
        return result;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    /**
     * <ol>
     *  <li> For each class annotated with {@link GradlePlugin}<ul>
     *      <li> Verify the {@link GradlePlugin} interface value is correct (TODO)
     *      </ul>
     *
     *  <li> For each {@link GradlePlugin} interface <ul>
     *       <li> Create a file named {@code META-INF/gradle-plugins/<id>.properties}
     *       <li> Add an entry in the file pointing to the implementation class
     *       </ul>
     * </ol>
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        try {
            return processImpl(annotations, roundEnv);
        } catch (Exception e) {
            // We don't allow exceptions of any kind to propagate to the compiler
            StringWriter writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            fatalError(writer.toString());
            return true;
        }
    }

    private boolean processImpl(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) {
            generateConfigFiles();
        } else {
            processAnnotations(annotations, roundEnv);
        }

        return true;
    }

    private void processAnnotations(Set<? extends TypeElement> annotations,
                                    RoundEnvironment roundEnv) {

        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(GradlePlugin.class);

        log(annotations.toString());
        log(elements.toString());

        for (Element annotatedElement : elements) {
            if (!annotatedElement.getKind().isClass()) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Annotated element not class");
            }


            GradlePlugin annotation = annotatedElement.getAnnotation(GradlePlugin.class);
            log(annotation.id());
            // Get the full QualifiedTypeName
            try {
                Class<?> clazz = annotation.annotationType();
                String qualifiedSuperClassName = clazz.getCanonicalName();
                String simpleTypeName = clazz.getSimpleName();
                log(qualifiedSuperClassName);
                log(simpleTypeName);
            } catch (MirroredTypeException mte) {
                DeclaredType classTypeMirror = (DeclaredType) mte.getTypeMirror();
                TypeElement classTypeElement = (TypeElement) classTypeMirror.asElement();
                String qualifiedSuperClassName = classTypeElement.getQualifiedName().toString();
                String simpleTypeName = classTypeElement.getSimpleName().toString();
                log(qualifiedSuperClassName);
                log(simpleTypeName);
            }

            TypeElement typeElement = (TypeElement)annotatedElement;
            log(typeElement.getQualifiedName().toString());
            plugins.put(annotation.id(), typeElement.getQualifiedName().toString());
        }
    }

    private void generateConfigFiles() {
        Filer filer = processingEnv.getFiler();

        for (Map.Entry<String, String> plugin : plugins.entrySet()) {
            String resourceFile = "META-INF/gradle-plugins/" + plugin.getKey() + ".properties";
            log("Working on resource file: " + resourceFile);
            try {
                FileObject fileObject = filer.createResource(StandardLocation.CLASS_OUTPUT, "",
                        resourceFile);
                try (PrintWriter out = new PrintWriter(fileObject.openOutputStream())) {
                    out.println("implementation-class=" + plugin.getValue());
                }

                log("Wrote to: " + fileObject.toUri());
            } catch (IOException e) {
                fatalError("Unable to create " + resourceFile + ", " + e);
                return;
            }
        }
    }

    private void log(String msg) {
        if (processingEnv.getOptions().containsKey("debug")) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, msg);
        }
    }

    private void error(String msg, Element element, AnnotationMirror annotation) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, msg, element, annotation);
    }

    private void fatalError(String msg) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "FATAL ERROR: " + msg);
    }
}
