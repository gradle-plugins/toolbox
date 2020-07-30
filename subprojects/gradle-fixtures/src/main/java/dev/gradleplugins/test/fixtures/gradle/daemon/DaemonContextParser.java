package dev.gradleplugins.test.fixtures.gradle.daemon;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import org.gradle.launcher.daemon.configuration.DaemonParameters;
import org.gradle.launcher.daemon.context.DaemonContext;
import org.gradle.launcher.daemon.context.DefaultDaemonContext;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DaemonContextParser {
    public static DaemonContext parseFromFile(File file) {
        try (FileReader in = new FileReader(file);
             BufferedReader reader = new BufferedReader(in)) {
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                DaemonContext context = parseFrom(line);
                if (context != null) {
                    return context;
                }
            }
        } catch(IOException e) {
            throw new IllegalStateException("unable to parse DefaultDaemonContext from source: [" + file.getAbsolutePath() + "].", e);
        }
        throw new IllegalStateException("unable to parse DefaultDaemonContext from source: [" + file.getAbsolutePath() + "].");
    }

    public static DaemonContext parseFromString(String source) {
        DaemonContext context = parseFrom(source);
        if (context == null) {
            throw new IllegalStateException("unable to parse DefaultDaemonContext from source: [" + source + "].");
        }
        return context;
    }

    private static DaemonContext parseFrom(String source) {
        Pattern pattern = Pattern.compile("^.*DefaultDaemonContext\\[(uid=[^\\n,]+)?,?javaHome=([^\\n]+),daemonRegistryDir=([^\\n]+),pid=([^\\n]+),idleTimeout=(.+?)(,priority=[^\\n]+)?,daemonOpts=([^\\n]+)].*",
                Pattern.MULTILINE + Pattern.DOTALL);
        Matcher matcher = pattern.matcher(source);

        if (matcher.matches()) {
            String uid = matcher.group(1) == null ? null : matcher.group(1).substring("uid=".length());
            String javaHome = matcher.group(2);
            String daemonRegistryDir = matcher.group(3);
            String pidStr = matcher.group(4);
            Long pid = pidStr.equals("null") ? null : Long.parseLong(pidStr);
            Integer idleTimeout = Integer.decode(matcher.group(5));
            DaemonParameters.Priority priority = matcher.group(6) == null ? DaemonParameters.Priority.NORMAL : DaemonParameters.Priority.valueOf(matcher.group(6).substring(",priority=".length()));
            List<String> jvmOpts = Lists.newArrayList(Splitter.on(',').split(matcher.group(7)));
            return new DefaultDaemonContext(uid, new File(javaHome), new File(daemonRegistryDir), pid, idleTimeout, jvmOpts, priority);
        } else {
            return null;
        }
    }
}