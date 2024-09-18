package com.saxonica.xmldoclet.scanners;

import com.saxonica.xmldoclet.builder.XmlProcessor;
import com.saxonica.xmldoclet.utils.TypeUtils;
import com.sun.source.doctree.DocCommentTree;
import com.sun.source.doctree.DocTree;
import com.sun.source.doctree.ParamTree;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
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
                if (Character.isSurrogate(ch) || ch < ' ') {
                    attr.put("value", String.format("(char) 0x%04x", (int) ch));
                } else {
                    attr.put("value", element.getConstantValue().toString());
                }
            } else {
                StringBuilder sb = new StringBuilder();
                String value = element.getConstantValue().toString();
                int offset = 0;
                int length = value.length();
                while (offset < length) {
                    char cur = value.charAt(offset);
                    if (cur < ' ' || cur > 0x7f) {
                        sb.append(String.format("\\u%04x", (int) cur));
                    } else {
                        sb.append(cur);
                    }
                    offset += Character.charCount(cur);
                }

                attr.put("value", sb.toString());
            }
        }

        builder.startElement(typeName(), attr);

        // If this is a parameter, get the ParamTree block from
        // our parent...
        ParamTree paramTree = null;
        Element parent = element.getEnclosingElement();
        if (parent.getKind() == ElementKind.METHOD || parent.getKind() == ElementKind.CONSTRUCTOR) {
            DocCommentTree parentTree = builder.environment.getDocTrees().getDocCommentTree(parent);
            if (parentTree != null) {
                for (DocTree block : parentTree.getBlockTags()) {
                    if (block.getKind() == DocTree.Kind.PARAM) {
                        ParamTree ptree = (ParamTree) block;
                        if (ptree.getName().toString().equals(element.getSimpleName().toString())) {
                            paramTree = ptree;
                            break;
                        }
                    }

                }
            }
        }

        if (paramTree != null) {
            builder.processList("purpose", paramTree.getDescription());
        }

        if (tree instanceof DocCommentTree) {
            DocCommentTree dcTree = (DocCommentTree) tree;
            builder.processList(dcTree.getBlockTags());
            builder.processList("purpose", dcTree.getFirstSentence());
            builder.processList("description", dcTree.getBody());
        }

        TypeUtils.xmlType(builder, "type", element.asType());

        builder.endElement(typeName());
    }
}
