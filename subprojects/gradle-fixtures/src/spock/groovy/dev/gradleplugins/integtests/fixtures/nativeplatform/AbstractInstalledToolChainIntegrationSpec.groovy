/*
 * Copyright 2012 the original author or authors.
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

package dev.gradleplugins.integtests.fixtures.nativeplatform

import dev.gradleplugins.integtests.fixtures.AbstractGradleSpecification
import dev.gradleplugins.integtests.fixtures.nativeplatform.NativeToolChainTestRunner
import dev.gradleplugins.test.fixtures.file.TestFile
import dev.gradleplugins.fixtures.sources.SourceElement
import dev.gradleplugins.fixtures.sources.SourceFile
import org.apache.commons.io.FilenameUtils
import org.gradle.internal.hash.HashUtil
import org.gradle.internal.os.OperatingSystem
import org.gradle.internal.time.Time
import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform
import org.junit.runner.RunWith

/**
 * Runs a test separately for each installed tool chain.
 */
@RunWith(NativeToolChainTestRunner.class)
//@Category(ContextualMultiVersionTest.class)
abstract class AbstractInstalledToolChainIntegrationSpec extends AbstractGradleSpecification implements HostPlatform {
    static AvailableToolChains.InstalledToolChain toolChain
    File initScript

    AvailableToolChains.InstalledToolChain getToolchainUnderTest() { toolChain }

    def setup() {
        initScript = file("init.gradle") << """
            allprojects { p ->
                plugins.withType(NativeComponentModelPlugin) {
                    model {
                        toolChains {
                            ${toolChain.buildScriptConfig}
                        }
                    }
                }
            }
        """
        executer = toolChain.configureExecuter(executer.usingInitScript(initScript))
    }

    String executableName(Object path) {
        return path + OperatingSystem.current().getExecutableSuffix()
    }

    String getExecutableExtension() {
        def suffix = OperatingSystem.current().executableSuffix
        return suffix.empty ? "" : suffix.substring(1)
    }

    ExecutableFixture executable(Object path) {
        return toolChain.executable(file(path))
    }

    LinkerOptionsFixture linkerOptionsFor(String taskName, TestFile projectDir = testDirectory) {
        return toolChain.linkerOptionsFor(projectDir.file("build/tmp/$taskName/options.txt"))
    }

    TestFile objectFile(Object path) {
        return toolChain.objectFile(file(path))
    }

    String withLinkLibrarySuffix(Object path) {
        return path + (toolChain.visualCpp ? OperatingSystem.current().linkLibrarySuffix : OperatingSystem.current().sharedLibrarySuffix)
    }

    String linkLibraryName(Object path) {
        return toolChain.visualCpp ? OperatingSystem.current().getLinkLibraryName(path.toString()) : OperatingSystem.current().getSharedLibraryName(path.toString())
    }

    String getLinkLibrarySuffix() {
        return toolChain.visualCpp ? OperatingSystem.current().linkLibrarySuffix.substring(1) : OperatingSystem.current().sharedLibrarySuffix.substring(1)
    }

    String staticLibraryName(Object path) {
        return OperatingSystem.current().getStaticLibraryName(path.toString())
    }

    String withStaticLibrarySuffix(Object path) {
        return path + OperatingSystem.current().staticLibrarySuffix
    }

    String getStaticLibraryExtension() {
        return OperatingSystem.current().staticLibrarySuffix.substring(1)
    }

    String withSharedLibrarySuffix(Object path) {
        return path + OperatingSystem.current().sharedLibrarySuffix
    }

    String sharedLibraryName(Object path) {
        return OperatingSystem.current().getSharedLibraryName(path.toString())
    }

    String getSharedLibraryExtension() {
        return OperatingSystem.current().sharedLibrarySuffix.substring(1)
    }

    SharedLibraryFixture sharedLibrary(Object path) {
        return toolChain.sharedLibrary(file(path))
    }

    StaticLibraryFixture staticLibrary(Object path) {
        return toolChain.staticLibrary(file(path))
    }

    NativeBinaryFixture resourceOnlyLibrary(Object path) {
        return toolChain.resourceOnlyLibrary(file(path))
    }

    NativeBinaryFixture machOBundle(Object path) {
        return new NativeBinaryFixture(file(path), toolChain)
    }

    def objectFileFor(File sourceFile, String rootObjectFilesDir = "build/objs/main/main${sourceType}") {
        return intermediateFileFor(sourceFile, rootObjectFilesDir, OperatingSystem.current().isWindows() ? ".obj" : ".o")
    }

    TestFile intermediateFileFor(File sourceFile, String intermediateFilesDir, String intermediateFileSuffix) {
        String baseName = FilenameUtils.removeExtension(sourceFile.getName())
        String relativePath = FilenameUtils.separatorsToSystem(getTestDirectory().toURI().relativize(sourceFile.toURI()).toString())
        String uniqueName = HashUtil.createCompactMD5(relativePath)
        return file(intermediateFilesDir, uniqueName, "${baseName}${intermediateFileSuffix}")
    }

    List<NativeBinaryFixture> objectFiles(SourceElement sourceElement, String rootObjectFilesDir = "build/obj/${sourceElement.sourceSetName}/debug") {
        List<NativeBinaryFixture> result = new ArrayList<NativeBinaryFixture>()

        String sourceSetName = sourceElement.getSourceSetName()
        for (SourceFile sourceFile : sourceElement.getFiles()) {
            def relativeSourceFile = file("src", sourceSetName, sourceFile.path, sourceFile.name)
            result.add(new NativeBinaryFixture(objectFileFor(relativeSourceFile, rootObjectFilesDir), toolChain))
        }

        return result
    }

    boolean isNonDeterministicCompilation() {
        // Visual C++ compiler embeds a timestamp in every object file, and ASLR is non-deterministic
        toolChain.visualCpp || objectiveCWithAslr
    }

    // compiling Objective-C and Objective-Cpp with clang generates
    // random different object files (related to ASLR settings)
    // We saw this behaviour only on linux so far.
    boolean isObjectiveCWithAslr() {
        return (sourceType == "Objc" || sourceType == "Objcpp") &&
            OperatingSystem.current().isLinux() &&
            toolChain.displayName.startsWith("clang")
    }

    protected void maybeWait() {
        if (toolChain.visualCpp) {
            def now = Time.clock().currentTime
            def nextSecond = now % 1000
            Thread.sleep(1200 - nextSecond)
        }
    }

    protected String getCurrentOsFamilyName() {
        DefaultNativePlatform.currentOperatingSystem.toFamilyName()
    }

    protected String getCurrentArchitecture() {
        return DefaultNativePlatform.currentArchitecture.name
    }
}
