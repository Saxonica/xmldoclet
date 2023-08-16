package com.saxonica.xmldoclet.scanners;

import com.saxonica.xmldoclet.builder.XmlProcessor;
import com.sun.source.doctree.DocTree;

import javax.lang.model.element.Element;
import java.util.HashMap;
import java.util.Map;

public class XmlAnnotation extends XmlScanner {
    private final Element element;

    public XmlAnnotation(XmlProcessor xmlproc, Element element) {
        super(xmlproc);
        this.element = element;
    }

    @Override
    public void scan(DocTree tree) {
        Map<String,String> attr = new HashMap<>();
        attr.put("fullname", element.toString());
        builder.startElement("annotationtype", attr);
        builder.comment(" It's not clear how to access more information about the annotation type ");
        builder.endElement("annotationtype");
    }
}
