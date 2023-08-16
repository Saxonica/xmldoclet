package com.saxonica.xmldoclet.scanners;

import com.saxonica.xmldoclet.builder.XmlProcessor;

import javax.lang.model.element.VariableElement;

public class XmlEnumConstant extends XmlVariableElement {
    public XmlEnumConstant(XmlProcessor builder, VariableElement element) {
        super(builder, element);
    }

    @Override
    public String typeName() {
        return "constant";
    }
}
