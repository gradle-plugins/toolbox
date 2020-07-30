package dev.gradleplugins.test.fixtures.gradle.daemon;

import dev.gradleplugins.test.fixtures.gradle.NativeServicesTestFixture;
import lombok.val;
import org.gradle.internal.os.OperatingSystem;
import org.gradle.launcher.daemon.context.DaemonContext;
import org.gradle.launcher.daemon.registry.DaemonDir;
import org.gradle.launcher.daemon.registry.DaemonInfo;
import org.gradle.launcher.daemon.registry.DaemonRegistry;
import org.gradle.internal.file.Stat;

import static org.gradle.launcher.daemon.server.api.DaemonStateControl.State;
import static org.gradle.launcher.daemon.server.api.DaemonStateControl.State.Stopped;

public class DaemonRegistryStateProbe implements DaemonStateProbe {
    private final DaemonRegistry registry;
    private final DaemonContext context;

    public DaemonRegistryStateProbe(DaemonRegistry registry, DaemonContext context) {
        this.context = context;
        this.registry = registry;
    }

    public void resetToken() {
        val daemonInfo = registry.getAll().stream().filter(it -> it.getContext().getPid().equals(context.getPid())).findFirst().orElse(null);
        registry.remove(daemonInfo.getAddress());
        registry.store(new DaemonInfo(daemonInfo.getAddress(), daemonInfo.getContext(), "password".getBytes(), daemonInfo.getState()));
    }

    public void assertRegistryNotWorldReadable() {
        val registryFile = new DaemonDir(context.getDaemonRegistryDir()).getRegistry();
        if (OperatingSystem.current().isLinux() || OperatingSystem.current().isMacOsX()) {
            val stat = NativeServicesTestFixture.getInstance().get(Stat.class);
            assert stat.getUnixMode(registryFile) == 0600; // user read-write
            assert stat.getUnixMode(registryFile.getParentFile()) == 0700; // user read-write-execute
        }
    }

    @Override
    public State getCurrentState() {
        val daemonInfo = registry.getAll().stream().filter(it -> it.getContext().getPid().equals(context.getPid())).findFirst();
        if (!daemonInfo.isPresent()) {
            return Stopped;
        }
        return daemonInfo.get().getState();
    }
}