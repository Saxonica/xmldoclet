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
        builder.startElement("package", "name", pkg.toString(), "simpleName", pkg.getSimpleName().toString());
        builder.nl();

        builder.docTree(pkg, docTrees.getDocCommentTree(pkg));
        builder.nl();
        builder.nl();

        for (Element child : pkg.getEnclosedElements()) {
            switch (child.getKind()) {
                case CLASS:
                    builder.startElement("classref", "name", child.toString(), "simpleName", child.getSimpleName().toString());
                    builder.endElement("classref");
                    builder.nl();
                    break;
                case INTERFACE:
                    builder.startElement("interfaceref", "name", child.toString(), "simpleName", child.getSimpleName().toString());
                    builder.endElement("interfaceref");
                    builder.nl();
                    break;
                case ENUM:
                    builder.startElement("enumref", "name", child.toString(), "simpleName", child.getSimpleName().toString());
                    builder.endElement("enumref");
                    builder.nl();
                    break;
                default:
                    System.err.println("Unexpected element in package: " + child);
                    break;
            }
        }

        builder.endElement("package");
        builder.nl();
    }
}
