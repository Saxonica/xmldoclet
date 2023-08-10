package com.saxonica.xmldoclet.scanners;

import com.saxonica.xmldoclet.XmlProcessor;

import javax.lang.model.element.TypeElement;

public class XmlInterface extends XmlTypeElement {
    public XmlInterface(XmlProcessor xmlproc, TypeElement element) {
        super(xmlproc, element);
    }

    @Override
    public String typeName() {
        return "interface";
    }
}
