package com.saxonica.xmldoclet;
 
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.lang.model.SourceVersion;

import com.sun.source.util.DocTrees;
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
    private String outputFile = "doclet.xml";
    private String doctitle;
    /** Passed by Gradle; ignored. */
    private String windowtitle;
    /** Passed by Gradle; ignored. */
    private boolean notimestamp;

    private final Set<Option> options = Set.of(
            new DocletOption("-d", true, "passed by gradle", "<string>") {
                @Override
                public boolean process(String option,
                                       List<String> arguments) {
                    destinationDir = arguments.get(0);
                    return OK;
                }
            },
            new DocletOption("-doctitle", true, "passed by gradle", "<string>") {
                @Override
                public boolean process(String option,
                                       List<String> arguments) {
                    doctitle = arguments.get(0);
                    return OK;
                }
            },
            new DocletOption("-windowtitle", true, "passed by gradle", "<string>") {
                @Override
                public boolean process(String option,
                                       List<String> arguments) {
                    windowtitle = arguments.get(0);
                    return OK;
                }
            },
            new DocletOption("-notimestamp", false, "passed by gradle", null) {
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
        DocTrees treeUtils = environment.getDocTrees();
        XmlScanner show = new XmlScanner(treeUtils, destinationDir, outputFile);
        show.show(environment.getIncludedElements());
        return OK;
    }
}
