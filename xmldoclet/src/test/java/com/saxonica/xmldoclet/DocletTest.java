package com.saxonica.xmldoclet;

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
}
