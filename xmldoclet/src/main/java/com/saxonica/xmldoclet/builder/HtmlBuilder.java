package com.saxonica.xmldoclet.builder;

import com.sun.source.doctree.*;

import javax.lang.model.element.VariableElement;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class HtmlBuilder extends MarkupBuilder{
    private StringBuilder html = null;

    public HtmlBuilder(XmlProcessor processor) {
        super(processor);
    }

    @Override
    public void startDocument() {
        html = new StringBuilder();
    }

    @Override
    public void endDocument() {
        // nop
    }

    @Override
    public void startElement(String name, Map<String, String> attributes) {
        html.append("<").append(name);
        if (attributes != null) {
            for (String aname : attributes.keySet()) {
                html.append(" ").append(aname).append("='");
                html.append(escape(attributes.get(aname)));
                html.append("'");
            }
        }
        html.append(">");
    }

    @Override
    public void endElement(String name) {
        html.append("</").append(name).append(">");
    }

    @Override
    public void text(String text) {
        html.append(escape(text));
    }

    @Override
    public void comment(String text) {
        html.append("<!--").append(text).append("-->");
    }

    @Override
    public void processingInstruction(String target, String data) {
        html.append("<?").append(target);
        if (data != null && !data.isEmpty()) {
            html.append(" ").append(data);
        }
        html.append("?>");
    }

    @Override
    public String getXml() {
        return html.toString();
    }

    private String escape(String input) {
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace("'", "&apos;");
    }
    public void processTree(DocTree tree) {
        if (tree == null) {
            return;
        }

        Map<String,String> attr = new HashMap<>();
        switch (tree.getKind()) {
            case TEXT:
                text(tree.toString());
                break;
            case START_ELEMENT:
                StartElementTree selem = (StartElementTree) tree;
                for (DocTree dtree : selem.getAttributes()) {
                    AttributeTree atree = (AttributeTree) dtree;
                    attr.put(atree.getName().toString(), atree.getValue().toString());
                }
                startElement(selem.getName(), attr);
                break;
            case END_ELEMENT:
                endElement(((EndElementTree) tree).getName());
                break;
            case LINK:
            case LINK_PLAIN:
                String elementName = (tree.getKind() == DocTree.Kind.LINK) ? "a" : "span";
                handleRef(elementName, ((LinkTree) tree).getReference(), ((LinkTree) tree).getLabel());
                break;
            case REFERENCE:
                ReferenceTree refTree = (ReferenceTree) tree;
                handleRef("span", refTree, Collections.emptyList());
                break;
            case CODE:
            case LITERAL:
                LiteralTree ltree = (LiteralTree) tree;
                startElement("code");
                text(ltree.getBody().getBody());
                endElement("code");
                break;
            case COMMENT:
                String comment = ((CommentTree) tree).getBody();
                attr.put("text", comment.substring(4, comment.length()-3));
                startElement("xmldoclet-comment", attr);
                endElement("xmldoclet-comment");
                break;
            case SUMMARY:
                SummaryTree sumTree = (SummaryTree) tree;
                processList(sumTree.getSummary());
                break;
            case ENTITY:
                String name = ((EntityTree) tree).getName().toString();
                switch (name) {
                    case "amp":
                        text("&");
                        break;
                    case "lt":
                        text("<");
                        break;
                    case "gt":
                        text(">");
                        break;
                    case "apos":
                        text("'");
                        break;
                    case "quot":
                        text("\"");
                        break;
                    default:
                        startElement("span", list2map("class", "entity " + name));
                        endElement("span");
                }
                break;
            case VALUE:
                VariableElement field = xmlProcessor.currentField();
                if (field.getConstantValue() != null) {
                    text(field.getConstantValue().toString());
                } else {
                    text("???");
                }
                break;
            case ERRONEOUS:
                ErroneousTree errTree = (ErroneousTree) tree;
                startElement("error");
                text(errTree.toString());
                System.err.println(errTree.getDiagnostic().toString());
                endElement("error");
                break;
            default:
                System.err.printf("Unexpected HTML tree kind: %s%n", tree.getKind().toString());
                startElement("unexpected", list2map("type", tree.getKind().toString()));
                text(tree.toString());
                endElement("unexpected");
                break;
        }
    }
}
