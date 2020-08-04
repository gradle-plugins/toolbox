package dev.gradleplugins.fixtures.vcs;

public class GradleSourceDependencyUtils {
    public static String configureDeclarativeSourceDependencyFor(Object gitRepository, String coordinates) {
        return configureDeclarativeSourceDependencyFor(gitRepository, coordinates, "");
    }

    public static String configureDeclarativeSourceDependencyFor(Object gitRepository, String coordinates, String repositoryDefinition) {
        return "sourceControl {\n"
            + "    gitRepository(\"" + gitRepository.toString() + "\") {\n"
            + "        producesModule(\"" + coordinates + "\")\n"
            + "        " + repositoryDefinition + "\n"
            + "    }\n"
            + "}\n";
    }

//    public static String configureMappingSourceDependencyFor(String gitRepository, String coordinates, String repositoryDefinition) {
//        return "sourceControl {\n"
//                + "    vcsMappings {\n"
//                + "        withModule(\"" + coordinates + "\") {\n"
//                + "            from(" + GitVersionControlSpec.name + ") {\n"
//                + "                url = uri(\"" + gitRepository + "\")\n"
//                + "                " + repositoryDefinition + "\n"
//                + "            }\n"
//                + "        }\n"
//                + "    }\n"
//                + "}\n";
//    }
}
