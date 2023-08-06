package com.saxonica.xmldoclet;

import com.sun.source.doctree.DocTree;
import com.sun.source.util.DocTrees;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;

public class XmlPackage extends XmlProcessor {
    public XmlPackage(XmlBuilder builder, DocTrees trees) {
        super(builder, trees);
    }

    public void xml(Element elem) {
        PackageElement pkg = (PackageElement) elem;

        //System.err.println("PACKAGE: " + pkg);

        builder.startElement("package", "name", pkg.toString(), "simpleName", pkg.getSimpleName().toString());

        builder.docTree(docTrees.getDocCommentTree(pkg));

        for (Element child : pkg.getEnclosedElements()) {
            switch (child.getKind()) {
                case CLASS:
                    builder.startElement("classref", "name", child.toString(), "simpleName", child.getSimpleName().toString());
                    builder.endElement();
                    break;
                case INTERFACE:
                    builder.startElement("interfaceref", "name", child.toString(), "simpleName", child.getSimpleName().toString());
                    builder.endElement();
                    break;
                case ENUM:
                    builder.startElement("enumref", "name", child.toString(), "simpleName", child.getSimpleName().toString());
                    builder.endElement();
                    break;
                case ANNOTATION_TYPE:
                    builder.startElement("annotationtyperef", "name", child.toString(), "simpleName", child.getSimpleName().toString());
                    builder.endElement();
                    break;
                default:
                    System.err.println("Unexpected element in package: " + child);
                    break;
            }
        }

        builder.endElement();
    }
}
