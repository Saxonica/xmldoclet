package com.saxonica.xmldoclet;

import org.junit.jupiter.api.Test;

import javax.tools.DocumentationTool;
import javax.tools.ToolProvider;

public class DocletTest {
    @Test
    public void testfieldx() {
        String[] docletArgs = new String[]{
                "-doclet", XmlDoclet.class.getName(),
                "-docletpath", "build/classes/",
                "-sourcepath", "src/main/java/",
                "--add-exports", "jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED",
                XmlDoclet.class.getPackageName(),
                "com.saxonica.xmldoclet.builder"
        };

        /*
        for (String arg : docletArgs) {
            System.err.println(arg);
        }
         */

        DocumentationTool docTool = ToolProvider.getSystemDocumentationTool();
        docTool.run(System.in, System.out, System.err, docletArgs);
    }


}
