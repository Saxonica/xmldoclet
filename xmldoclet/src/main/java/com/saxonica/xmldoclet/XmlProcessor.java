package com.saxonica.xmldoclet;

import com.sun.source.util.DocTrees;

import javax.lang.model.element.Element;

public class XmlProcessor {
    protected final XmlBuilder builder;
    protected final DocTrees docTrees;

    public XmlProcessor(XmlBuilder builder, DocTrees trees) {
        this.docTrees = trees;
        this.builder = builder;
    }

    public void xml(Element elem) {
        System.err.printf("Unknown element: %s%n", elem);
    }

    public void xmlForElement(Element elem) {
        XmlProcessor xelem;

        switch (elem.getKind()) {
            case PACKAGE:
                xelem = new XmlPackage(builder, docTrees);
                xelem.xml(elem);
                break;
            case CLASS:
                xelem = new XmlClass(builder, docTrees);
                xelem.xml(elem);
                break;
            case INTERFACE:
                xelem = new XmlInterface(builder, docTrees);
                xelem.xml(elem);
                break;
            case ENUM:
                xelem = new XmlEnum(builder, docTrees);
                xelem.xml(elem);
                break;
            case CONSTRUCTOR:
                xelem = new XmlConstructor(builder, docTrees);
                xelem.xml(elem);
                break;
            case METHOD:
                xelem = new XmlMethod(builder, docTrees);
                xelem.xml(elem);
                break;
            case FIELD:
                xelem = new XmlField(builder, docTrees);
                xelem.xml(elem);
                break;
            default:
                System.err.printf("Unknown element: %s%n", elem.getKind());
                break;
        }
    }

    public void xmlForChildren(Element parent) {
        for (Element child : parent.getEnclosedElements()) {
            xmlForElement(child);
        }
    }
}
