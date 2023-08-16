package com.saxonica.xmldoclet.scanners;

import com.saxonica.xmldoclet.builder.XmlProcessor;

import javax.lang.model.element.VariableElement;

public class XmlParameter extends XmlVariableElement {
    public XmlParameter(XmlProcessor builder, VariableElement element) {
        super(builder, element);
    }

    @Override
    public String typeName() {
        return "parameter";
    }
}
