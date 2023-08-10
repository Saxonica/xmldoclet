package com.saxonica.xmldoclet.scanners;

import com.saxonica.xmldoclet.TypeUtils;
import com.saxonica.xmldoclet.XmlProcessor;
import com.sun.source.doctree.DocCommentTree;
import com.sun.source.doctree.DocTree;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.util.HashMap;
import java.util.Map;

public abstract class XmlTypeElement extends XmlScanner {
    private final TypeElement element;

    public XmlTypeElement(XmlProcessor xmlproc, TypeElement element) {
        super(xmlproc);
        this.element = element;
    }

    public abstract String typeName();

    public void scan(DocTree tree) {
        Map<String,String> attr = new HashMap<>();
        attr.put("fulltype", element.getQualifiedName().toString());
        attr.put("package", element.getEnclosingElement().toString());
        attr.put("type", element.getSimpleName().toString());
        attr.put("nesting", element.getNestingKind().toString().toLowerCase());
        attr.putAll(modifierAttributes(element));

        builder.startElement(typeName(), attr);

        if (element.getSuperclass() instanceof DeclaredType) {
            builder.startElement("superclass");
            TypeUtils.xmlType(builder, "type", element.getSuperclass());
            builder.endElement("superclass");
        }

        if (!element.getInterfaces().isEmpty()) {
            builder.startElement("interfaces");
            for (TypeMirror tm : element.getInterfaces()) {
                TypeUtils.xmlType(builder, "interface", tm);
            }
            builder.endElement("interfaces");
        }

        if (!element.getTypeParameters().isEmpty()) {
            builder.startElement("typeparams");
            for (TypeParameterElement tp : element.getTypeParameters()) {
                attr.clear();
                attr.put("fulltype", tp.toString());
                //attr.put("package", packageName(tp.toString()));
                //attr.put("type", className(tp.toString()));
                builder.startElement("typeparam", attr);
                builder.endElement("typeparam");
            }
            builder.endElement("typeparams");
        }

        if (tree instanceof DocCommentTree) {
            DocCommentTree dcTree = (DocCommentTree) tree;
            builder.processList(dcTree.getBlockTags());
            builder.html("purpose", dcTree.getFirstSentence());
            builder.html("description", dcTree.getBody());
        }

        builder.xmlscan(element.getEnclosedElements());

        builder.endElement(typeName());
    }
}
