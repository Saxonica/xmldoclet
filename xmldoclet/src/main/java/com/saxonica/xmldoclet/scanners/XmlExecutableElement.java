package com.saxonica.xmldoclet.scanners;

import com.saxonica.xmldoclet.TypeUtils;
import com.saxonica.xmldoclet.XmlProcessor;
import com.sun.source.doctree.DocCommentTree;
import com.sun.source.doctree.DocTree;
import com.sun.source.doctree.ParamTree;
import com.sun.source.doctree.ReturnTree;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import java.util.HashMap;
import java.util.Map;

public abstract class XmlExecutableElement extends XmlScanner {
    private final ExecutableElement element;

    public XmlExecutableElement(XmlProcessor xmlproc, ExecutableElement element) {
        super(xmlproc);
        this.element = element;
    }

    public abstract String typeName();

    public void scan(DocTree tree) {
        Map<String,String> attr = new HashMap<>();

        // Hack
        if (!"constructor".equals(typeName())) {
            attr.put("name", element.getSimpleName().toString());
        }

        for (Modifier modifier : element.getModifiers()) {
            attr.put(modifier.toString(), "true");
        }

        builder.startElement(typeName(), attr);

        DocCommentTree comments = (tree instanceof DocCommentTree) ? (DocCommentTree) tree : null;

        if (tree instanceof DocCommentTree) {
            DocCommentTree dcTree = (DocCommentTree) tree;
            builder.processList(dcTree.getBlockTags());
            builder.html("purpose", dcTree.getFirstSentence());
            builder.html("description", dcTree.getBody());
        }

        //xmlForChildren(xelem);

        ReturnTree returns = null;
        Map<String, ParamTree> params = new HashMap<>();
        if (comments != null) {
            for (DocTree block : comments.getBlockTags()) {
                switch (block.getKind()) {
                    case PARAM:
                        ParamTree ptree = (ParamTree) block;
                        params.put(ptree.getName().toString(), ptree);
                        break;
                    case RETURN:
                        returns = (ReturnTree) block;
                        break;
                    default:
                        break;
                }
            }
        }

        if (!"constructor".equals(typeName()) && element.getReturnType() != null) {
            builder.startElement("return");
            TypeUtils.xmlType(builder, "type", element.getReturnType());
            if (returns != null) {
                builder.html(returns.getDescription());
            }
            builder.endElement("return");
        }

        builder.endElement(typeName());
    }
}
