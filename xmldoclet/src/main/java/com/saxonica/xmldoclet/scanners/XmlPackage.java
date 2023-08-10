package com.saxonica.xmldoclet.scanners;

import com.saxonica.xmldoclet.XmlProcessor;
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
        attributes.put("simpleName", element.getSimpleName().toString());
        builder.startElement("package", attributes);

        if (tree instanceof DocCommentTree) {
            DocCommentTree dcTree = (DocCommentTree) tree;
            builder.processList(dcTree.getBlockTags());
            builder.html("purpose", dcTree.getFirstSentence());
            builder.html("description", dcTree.getBody());
        }

        for (Element child : element.getEnclosedElements()) {
            attributes.clear();
            attributes.put("name", child.toString());
            attributes.put("simpleName", child.getSimpleName().toString());

            switch (child.getKind()) {
                case CLASS:
                    builder.startElement("classref", attributes);
                    builder.endElement("classref");
                    break;
                case INTERFACE:
                    builder.startElement("interfaceref", attributes);
                    builder.endElement("interfaceref");
                    break;
                case ENUM:
                    builder.startElement("enumref", attributes);
                    builder.endElement("enumref");
                    break;
                case ANNOTATION_TYPE:
                    builder.startElement("annotationtyperef", attributes);
                    builder.endElement("annotationtyperef");
                    break;
                default:
                    System.err.println("Unexpected element in package: " + child);
                    break;
            }
        }

        builder.endElement("package");
    }
}
