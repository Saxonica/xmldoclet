package com.saxonica.xmldoclet.scanners;

import com.saxonica.xmldoclet.TypeUtils;
import com.saxonica.xmldoclet.XmlProcessor;
import com.sun.source.doctree.DocCommentTree;
import com.sun.source.doctree.DocTree;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import java.util.HashMap;
import java.util.Map;

public abstract class XmlVariableElement extends XmlScanner {
    private final VariableElement element;

    public XmlVariableElement(XmlProcessor xmlproc, VariableElement element) {
        super(xmlproc);
        this.element = element;
    }

    public abstract String typeName();

    @Override
    public void scan(DocTree tree) {
        Map<String,String> attr = new HashMap<>();
        attr.put("name", element.getSimpleName().toString());
        attr.putAll(modifierAttributes(element));
        if (element.getConstantValue() != null) {
            if (element.getConstantValue() instanceof Character) {
                Character ch = (Character) element.getConstantValue();
                if (Character.isSurrogate(ch)) {
                    attr.put("value", String.format("(char) 0x%04x", (int) ch));
                } else {
                    attr.put("value", element.getConstantValue().toString());
                }
            } else {
                attr.put("value", element.getConstantValue().toString());
            }
        }

        builder.startElement(typeName(), attr);

        TypeUtils.xmlType(builder, "type", element.asType());

        if (tree instanceof DocCommentTree) {
            DocCommentTree dcTree = (DocCommentTree) tree;
            builder.processList(dcTree.getBlockTags());
            builder.html("purpose", dcTree.getFirstSentence());
            builder.html("description", dcTree.getBody());
        }

        builder.endElement(typeName());
    }
}
