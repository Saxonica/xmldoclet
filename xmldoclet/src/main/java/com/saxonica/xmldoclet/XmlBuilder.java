package com.saxonica.xmldoclet;

import com.sun.source.doctree.*;

import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import javax.lang.model.element.VariableElement;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Helper class to construct well-formed XML.
 * <p>Obviously, this should just be done with a Saxon Processor and a DocumentBuilder. Unfortunately,
 * I couldn't figure out how to get the new jdk.doclet builder to use jars passed on the -classpath
 * and I got impatient.</p>
 */
public class XmlBuilder {
    private final int DOCUMENT = 0;
    private final int ELEMENT = 1;
    private final int CONTENT = 2;

    private ByteArrayOutputStream stream = null;
    private PrintStream out = null;
    private String xml;
    private final Stack<String> elemStack = new Stack<>();
    private int state = DOCUMENT;

    public String getXml() {
        return xml;
    }

    public void startDocument() {
        stream = new ByteArrayOutputStream();
        out = new PrintStream(stream);
    }

    public void endDocument() {
        while (!elemStack.isEmpty()) {
            endElement(elemStack.peek());
        }

        xml = stream.toString(StandardCharsets.UTF_8);
        out = null;
        stream = null;
        state = DOCUMENT;
    }

    public void startElement(String name) {
        startElement(name, Collections.emptyMap());
    }

    private void startElement(Name name) {
        startElement(name.toString(), Collections.emptyMap());
    }

    public void startElement(String name, String... pairs) {
        if (pairs.length %2 != 0) {
            throw new IllegalArgumentException("Odd number of name/value pairs for attributes");
        }

        Map<String,String> attr = new HashMap<>();
        for (int pos = 0; pos < pairs.length; pos += 2) {
            attr.put(pairs[pos], pairs[pos+1]);
        }

        startElement(name, attr);
    }

    public void startElement(String name, Map<String,String> attributes) {
        if (state == ELEMENT) {
            out.print(">");
        }

        //System.err.printf("PUSH %s%n", name);
        elemStack.push(name);
        out.printf("<%s", name);

        if (attributes != null && !attributes.isEmpty()) {
            String[] names = new String[attributes.size()];
            int pos = 0;
            for (String aname : attributes.keySet()) {
                names[pos] = aname;
                pos++;
            }
            Arrays.sort(names);
            for (String aname : names) {
                String value = escape(attributes.get(aname)).replace("\"", "&quot;");
                out.printf(" %s=\"%s\"", aname, value);
            }
        }

        state = ELEMENT;
    }

    private void endElement(Name name) {
        endElement(name.toString());
    }

    public void endElement(String name) {
        //System.err.printf("POP %s%n", name);
        if (!name.equals(elemStack.peek())) {
            System.err.printf("BAD XML: %s ends %s%n", name, elemStack.peek());
        }
        if (state == ELEMENT) {
            out.print("/>");
        } else {
            out.printf("</%s>", name);
        }
        elemStack.pop();
        state = CONTENT;
    }

    public void comment(String text) {
        if (state == ELEMENT) {
            out.print(">");
        }
        out.printf("<!--%s-->", escape(text));
        state = CONTENT;
    }

    public void processingInstruction(String target, String data) {
        if (state == ELEMENT) {
            out.print(">");
        }
        out.printf("<?%s", target);
        if (data != null && !data.isEmpty()) {
            out.printf(" %s", escape(data));
        }
        out.print("?>");
        state = CONTENT;
    }

    public void text(String text) {
        if (state == ELEMENT) {
            out.print(">");
        }
        out.print(escape(text));
        state = CONTENT;
    }

    public void nl() {
        text("\n");
    }

    private String escape(String str) {
        return str.replace("&", "&amp;").replace("<", "&lt;");
    }

    // ==================================================================================

    public void docTree(Element elem, DocCommentTree dcTree) {
        if (dcTree == null) {
            //System.err.printf("No comments for: %s%n", elem);
            return;
        }

        processList(dcTree.getBlockTags());

        startElement("purpose");
        processList(dcTree.getFirstSentence());
        endElement("purpose");
        nl();

        if (!dcTree.getBody().isEmpty()) {
            startElement("description");
            nl();
            processList(dcTree.getBody());
            nl();
            endElement("description");
            nl();
        }
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
                startElement(selem.getName().toString(), attr);
                break;
            case END_ELEMENT:
                endElement(((EndElementTree) tree).getName());
                break;
            case LINK:
            case LINK_PLAIN:
                LinkTree lkTree = (LinkTree) tree;
                startElement(lkTree.getTagName(), "ref", ((LinkTree) tree).getReference().getSignature());
                endElement(lkTree.getTagName());
                break;
            case SINCE:
                SinceTree sTree = (SinceTree) tree;
                startElement(sTree.getTagName());
                processList(sTree.getBody());
                endElement(sTree.getTagName());
                nl();
                break;
            case VERSION:
                VersionTree vTree = (VersionTree) tree;
                startElement(vTree.getTagName());
                processList(vTree.getBody());
                endElement(vTree.getTagName());
                nl();
                break;
            case VALUE:
                ValueTree valTree = (ValueTree) tree;
                startElement(valTree.getTagName());
                endElement(valTree.getTagName());
                nl();
                break;
            case SUMMARY:
                SummaryTree sumTree = (SummaryTree) tree;
                startElement(sumTree.getTagName());
                processList(sumTree.getSummary());
                endElement(sumTree.getTagName());
                nl();
                break;
            case AUTHOR:
                nl();
                startElement("author");
                nl();
                for (DocTree name : ((AuthorTree) tree).getName()) {
                    startElement("name");
                    processTree(name);
                    endElement("name");
                    nl();
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
                EntityTree etree = (EntityTree) tree;
                startElement("ent", "name", etree.getName().toString());
                endElement("ent");
                break;
            case DEPRECATED:
                DeprecatedTree dtree = (DeprecatedTree) tree;
                startElement("deprecated");
                processList(dtree.getBody());
                endElement("deprecated");
                nl();
                break;
            case HIDDEN:
                HiddenTree htree = (HiddenTree) tree;
                startElement("hidden");
                processList(htree.getBody());
                endElement("hidden");
                nl();
                break;
            case THROWS:
            case EXCEPTION:
                ThrowsTree ttree = (ThrowsTree) tree;
                startElement("throws", "exception", ttree.getExceptionName().toString());
                processList(ttree.getDescription());
                endElement("throws");
                nl();
                break;
            case UNKNOWN_BLOCK_TAG:
                UnknownBlockTagTree ubttree = (UnknownBlockTagTree) tree;
                startElement("unknown", "tagname", ubttree.getTagName());
                processList(ubttree.getContent());
                endElement("unknown");
                nl();
                break;
            default:
                System.err.printf("Unexpected tree kind: %s%n", tree.getKind().toString());
                startElement("unexpected", "type", tree.getKind().toString());
                text(tree.toString());
                endElement("unexpected");
                nl();
                break;
        }
    }
}
