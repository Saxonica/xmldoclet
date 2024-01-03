package com.saxonica.xmldoclet;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.tools.DocumentationTool;
import javax.tools.ToolProvider;

public class DocletTest {
    @Test
    public void xmldoclet() {
        String[] docletArgs = new String[]{
                "-doclet", XmlDoclet.class.getName(),
                "-docletpath", "build/classes/",
                "-sourcepath", "src/main/java:build/generated/sources/buildConfig/main",
                XmlDoclet.class.getPackageName(),
                "com.saxonica.xmldoclet",
                "com.saxonica.xmldoclet.builder",
                "com.saxonica.xmldoclet.scanners"
        };

        DocumentationTool docTool = ToolProvider.getSystemDocumentationTool();
        docTool.run(System.in, System.out, System.err, docletArgs);
    }

    @Test
    public void sample() {
        String[] docletArgs = new String[]{
                "-doclet", XmlDoclet.class.getName(),
                "-docletpath", "build/classes/",
                "-sourcepath", "../sample/src/main/java",
                "org.example",
        };

        DocumentationTool docTool = ToolProvider.getSystemDocumentationTool();
        docTool.run(System.in, System.out, System.err, docletArgs);
    }

    // This test is only meaningful locally
    @Disabled
    public void saxonSample() {
        String[] docletArgs = new String[]{
                "-doclet", XmlDoclet.class.getName(),
                "-private",
                "-docletpath", "build/classes/",
                "-classpath", "/Users/ndw/.m2/repository/jline/jline/2.14.6/jline-2.14.6.jar:/Volumes/Saxonica/src/saxonica/saxondev/build/releases/eej/lib/xmlresolver-5.2.2.jar",
                "-sourcepath", "../../saxondev/src/main/java",
                "net.sf.saxon.trans"
        };

        DocumentationTool docTool = ToolProvider.getSystemDocumentationTool();
        docTool.run(System.in, System.out, System.err, docletArgs);
    }

}
