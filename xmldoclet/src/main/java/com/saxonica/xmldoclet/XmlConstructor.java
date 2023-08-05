package com.saxonica.xmldoclet;

import com.sun.source.util.DocTrees;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;

public class XmlConstructor extends XmlExecutable {
    public XmlConstructor(XmlBuilder builder, DocTrees trees) {
        super(builder, trees, "constructor");
    }


}
