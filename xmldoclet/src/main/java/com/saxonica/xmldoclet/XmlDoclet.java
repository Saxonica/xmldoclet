package com.saxonica.xmldoclet;
 
import java.util.*;
import javax.lang.model.SourceVersion;

import com.saxonica.xmldoclet.builder.XmlProcessor;
import jdk.javadoc.doclet.Doclet;
import jdk.javadoc.doclet.DocletEnvironment;
import jdk.javadoc.doclet.Reporter;

/**
 * A doclet that produces an XML representation of the JavaDoc.
 * <p>This doclet accpets but ignores several options that older JavaDoc invocations seem to use,
 * such as <code>-d</code>, <code>-doctitle</code>, <code>-windowtitle</code>,
 * and <code>notimestamp</code></p>
 */
public class XmlDoclet implements Doclet {
    private static final boolean OK = true;

    /** The root directory where Javadoc is output. */
    private String destinationDir;
    /** Passed by Gradle; ignored. */
    private String outputFile = null;
    private String doctitle;
    /** Passed by Gradle; ignored. */
    private String windowtitle;
    /** Passed by Gradle; ignored. */
    private boolean notimestamp;
    private List<String> sourcepath = null;

    private final Set<Option> options = Set.of(
            new DocletOption("-d", true, "Output directory", "<string>") {
                @Override
                public boolean process(String option,
                                       List<String> arguments) {
                    destinationDir = arguments.get(0);
                    return OK;
                }
            },
            new DocletOption("-outputfile", true, "Output filename", "<string>") {
                @Override
                public boolean process(String option,
                                       List<String> arguments) {
                    outputFile = arguments.get(0);
                    return OK;
                }
            },
            new DocletOption("-sourcepath", true, "The source path", "<string>") {
                @Override
                public boolean process(String option,
                                       List<String> arguments) {
                    String paths = arguments.get(0);
                    sourcepath = new ArrayList<>(Arrays.asList(paths.split(System.getProperty("path.separator"))));
                    return OK;
                }
            },
            new DocletOption("-doctitle", true, "Ignored", "<string>") {
                @Override
                public boolean process(String option,
                                       List<String> arguments) {
                    doctitle = arguments.get(0);
                    return OK;
                }
            },
            new DocletOption("-windowtitle", true, "Ignored", "<string>") {
                @Override
                public boolean process(String option,
                                       List<String> arguments) {
                    windowtitle = arguments.get(0);
                    return OK;
                }
            },
            new DocletOption("-notimestamp", false, "Ignored", null) {
                @Override
                public boolean process(String option,
                                       List<String> arguments) {
                    notimestamp = true;
                    return OK;
                }
            }
    );

    @Override
    public void init(Locale locale, Reporter reporter) {
    }

    /**
     * Get the doclet name.
     * @return the doclet name.
     */
    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    @Override
    public Set<? extends Option> getSupportedOptions() {
        return options;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    @Override
    public boolean run(DocletEnvironment environment) {
        if (sourcepath == null) {
            throw new IllegalArgumentException("The -sourcepath must be provided");
        }

        XmlProcessor scanner = new XmlProcessor(environment, sourcepath, destinationDir, outputFile);
        scanner.scan();
        return OK;
    }

    abstract public class DocletOption implements Doclet.Option {
        private final String name;
        private final boolean hasArg;
        private final String description;
        private final String parameters;

        DocletOption(String name, boolean hasArg,
                     String description, String parameters) {
            this.name = name;
            this.hasArg = hasArg;
            this.description = description;
            this.parameters = parameters;
        }

        @Override
        public int getArgumentCount() {
            return hasArg ? 1 : 0;
        }

        @Override
        public String getDescription() {
            return description;
        }

        @Override
        public Kind getKind() {
            return Kind.STANDARD;
        }

        @Override
        public List<String> getNames() {
            return List.of(name);
        }

        @Override
        public String getParameters() {
            return hasArg ? parameters : "";
        }
    }
}
