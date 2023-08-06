package com.saxonica.xmldoclet;

import com.sun.source.util.DocTrees;

import javax.lang.model.AnnotatedConstruct;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import java.util.HashMap;
import java.util.Map;

public class XmlAnnotationType extends XmlProcessor {
    /** The constructor.
     *
     * @param builder the builder
     * @param trees the trees
     */
    public XmlAnnotationType(XmlBuilder builder, DocTrees trees) {
        super(builder, trees);
    }

    @Override
    public void xml(Element elem) {
        AnnotatedConstruct xelem = (AnnotatedConstruct) elem;

        Map<String,String> attr = new HashMap<>();
        attr.put("fulltype", xelem.toString());
        attr.put("type", className(xelem.toString()));
        builder.startElement("annotationtype", attr);
        builder.comment(" It's not clear how to access more information about the annotation type ");
        builder.endElement();
    }

}
