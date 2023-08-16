package com.saxonica.xmldoclet.scanners;

import com.saxonica.xmldoclet.builder.XmlProcessor;
import com.sun.source.doctree.DocCommentTree;
import com.sun.source.doctree.DocTree;

import javax.lang.model.element.Element;
import javax.lang.model.element.ModuleElement;
import java.util.HashMap;
import java.util.Map;

public class XmlModule extends XmlScanner {
    private final ModuleElement element;

    public XmlModule(XmlProcessor xmlproc, ModuleElement element) {
        super(xmlproc);
        this.element = element;
    }

    @Override
    public void scan(DocTree tree) {
        Map<String,String> attributes = new HashMap<>();
        attributes.put("fullname", element.getQualifiedName().toString());
        attributes.put("name", element.getSimpleName().toString());
        builder.startElement("module", attributes);

        if (tree instanceof DocCommentTree) {
            DocCommentTree dcTree = (DocCommentTree) tree;
            builder.processList(dcTree.getBlockTags());
            builder.processList("purpose", dcTree.getFirstSentence());
            builder.processList("description", dcTree.getBody());
        }

        for (Element child : element.getEnclosedElements()) {
            attributes.clear();
            attributes.put("fullname", child.toString());
            attributes.put("name", child.getSimpleName().toString());

            switch (child.getKind()) {
                case PACKAGE:
                    builder.startElement("packageref", attributes);
                    builder.endElement("packageref");
                    break;
                default:
                    System.err.println("Unexpected element in module: " + child);
                    break;
            }
        }

        builder.endElement("module");
    }

}
