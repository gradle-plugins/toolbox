package dev.gradleplugins.test.fixtures.gradle.daemon;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import dev.gradleplugins.test.fixtures.gradle.NativeServicesTestFixture;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.gradle.api.JavaVersion;
import org.gradle.internal.logging.services.LoggingServiceRegistry;
import org.gradle.internal.service.ServiceRegistryBuilder;
import org.gradle.internal.service.scopes.BasicGlobalScopeServices;
import org.gradle.launcher.daemon.client.DaemonClientGlobalServices;
import org.gradle.launcher.daemon.registry.DaemonRegistry;
import org.gradle.launcher.daemon.registry.DaemonRegistryServices;
import org.gradle.util.GradleVersion;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertTrue;

public class DaemonLogsAnalyzer implements DaemonsFixture {
    private final File daemonLogsDir;
    private final File daemonBaseDir;
    private final DaemonRegistry registry;
    private final String version;

    public DaemonLogsAnalyzer(File daemonBaseDirectory) {
        this(daemonBaseDirectory, GradleVersion.current().getVersion());
    }

    public DaemonLogsAnalyzer(File daemonBaseDir, String version) {
        this.version = version;
        this.daemonBaseDir = daemonBaseDir;
        daemonLogsDir = new File(daemonBaseDir, version);
        val services = ServiceRegistryBuilder.builder()
            .parent(LoggingServiceRegistry.newEmbeddableLogging())
            .parent(NativeServicesTestFixture.getInstance())
            .provider(new BasicGlobalScopeServices())
            .provider(new DaemonClientGlobalServices())
            .provider(new DaemonRegistryServices(daemonBaseDir))
            .build();
        registry = services.get(DaemonRegistry.class);
    }

    public static DaemonsFixture newAnalyzer(File daemonBaseDirectory) {
        return newAnalyzer(daemonBaseDirectory, GradleVersion.current().getVersion());
    }

    public static DaemonsFixture newAnalyzer(File daemonBaseDir, String version) {
        return new DaemonLogsAnalyzer(daemonBaseDir, version);
    }

    public DaemonRegistry getRegistry() {
        return registry;
    }

    public void killAll() {
        getAllDaemons().forEach(DaemonFixture::kill);
    }


    public List<DaemonFixture> getDaemons() {
        return getAllDaemons().stream().filter(it -> daemonStoppedWithSocketExceptionOnWindows(it) || it.logContains("Starting build in new daemon")).collect(Collectors.toList());
    }

    public List<DaemonFixture> getAllDaemons() {
        if (!daemonLogsDir.exists() || !daemonLogsDir.isDirectory()) {
            return ImmutableList.of();
        }
        return Arrays.stream(daemonLogsDir.listFiles()).filter(it -> it.getName().endsWith(".log") && !it.getName().startsWith("hs_err")).map(this::daemonForLogFile).collect(Collectors.toList());
    }

    public List<DaemonFixture> getVisible() {
        return registry.getAll().stream().map(it -> daemonForLogFile(new File(daemonLogsDir, String.format("daemon-%d.out.log", it.getPid())))).collect(Collectors.toList());
    }

    public DaemonFixture daemonForLogFile(File logFile) {
        if (version.equals(GradleVersion.current().getVersion())) {
            return new TestableDaemon(logFile, registry);
        }
        return new LegacyDaemon(logFile, version);
    }

    public DaemonFixture getDaemon() {
        val daemons = getDaemons();
        assert daemons.size() == 1;
        return daemons.get(0);
    }

    public File getDaemonBaseDir() {
        return daemonBaseDir;
    }

    public String getVersion() {
        return version;
    }

    public void assertNoCrashedDaemon() {
        List<File> crashLogs = Arrays.stream(daemonLogsDir.listFiles()).filter(it -> it.getName().endsWith(".log") && it.getName().startsWith("hs_err")).collect(Collectors.toList());
        crashLogs.forEach(it -> {
            try {
                System.out.println(FileUtils.readFileToString(it, Charset.defaultCharset()));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
        assertTrue("Found crash los: " + crashLogs, crashLogs.isEmpty());
    }

    static boolean daemonStoppedWithSocketExceptionOnWindows(DaemonFixture daemon) {
        return runsOnWindowsAndJava7or8() && (daemon.logContains("java.net.SocketException: Socket operation on nonsocket:")
                || daemon.logContains("java.io.IOException: An operation was attempted on something that is not a socket")
                || daemon.logContains("java.io.IOException: An existing connection was forcibly closed by the remote host"));
    }

    static boolean runsOnWindowsAndJava7or8() {
        return SystemUtils.IS_OS_WINDOWS && ImmutableSet.of(JavaVersion.VERSION_1_7, JavaVersion.VERSION_1_8).contains(JavaVersion.current());
    }
}