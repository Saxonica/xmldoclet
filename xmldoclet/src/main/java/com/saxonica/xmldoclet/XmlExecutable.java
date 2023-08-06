package com.saxonica.xmldoclet;

import com.sun.source.doctree.DocCommentTree;
import com.sun.source.doctree.DocTree;
import com.sun.source.doctree.ParamTree;
import com.sun.source.doctree.ReturnTree;
import com.sun.source.util.DocTrees;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import java.util.HashMap;
import java.util.Map;

public class XmlExecutable extends XmlProcessor {
    /** The XML element name.
     * <p>The {@linkplain XmlExecutable} class does most of the processing for
     * its subclasses. The <code>name</code> allows them to change the
     * name of the XML element name (e.g. <code>constructor</code> or
     * <code>method</code>).</p>
     */
    private final String name;

    public XmlExecutable(XmlBuilder builder, DocTrees trees, String name) {
        super(builder, trees);
        this.name = name;
    }

    @Override
    public void xml(Element elem) {
        ExecutableElement xelem = (ExecutableElement) elem;

        Map<String,String> attr = new HashMap<>();

        // Hack
        if (!"constructor".equals(name)) {
            attr.put("name", xelem.getSimpleName().toString());
        }

        for (Modifier modifier : xelem.getModifiers()) {
            attr.put(modifier.toString(), "true");
        }

        DocCommentTree comments = docTrees.getDocCommentTree(xelem);

        builder.startElement(name, attr);

        builder.docTree(comments);

        xmlForChildren(xelem);

        ReturnTree returns = null;
        Map<String,ParamTree> params = new HashMap<>();
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

        for (VariableElement param : xelem.getParameters()) {
            attr.clear();
            attr.put("name", param.getSimpleName().toString());
            attr.put("fulltype", param.asType().toString());
            attr.put("type", className(param.asType().toString()));
            builder.startElement("param", attr);
            builder.docTree(docTrees.getDocCommentTree(param));
            ParamTree ptree = params.getOrDefault(param.getSimpleName().toString(), null);
            if (ptree != null) {
                builder.processList(ptree.getDescription());
            }
            builder.endElement();
        }

        if (!"constructor".equals(name) && xelem.getReturnType() != null) {
            attr.clear();
            attr.put("fulltype", xelem.getReturnType().toString());
            attr.put("type", className(xelem.getReturnType().toString()));
            builder.startElement("return", attr);
            if (returns != null) {
                builder.processList(returns.getDescription());
            }
            builder.endElement();
        }

        builder.endElement();
    }
}
