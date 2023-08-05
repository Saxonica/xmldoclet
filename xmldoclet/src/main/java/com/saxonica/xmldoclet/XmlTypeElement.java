package com.saxonica.xmldoclet;

import com.sun.source.util.DocTrees;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.util.HashMap;
import java.util.Map;

public class XmlTypeElement extends XmlProcessor {
    protected final String name;

    public XmlTypeElement(XmlBuilder builder, DocTrees trees, String name) {
        super(builder, trees);
        this.name = name;
    }

    public void xml(Element cls) {
        TypeElement xelem = (TypeElement) cls;

        Map<String,String> attr = new HashMap<>();
        attr.put("name", xelem.toString());
        attr.put("simpleName", xelem.getSimpleName().toString());
        for (Modifier modifier : xelem.getModifiers()) {
            attr.put(modifier.toString(), "true");
        }

        builder.startElement(name, attr);
        builder.nl();
        builder.docTree(cls, docTrees.getDocCommentTree(cls));

        xmlForChildren(cls);

        builder.endElement(name);
        builder.nl();
        builder.nl();
    }

}
