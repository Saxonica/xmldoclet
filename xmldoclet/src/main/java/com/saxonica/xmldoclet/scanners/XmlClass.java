package com.saxonica.xmldoclet.scanners;

import com.saxonica.xmldoclet.XmlProcessor;

import javax.lang.model.element.TypeElement;

public class XmlClass extends XmlTypeElement {
    public XmlClass(XmlProcessor xmlproc, TypeElement element) {
        super(xmlproc, element);
    }

    @Override
    public String typeName() {
        return "class";
    }
}
