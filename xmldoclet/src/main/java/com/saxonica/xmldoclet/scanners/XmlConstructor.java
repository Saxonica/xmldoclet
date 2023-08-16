package com.saxonica.xmldoclet.scanners;

import com.saxonica.xmldoclet.builder.XmlProcessor;

import javax.lang.model.element.ExecutableElement;

public class XmlConstructor extends XmlExecutableElement {
    public XmlConstructor(XmlProcessor builder, ExecutableElement element) {
        super(builder, element);
    }

    @Override
    public String typeName() {
        return "constructor";
    }
}
