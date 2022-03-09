package dev.gradleplugins;

import org.gradle.api.file.FileSystemLocation;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

import java.io.File;
import java.nio.file.Path;

public final class FileMatchers {

    public static Matcher<Object> aFile(Matcher<? super File> matcher) {
        return new FeatureMatcher<Object, File>(matcher, "", "") {
            @Override
            protected File featureValueOf(Object actual) {
                if (actual instanceof FileSystemLocation) {
                    return ((FileSystemLocation) actual).getAsFile();
                } else if (actual instanceof Path) {
                    return ((Path) actual).toFile();
                } else if (actual instanceof File) {
                    return (File) actual;
                }
                throw new UnsupportedOperationException("Not a file.");
            }
        };
    }

    public static Matcher<File> withAbsolutePath(Matcher<? super String> matcher) {
        return new FeatureMatcher<File, String>(matcher, "", "") {
            @Override
            protected String featureValueOf(File actual) {
                return actual.getAbsolutePath();
            }
        };
    }
}
