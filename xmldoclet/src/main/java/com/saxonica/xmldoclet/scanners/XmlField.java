package com.saxonica.xmldoclet.scanners;

import com.saxonica.xmldoclet.XmlProcessor;

import javax.lang.model.element.VariableElement;

public class XmlField extends XmlVariableElement {
    public XmlField(XmlProcessor builder, VariableElement element) {
        super(builder, element);
    }

    @Override
    public String typeName() {
        return "field";
    }
}
