package com.saxonica.xmldoclet.builder;

import com.saxonica.xmldoclet.ResolvedReference;
import com.saxonica.xmldoclet.XmlProcessor;
import com.sun.source.doctree.*;

import javax.lang.model.element.Name;
import java.util.*;

public abstract class MarkupBuilder {
    protected final XmlProcessor xmlProcessor;

    public MarkupBuilder(XmlProcessor processor) {
        this.xmlProcessor = processor;
    }

    public void processList(List<? extends DocTree> elements) {
        for (DocTree elem : elements) {
            processTree(elem);
        }
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
            case SINCE:
                SinceTree sTree = (SinceTree) tree;
                startElement(sTree.getTagName());
                processList(sTree.getBody());
                endElement(sTree.getTagName());
                break;
            case VERSION:
                VersionTree vTree = (VersionTree) tree;
                startElement(vTree.getTagName());
                processList(vTree.getBody());
                endElement(vTree.getTagName());
                break;
            case VALUE:
                ValueTree valTree = (ValueTree) tree;
                startElement(valTree.getTagName());
                endElement(valTree.getTagName());
                break;
            case SUMMARY:
                SummaryTree sumTree = (SummaryTree) tree;
                startElement(sumTree.getTagName());
                processList(sumTree.getSummary());
                endElement(sumTree.getTagName());
                break;
            case AUTHOR:
                startElement("author");
                for (DocTree name : ((AuthorTree) tree).getName()) {
                    startElement("name");
                    processTree(name);
                    endElement("name");
                }
                endElement("author");
                break;
            case PARAM:
                // Handled in enclosing class
                break;
            case RETURN:
                // Handled in enclosing class
                break;
            case CODE:
            case LITERAL:
                LiteralTree ltree = (LiteralTree) tree;
                startElement("code");
                text(ltree.getBody().toString());
                endElement("code");
                break;
            case SEE:
                SeeTree seeTree = (SeeTree) tree;
                startElement(seeTree.getTagName());
                processList(seeTree.getReference());
                endElement(seeTree.getTagName());
                break;
            case COMMENT:
                CommentTree ctree = (CommentTree) tree;
                String text = ctree.getBody();
                text = text.substring(4, text.length()-3);
                comment(text);
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
            case DEPRECATED:
                DeprecatedTree dtree = (DeprecatedTree) tree;
                startElement("deprecated");
                processList(dtree.getBody());
                endElement("deprecated");
                break;
            case HIDDEN:
                HiddenTree htree = (HiddenTree) tree;
                startElement("hidden");
                processList(htree.getBody());
                endElement("hidden");
                break;
            case THROWS:
            case EXCEPTION:
                ThrowsTree ttree = (ThrowsTree) tree;
                startElement("throws", list2map("exception", ttree.getExceptionName().toString()));
                processList(ttree.getDescription());
                endElement("throws");
                break;
            case UNKNOWN_BLOCK_TAG:
                UnknownBlockTagTree ubttree = (UnknownBlockTagTree) tree;
                startElement("unknown", list2map("tagname", ubttree.getTagName()));
                processList(ubttree.getContent());
                endElement("unknown");
                break;
            case ERRONEOUS:
                ErroneousTree errTree = (ErroneousTree) tree;
                startElement("error");
                text(errTree.toString());
                System.err.println(errTree.getDiagnostic().toString());
                endElement("error");
                break;
            default:
                System.err.printf("Unexpected tree kind: %s%n", tree.getKind().toString());
                startElement("unexpected", list2map("type", tree.getKind().toString()));
                text(tree.toString());
                endElement("unexpected");
                break;
        }
    }

    private void handleRef(String wrapper, ReferenceTree ref, List<? extends DocTree> label) {
        ResolvedReference resolved = xmlProcessor.resolveReference(ref);
        Map<String,String> typeAttr = new HashMap<>();

        StringBuilder href = new StringBuilder();
        typeAttr.put("class", "ref");
        typeAttr.put("java-signature", ref.getSignature());
        if (resolved.packageName != null) {
            typeAttr.put("java-package", resolved.packageName);
            href.append(resolved.packageName);
        }
        if (resolved.className != null) {
            typeAttr.put("java-class", resolved.className);
            if (resolved.packageName != null) {
                href.append(".").append(resolved.className);
            } else {
                href.append(resolved.className);
            }
        }
        if (resolved.methodName != null) {
            typeAttr.put("java-method", resolved.methodName);
            href.append("#").append(resolved.methodName);
            href.append("(");
            for (int pos = 0; pos < resolved.args.size(); pos++) {
                if (pos > 0) {
                    href.append(",");
                }
                href.append(resolved.resolvedArgs.get(pos));
            }
            href.append(")");
        }
        if (resolved.fieldName != null) {
            typeAttr.put("java-field", resolved.fieldName);
            href.append("#").append(resolved.fieldName);
        }

        typeAttr.put("href", href.toString());
        startElement(wrapper, typeAttr);

        for (int pos = 0; pos < resolved.args.size(); pos++) {
            typeAttr.clear();
            typeAttr.put("class", "ref-param");
            typeAttr.put("java-name", resolved.args.get(pos));
            if (resolved.resolvedArgs.get(pos) != null) {
                typeAttr.put("java-fullname", resolved.resolvedArgs.get(pos));
            } else {
                typeAttr.put("java-fullname", resolved.args.get(pos));
            }
            startElement("span", typeAttr);
            endElement("span");
        }

        if (label.isEmpty()) {
            String linktext = ref.getSignature();
            int pos = linktext.indexOf("#");
            if (pos >= 0) {
                linktext = linktext.substring(pos+1);
            }
            text(linktext);
        } else {
            processList(label);
        }

        endElement(wrapper);
    }

    private Map<String,String> list2map(String... attr) {
        if (attr.length % 2 != 0) {
            throw new IllegalArgumentException("A list2map call with an odd number of arguments");
        }
        Map<String,String> attributes = new HashMap<>();
        for (int index = 0; index < attr.length; index += 2) {
            attributes.put(attr[index], attr[index+1]);
        }
        return attributes;
    }

    public abstract void startDocument();
    public abstract void endDocument();

    public void startElement(Name name) {
        startElement(name.toString());
    }

    public void startElement(String name) {
        startElement(name, Collections.emptyMap());
    }

    public void startElement(Name name, Map<String,String> attributes) {
        startElement(name.toString(), attributes);
    }

    public abstract void startElement(String name, Map<String,String> attributes);

    public void endElement(Name name) {
        endElement(name.toString());
    }
    public abstract void endElement(String name);

    public abstract void text(String text);
    public abstract void comment(String text);
    public abstract void processingInstruction(String target, String data);

    public abstract String getXml();
}
