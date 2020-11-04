package dev.gradleplugins.runnerkit.utils;

import dev.gradleplugins.runnerkit.InvalidPluginMetadataException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public final class PluginUnderTestMetadataReading {
    public static final String IMPLEMENTATION_CLASSPATH_PROP_KEY = "implementation-classpath";
    public static final String PLUGIN_METADATA_FILE_NAME = "plugin-under-test-metadata.properties";

    private PluginUnderTestMetadataReading() {}

    public static List<File> readImplementationClasspath() {
        return readImplementationClasspath(Thread.currentThread().getContextClassLoader());
    }

    public static List<File> readImplementationClasspath(ClassLoader classLoader) {
        URL pluginClasspathUrl = classLoader.getResource(PLUGIN_METADATA_FILE_NAME);

        if (pluginClasspathUrl == null) {
            throw new InvalidPluginMetadataException(String.format("Test runtime classpath does not contain plugin metadata file '%s'", PLUGIN_METADATA_FILE_NAME));
        }

        return readImplementationClasspath(pluginClasspathUrl);
    }

    public static List<File> readImplementationClasspath(URL pluginClasspathUrl) {
        return readImplementationClasspath(pluginClasspathUrl.toString(), loadProperties(pluginClasspathUrl));
    }

    //region From org.gradle.util.GUtil
    private static Properties loadProperties(URL url) {
        try {
            URLConnection uc = url.openConnection();
            uc.setUseCaches(false);
            return loadProperties(uc.getInputStream());
        } catch (IOException var2) {
            throw new UncheckedIOException(var2);
        }
    }

    private static Properties loadProperties(InputStream inputStream) {
        Properties properties = new Properties();

        try {
            properties.load(inputStream);
            inputStream.close();
            return properties;
        } catch (IOException var3) {
            throw new UncheckedIOException(var3);
        }
    }
    //endregion

    public static List<File> readImplementationClasspath(String description, Properties properties) {
        if (!properties.containsKey(IMPLEMENTATION_CLASSPATH_PROP_KEY)) {
            throw new InvalidPluginMetadataException(String.format("Plugin metadata file '%s' does not contain expected property named '%s'", description, IMPLEMENTATION_CLASSPATH_PROP_KEY));
        }

        String value = properties.getProperty(IMPLEMENTATION_CLASSPATH_PROP_KEY);
        if (value != null) {
            value = value.trim();
        }

        if (value == null || value.isEmpty()) {
            throw new InvalidPluginMetadataException(String.format("Plugin metadata file '%s' has empty value for property named '%s'", description, IMPLEMENTATION_CLASSPATH_PROP_KEY));
        }

        String[] parsedImplementationClasspath = value.trim().split(File.pathSeparator);
        List<File> files = new ArrayList<File>(parsedImplementationClasspath.length);
        for (String path : parsedImplementationClasspath) {
            files.add(new File(path));
        }
        return files;
    }

}