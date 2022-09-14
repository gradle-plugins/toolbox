package dev.gradleplugins.runnerkit;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.val;
import org.gradle.util.TextUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ToString
@EqualsAndHashCode
public final class BuildFailures {
    private static final Pattern FAILURE_PATTERN = Pattern.compile("FAILURE: (.+)");
    private static final Pattern CAUSE_PATTERN = Pattern.compile("(?m)(^\\s*> )");
    private static final Pattern DESCRIPTION_PATTERN = Pattern.compile("(?ms)^\\* What went wrong:$(.+?)^\\* Try:$");
    private static final Pattern LOCATION_PATTERN = Pattern.compile("(?ms)^\\* Where:((.+?)'.+?') line: (\\d+)$");
    private static final Pattern RESOLUTION_PATTERN = Pattern.compile("(?ms)^\\* Try:$(.+?)^\\* Exception is:$");
    private final String summary;
    private final List<BuildResult.Failure> problems;
//    private final List<Problem> problemsNotChecked = new ArrayList<>();
//    private final List<String> lineNumbers;
//    private final List<String> fileNames;
//    private final String resolution;

    public BuildFailures(String summary, List<String> lineNumbers, List<String> fileNames, List<BuildResult.Failure> problems, String resolution) {
        this.summary = summary;
//        this.lineNumbers = lineNumbers;
//        this.fileNames = fileNames;
        this.problems = problems;
//        this.resolution = resolution;
    }

//    static boolean hasFailure(String error) {
//        return FAILURE_PATTERN.matcher(error).find();
//    }

    public List<BuildResult.Failure> get() {
        return problems;
    }

    public String getSummary() {
        return summary;
    }

    private boolean hasFailure() {
        return !problems.isEmpty();
    }

    private boolean hasMultipleFailures() {
        return problems.size() > 1;
    }

    private void writeHeaderIfMultipleFailure(StringBuilder builder, int count) {
        if (hasMultipleFailures()) {
            builder.append(count).append(": Task failed with an exception.").append("\n");
            builder.append("-----------").append("\n");
        }
    }

    private void writeFooterIfMultipleFailure(StringBuilder builder, int count) {
        if (hasMultipleFailures()) {
            builder.append("\n").append("==============================================================================");
            if (count < problems.size()) {
                builder.append("\n").append("\n");
            }
        }
    }

    void toString(StringBuilder result) {
        if (hasFailure()) {
            result.append("FAILURE: ").append(summary).append("\n");
            result.append("\n");

            for (int i = 0; i < problems.size(); ++i) {
                val failure = problems.get(i);

                writeHeaderIfMultipleFailure(result, i + 1);

                result.append("* What went wrong:").append("\n");
                result.append(failure.getDescription());
                String indent = "\n> ";
                for (val cause : failure.getCauses()) {
                    result.append(indent).append(cause);
                    indent = "  " + indent;
                }

                writeFooterIfMultipleFailure(result, i + 1);
            }
        }
    }

    public static BuildFailures from(CommandLineToolLogContent output) {
        // Find failure section
        val failureContent= output.visitEachLine(new Consumer<CommandLineToolLogContent.LineDetails>() {
            private boolean found = false;
            @Override
            public void accept(CommandLineToolLogContent.LineDetails it) {
                val failureMatching = FAILURE_PATTERN.matcher(it.getLine()).matches();
                if (found && failureMatching) {
                    throw new IllegalArgumentException("Found multiple failure sections in log output: " + output.getAsString());
                }

                if (!found && failureMatching) {
                    found = true;
                } else if (!found) {
                    it.dropLine();
                }
            }
        });

        val builder = builder();
        String failureText = failureContent.getAsString();
        Matcher matcher = FAILURE_PATTERN.matcher(failureText);
        if (matcher.lookingAt()) {
            builder.withSummary(matcher.group(1));
        }

        matcher = LOCATION_PATTERN.matcher(failureText);
        while (matcher.find()) {
            builder.addFileName(matcher.group(1).trim());
            builder.addLineNumber(matcher.group(3));
        }

        matcher = DESCRIPTION_PATTERN.matcher(failureText);
        while (matcher.find()) {
            String problemStr = matcher.group(1);
            BuildFailure problem = extract(problemStr);
            builder.withFailure(problem);
        }

        matcher = RESOLUTION_PATTERN.matcher(failureText);
        if (matcher.find()) {
            builder.withResolution(matcher.group(1).trim());
        }

        return builder.build();
    }

    private static BuildFailure extract(String problem) {
        Matcher matcher = CAUSE_PATTERN.matcher(problem);
        String description;
        List<String> causes = new ArrayList<>();
        if (!matcher.find()) {
            description = TextUtil.normaliseLineSeparators(problem.trim());
        } else {
            description = TextUtil.normaliseLineSeparators(problem.substring(0, matcher.start()).trim());
            while (true) {
                int pos = matcher.end();
                int prefix = matcher.group(1).length();
                String prefixPattern = toPrefixPattern(prefix);
                if (matcher.find(pos)) {
                    String cause = TextUtil.normaliseLineSeparators(problem.substring(pos, matcher.start()).trim().replaceAll(prefixPattern, ""));
                    causes.add(cause);
                } else {
                    String cause = TextUtil.normaliseLineSeparators(problem.substring(pos).trim().replaceAll(prefixPattern, ""));
                    causes.add(cause);
                    break;
                }
            }
        }
        return new BuildFailure(description, causes);
    }

    private static String toPrefixPattern(int prefix) {
        StringBuilder builder = new StringBuilder("(?m)^");
        for (int i = 0; i < prefix; i++) {
            builder.append(' ');
        }
        return builder.toString();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String resolution;
        private final List<BuildResult.Failure> problems = new ArrayList<>();
        private final List<String> lineNumbers = new ArrayList<>();
        private final List<String> fileNames = new ArrayList<>();
        private String summary;

        public Builder withResolution(String resolution) {
            this.resolution = resolution;
            return this;
        }

        public Builder withFailure(BuildResult.Failure problem) {
            this.problems.add(problem);
            return this;
        }

        public Builder addLineNumber(String lineNumber) {
            this.lineNumbers.add(lineNumber);
            return this;
        }

        public Builder addFileName(String fileName) {
            this.fileNames.add(fileName);
            return this;
        }

        public Builder withSummary(String summary) {
            this.summary = summary;
            return this;
        }

        public BuildFailures build() {
            return new BuildFailures(summary, lineNumbers, fileNames, problems, resolution);
        }
    }
}
