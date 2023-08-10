package com.saxonica.xmldoclet.scanners;

import com.saxonica.xmldoclet.XmlProcessor;

import javax.lang.model.element.ExecutableElement;

public class XmlMethod extends XmlExecutableElement {
    public XmlMethod(XmlProcessor builder, ExecutableElement element) {
        super(builder, element);
    }

    @Override
    public String typeName() {
        return "method";
    }
}
