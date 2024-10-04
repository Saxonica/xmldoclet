package com.saxonica.xmldoclet.scanners;

import com.saxonica.xmldoclet.builder.XmlProcessor;
import com.saxonica.xmldoclet.utils.TypeUtils;
import com.sun.source.doctree.DocCommentTree;
import com.sun.source.doctree.DocTree;
import com.sun.source.doctree.ParamTree;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import java.util.HashMap;
import java.util.Map;

public abstract class XmlTypeParameterElement extends XmlScanner {
    private final TypeParameterElement element;

    public XmlTypeParameterElement(XmlProcessor xmlproc, TypeParameterElement element) {
        super(xmlproc);
        this.element = element;
    }

    public abstract String typeName();

    @Override
    public void scan(DocTree tree) {
        Map<String,String> attr = new HashMap<>();
        attr.put("name", element.getSimpleName().toString());
        attr.put("class", "type");
        attr.putAll(modifierAttributes(element));

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
