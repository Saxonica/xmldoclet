package com.saxonica.xmldoclet;

import com.sun.source.util.DocTrees;

public class XmlInterface extends XmlTypeElement {
    public XmlInterface(XmlBuilder builder, DocTrees trees) {
        super(builder, trees, "interface");
    }
}
