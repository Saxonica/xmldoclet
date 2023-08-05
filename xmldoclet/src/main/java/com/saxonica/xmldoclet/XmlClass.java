package com.saxonica.xmldoclet;

import com.sun.source.util.DocTrees;

public class XmlClass extends XmlTypeElement {
    public XmlClass(XmlBuilder builder, DocTrees trees) {
        super(builder, trees, "class");
    }
}
