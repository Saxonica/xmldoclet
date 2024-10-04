package com.saxonica.xmldoclet.scanners;

import com.saxonica.xmldoclet.builder.XmlProcessor;

import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;

public class XmlTypeParameter extends XmlTypeParameterElement {
    public XmlTypeParameter(XmlProcessor builder, TypeParameterElement element) {
        super(builder, element);
    }

    @Override
    public String typeName() {
        return "parameter";
    }
}
