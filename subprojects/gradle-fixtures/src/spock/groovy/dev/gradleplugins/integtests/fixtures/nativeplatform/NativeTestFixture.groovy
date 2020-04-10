package dev.gradleplugins.integtests.fixtures.nativeplatform

import dev.gradleplugins.integtests.fixtures.nativeplatform.AvailableToolChains.InstalledToolChain
import dev.gradleplugins.test.fixtures.file.TestFile
import org.apache.commons.lang3.ArchUtils
import org.apache.commons.lang3.SystemUtils

trait NativeTestFixture {
    abstract InstalledToolChain getToolChain()
    abstract TestFile file(String path)

    SharedLibraryFixture sharedLibrary(String path) {
        return toolChain.sharedLibrary(file(path))
    }

    StaticLibraryFixture staticLibrary(String path) {
        return toolChain.staticLibrary(file(path))
    }

    NativeBinaryFixture resourceOnlyLibrary(String path) {
        return toolChain.resourceOnlyLibrary(file(path))
    }

    NativeBinaryFixture machOBundle(String path) {
        return new NativeBinaryFixture(file(path), toolChain)
    }

    String getCurrentOsFamilyName() {
        if (SystemUtils.IS_OS_MAC_OSX) {
            return 'macos'
        } else if (SystemUtils.IS_OS_WINDOWS) {
            return 'windows'
        } else if (SystemUtils.IS_OS_LINUX) {
            return 'linux'
        } else if (SystemUtils.IS_OS_FREE_BSD) {
            return 'freebsd'
        }
        throw new UnsupportedOperationException("Unsupported operating system family of name '${SystemUtils.OS_NAME}'")
    }

    String getCurrentArchitecture() {
        if (ArchUtils.processor.x86) {
            if (ArchUtils.processor.'32Bit') {
                return 'x86'
            } else if (ArchUtils.processor.'64Bit') {
                return 'x86_64'
            }
        }
        throw new UnsupportedOperationException("Unsupported architecture of name '${SystemUtils.OS_ARCH}'")
    }
}