package com.saxonica.xmldoclet;

import com.sun.source.util.DocTrees;

import javax.lang.model.element.Element;
import javax.lang.model.util.ElementScanner9;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;

public class XmlScanner extends ElementScanner9<Void, Integer> {
    final DocTrees treeUtils;
    final String destinationDir;
    final String outputFilename;
    private OutputManager manager;

    XmlScanner(DocTrees treeUtils, String destinationDir, String outputFilename) {
        this.treeUtils = treeUtils;
        this.destinationDir = destinationDir;
        this.outputFilename = outputFilename;
        this.manager = new OutputManager(destinationDir, outputFilename);
    }

    void show(Set<? extends Element> elements) {
        manager.beginOutput();
        /*
        Set<Element> subset = new HashSet<>();
        for (Element e : elements) {
            if (subset.isEmpty()) {
                subset.add(e);
            }
        }
         */
        scan(elements, 0);
        manager.endOutput();
    }

    @Override
    public Void scan(Element elem, Integer depth) {
        XmlBuilder builder = new XmlBuilder();
        XmlProcessor xmlProcessor = new XmlClass(builder, treeUtils);
        builder.startDocument();
        xmlProcessor.xmlForElement(elem);
        builder.endDocument();
        manager.output(elem, builder.getXml());
        return null;
    }
}
