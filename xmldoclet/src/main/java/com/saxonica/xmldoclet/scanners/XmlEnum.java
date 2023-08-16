package com.saxonica.xmldoclet.scanners;

import com.saxonica.xmldoclet.builder.XmlProcessor;

import javax.lang.model.element.TypeElement;

public class XmlEnum extends XmlTypeElement {
    public XmlEnum(XmlProcessor builder, TypeElement element) {
        super(builder, element);
    }

    @Override
    public String typeName() {
        return "enum";
    }
}
