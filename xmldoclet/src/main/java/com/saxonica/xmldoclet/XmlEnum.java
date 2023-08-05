package com.saxonica.xmldoclet;

import com.sun.source.util.DocTrees;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.util.HashMap;
import java.util.Map;

public class XmlEnum extends XmlProcessor {
    public XmlEnum(XmlBuilder builder, DocTrees trees) {
        super(builder, trees);
    }

    public void xml(Element enumElem) {
        TypeElement xelem = (TypeElement) enumElem;

        Map<String,String> attr = new HashMap<>();
        attr.put("name", xelem.toString());
        attr.put("simpleName", xelem.getSimpleName().toString());
        for (Modifier modifier : xelem.getModifiers()) {
            attr.put(modifier.toString(), "true");
        }

        builder.startElement("enum", attr);
        builder.nl();
        builder.docTree(enumElem, docTrees.getDocCommentTree(enumElem));
        builder.nl();

        for (Element child : enumElem.getEnclosedElements()) {
            if (child.getKind() == ElementKind.ENUM_CONSTANT) {
                builder.startElement("value", "name", child.toString());
                builder.endElement("value");
                builder.nl();
            }
        }

        builder.endElement("enum");
        builder.nl();
        builder.nl();
    }

}
