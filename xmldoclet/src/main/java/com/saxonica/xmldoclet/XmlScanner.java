package com.saxonica.xmldoclet;

import com.sun.source.util.DocTrees;

import javax.lang.model.element.Element;
import javax.lang.model.util.ElementScanner9;
import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Set;

public class XmlScanner extends ElementScanner9<Void, Integer> {
    final DocTrees treeUtils;
    final String destinationDir;

    XmlScanner(DocTrees treeUtils, String destinationDir) {
        this.treeUtils = treeUtils;
        this.destinationDir = destinationDir;
    }

    void show(Set<? extends Element> elements) {
        scan(elements, 0);
    }

    @Override
    public Void scan(Element elem, Integer depth) {
        final String filename;
        switch (elem.getKind()) {
            case PACKAGE:
                //System.err.printf("Scanning package: %s%n", elem);
                filename = destinationDir + "/" + elem.toString().replace(".", "/") + "/package.xml";
                break;
            case CLASS:
            case INTERFACE:
            case ENUM:
                //System.err.printf("Scanning class: %s%n", elem);
                filename = destinationDir + "/" + elem.toString().replace(".", "/") + ".xml";
                break;
            default:
                System.err.println("Unexpected element to scan: " + elem);
                filename = null;
                break;
        }

        XmlBuilder builder = new XmlBuilder();
        XmlProcessor xmlProcessor = new XmlClass(builder, treeUtils);
        builder.startDocument();
        xmlProcessor.xmlForElement(elem);
        builder.endDocument();

        if (filename != null) {
            try {
                File outputFile = new File(filename);
                File outputDir = outputFile.getParentFile();
                outputDir.mkdirs();
                PrintStream out = new PrintStream(outputFile);
                out.println(builder.getXml());
                out.close();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }

        return null;
    }
}
