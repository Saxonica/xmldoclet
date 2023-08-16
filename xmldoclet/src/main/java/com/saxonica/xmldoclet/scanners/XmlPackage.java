package com.saxonica.xmldoclet.scanners;

import com.saxonica.xmldoclet.utils.TypeUtils;
import com.saxonica.xmldoclet.builder.XmlProcessor;
import com.sun.source.doctree.DocCommentTree;
import com.sun.source.doctree.DocTree;

import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import java.util.HashMap;
import java.util.Map;

public class XmlPackage extends XmlScanner {
    private final PackageElement element;

    public XmlPackage(XmlProcessor xmlproc, PackageElement element) {
        super(xmlproc);
        this.element = element;
    }

    public void scan(DocTree tree) {
        Map<String,String> attributes = new HashMap<>();
        attributes.put("name", element.toString());
        attributes.put("simplename", element.getSimpleName().toString());
        builder.startElement("package", attributes);

        if (tree instanceof DocCommentTree) {
            DocCommentTree dcTree = (DocCommentTree) tree;
            builder.processList(dcTree.getBlockTags());
            builder.processList("purpose", dcTree.getFirstSentence());
            builder.processList("description", dcTree.getBody());
        }

        for (Element child : element.getEnclosedElements()) {
            switch (child.getKind()) {
                case CLASS:
                    TypeUtils.xmlType(builder, "classref", child.asType());
                    break;
                case INTERFACE:
                    TypeUtils.xmlType(builder, "interfaceref", child.asType());
                    break;
                case ENUM:
                    TypeUtils.xmlType(builder, "enumref", child.asType());
                    break;
                case ANNOTATION_TYPE:
                    TypeUtils.xmlType(builder, "annotationtyperef", child.asType());
                    break;
                default:
                    System.err.println("Unexpected element in package: " + child);
                    break;
            }
        }

        builder.endElement("package");
    }
}
