package com.saxonica.xmldoclet;

import com.sun.source.util.DocTrees;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.TypeMirror;
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

        //System.err.println("CLASS: " + xelem.getQualifiedName());

        Map<String,String> attr = new HashMap<>();
        attr.put("fulltype", xelem.getQualifiedName().toString());
        attr.put("package", packageName(xelem.toString()));
        attr.put("type", xelem.getSimpleName().toString());
        attr.put("nesting", xelem.getNestingKind().toString().toLowerCase());
        for (Modifier modifier : xelem.getModifiers()) {
            attr.put(modifier.toString(), "true");
        }

        builder.startElement(name, attr);

        attr.clear();
        attr.put("fulltype", xelem.getSuperclass().toString());
        attr.put("package", packageName(xelem.getSuperclass().toString()));
        attr.put("type", className(xelem.getSuperclass().toString()));
        builder.startElement("superclass", attr);
        builder.endElement();

        if (!xelem.getInterfaces().isEmpty()) {
            builder.startElement("interfaces");
            for (TypeMirror tm : xelem.getInterfaces()) {
                attr.clear();
                attr.put("fulltype", tm.toString());
                attr.put("package", packageName(tm.toString()));
                attr.put("type", className(tm.toString()));
                builder.startElement("interface", attr);
                builder.endElement();
            }
            builder.endElement();
        }

        if (!xelem.getTypeParameters().isEmpty()) {
            builder.startElement("typeparams");
            for (TypeParameterElement tp : xelem.getTypeParameters()) {
                attr.clear();
                attr.put("fulltype", tp.toString());
                attr.put("package", packageName(tp.toString()));
                attr.put("type", className(tp.toString()));
                builder.startElement("typeparam", attr);
                builder.endElement();
            }
            builder.endElement();
        }

        builder.docTree(docTrees.getDocCommentTree(cls));

        xmlForChildren(cls);

        builder.endElement();
    }

}
