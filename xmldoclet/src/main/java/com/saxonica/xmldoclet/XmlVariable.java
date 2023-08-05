package com.saxonica.xmldoclet;

import com.sun.source.util.DocTrees;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import java.util.HashMap;
import java.util.Map;

public class XmlVariable extends XmlProcessor {
    private final String name;

    public XmlVariable(XmlBuilder builder, DocTrees trees, String name) {
        super(builder, trees);
        this.name = name;
    }

    @Override
    public void xml(Element elem) {
        VariableElement xelem = (VariableElement) elem;

        Map<String,String> attr = new HashMap<>();
        attr.put("name", xelem.getSimpleName().toString());
        attr.put("type", xelem.asType().toString());
        if (xelem.getConstantValue() != null) {
            attr.put("value", xelem.getConstantValue().toString());
        }
        for (Modifier modifier : xelem.getModifiers()) {
            attr.put(modifier.toString(), "true");
        }

        builder.startElement(name, attr);
        builder.nl();
        builder.docTree(xelem, docTrees.getDocCommentTree(xelem));

        xmlForChildren(xelem);

        builder.endElement(name);
        builder.nl();
        builder.nl();
    }
}
