package dev.gradleplugins.test.fixtures;

import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.gradle.internal.os.OperatingSystem;
import org.gradle.process.internal.streams.SafeStreams;

import java.io.*;
import java.util.Arrays;
import java.util.stream.Collectors;

public class ProcessFixture {
    public final Long pid;

    public ProcessFixture(Long pid) {
        this.pid = pid;
    }

    @SneakyThrows
    public boolean isAlive() {
        val output = new ByteArrayOutputStream();
        val builder = new ProcessBuilder();
        builder.command("ps", "-p", String.valueOf(pid));
        builder.redirectErrorStream(true);
        builder.directory(new File(".").getAbsoluteFile());
        val process = builder.start();
        process.getOutputStream().close();

        val pumperStdout = new Thread(() -> {
            try {
                IOUtils.copy(process.getInputStream(), output);
                process.getInputStream().close();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
        pumperStdout.start();
        int exitCode = process.waitFor();
        try {
            if (exitCode == 0) {
                return true;
            } else if (exitCode == 1) {
                return false;
            } else {
                throw new RuntimeException("Erro while checking process state");
            }
        } finally {
            pumperStdout.join();
        }
    }

    /**
     * Forcefully kills this daemon.
     */
    public void kill(boolean killTree) {
        System.out.println("Killing process with pid: " + pid);
        if (pid == null) {
            throw new RuntimeException("Unable to force kill the process because provided pid is null!");
        }
        if (!(OperatingSystem.current().isUnix() || OperatingSystem.current().isWindows())) {
            throw new RuntimeException("This implementation does not know how to forcefully kill a process on os: " + OperatingSystem.current());
        }
        execute(killArgs(pid, killTree), killScript(pid, killTree));
    }

    // Only supported on *nix platforms
    public String[] getChildProcesses() {
        if (pid == null) {
            throw new RuntimeException("Unable to get child processes because provided pid is null!");
        }
        if (!(OperatingSystem.current().isUnix())) {
            throw new RuntimeException("This implementation does not know how to get child processes on os: " + OperatingSystem.current());
        }
        return bash("ps -o pid,ppid -ax | awk '{ if ( $2 == " + pid + " ) { print $1 }}'").split("\n");
    }

    // Only supported on *nix platforms
    public String[] getProcessInfo(String[] pids) {
        if (pids == null || pids.length == 0) {
            throw new RuntimeException("Unable to get process info because provided pids are null or empty!");
        }
        if (!(OperatingSystem.current().isUnix())) {
            throw new RuntimeException("This implementation does not know how to get process info on os: " + OperatingSystem.current());
        }
        return bash("ps -o pid,ppid,args -p " + String.join(" -p ", pids)).split("\n");
    }

    private String bash(String commands) {
        return execute(new Object[] {"bash"}, new ByteArrayInputStream(commands.getBytes()));
    }

    @SneakyThrows
    private String execute(Object[] commandLine, InputStream input) {
        val output = new ByteArrayOutputStream();
        val builder = new ProcessBuilder();
        builder.command(Arrays.stream(commandLine).map(Object::toString).collect(Collectors.toList()));
        builder.redirectErrorStream(true);
        builder.directory(new File(".").getAbsoluteFile());
        val process = builder.start();

        val pumperStdin = new Thread(() -> {
            try {
                IOUtils.copy(input, process.getOutputStream());
                process.getOutputStream().flush();
                process.getOutputStream().close();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
        val pumperStdout = new Thread(() -> {
            try {
                IOUtils.copy(process.getInputStream(), output);
                process.getInputStream().close();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
        pumperStdin.start();
        pumperStdout.start();
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Exit non-zero");
        }
        pumperStdin.join();
        pumperStdout.join();

        return output.toString();
    }

    private static Object[] killArgs(Long pid, boolean killTree) {
        if (OperatingSystem.current().isUnix()) {
            // start shell, read script from stdin
            return new Object[] {"bash"};
        } else if (OperatingSystem.current().isWindows()) {
            if (killTree) {
                // '/T' kills full process tree
                // TODO: '/T' option should be removed after fixing GRADLE-3298
                return new Object[] {"taskkill.exe", "/F", "/T", "/PID", pid};
            } else {
                return new Object[] {"taskkill.exe", "/F", "/PID", pid};
            }
        } else {
            throw new IllegalStateException();
        }
    }

    private static InputStream killScript(Long pid, boolean killTree) {
        if (OperatingSystem.current().isUnix()) {
            // script for killing full process tree
            // TODO: killing full process tree should be removed after fixing GRADLE-3298
            // this script is tested on Linux and MacOSX
            String killScript = "killtree() {\n"
                    + "    local _pid=$1\n"
                    + "    for _child in $(ps -o pid,ppid -ax | awk \"{ if ( \\$2 == ${_pid} ) { print \\$1 }}\"); do\n"
                    + "        killtree ${_child}\n"
                    + "    done\n"
                    + "    kill -9 ${_pid}\n"
                    + "}\n";
            killScript += killTree ? "\nkilltree " + pid + "\n" : "\nkill -9 " + pid + "\n";
            return new ByteArrayInputStream(killScript.getBytes());
        } else if (OperatingSystem.current().isWindows()) {
            return SafeStreams.emptyInput();
        } else {
            throw new IllegalStateException();
        }
    }
}