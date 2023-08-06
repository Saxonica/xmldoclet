package com.saxonica.xmldoclet;

import com.sun.source.doctree.*;
import net.sf.saxon.Controller;
import net.sf.saxon.event.ComplexContentOutputter;
import net.sf.saxon.event.NamespaceReducer;
import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.om.*;
import net.sf.saxon.s9api.*;
import net.sf.saxon.serialize.SerializationProperties;
import net.sf.saxon.str.StringView;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.Untyped;
import nu.validator.htmlparser.common.XmlViolationPolicy;
import nu.validator.htmlparser.dom.HtmlDocumentBuilder;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.lang.model.element.Name;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Helper class to construct well-formed XML.
 */
public class XmlBuilder {
    private final Processor processor;
    private Receiver receiver = null;
    private XdmDestination destination = null;
    private Stack<String> elementStack = null;

    public XmlBuilder() {
        this(new Processor());
    }

    public XmlBuilder(Processor processor) {
        this.processor = processor;
        elementStack = new Stack<>();
    }

    public String getXml() {
        //System.err.println("GX: " + receiver);
        return destination.getXdmNode().toString();
    }

    public void startDocument() {
        Controller controller = new Controller(processor.getUnderlyingConfiguration());
        destination = new XdmDestination();

        PipelineConfiguration pipe = controller.makePipelineConfiguration();
        SerializationProperties serprop = new SerializationProperties();
        receiver = destination.getReceiver(pipe, serprop);
        receiver.setPipelineConfiguration(pipe);

        receiver.setSystemId("https://example.com/");
        receiver = new ComplexContentOutputter(new NamespaceReducer(receiver));
        //System.err.println("SD: " + receiver);
        try {
            receiver.open();
            receiver.startDocument(0);
        } catch (XPathException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void endDocument() {
        //System.err.println("ED: " + receiver);
        try {
            while (!elementStack.isEmpty()) {
                elementStack.pop();
                receiver.endElement();
            }
            receiver.endDocument();
            receiver.close();
        } catch (XPathException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void addSubtree(XdmNode node) {
        if (node.getNodeKind() == XdmNodeKind.DOCUMENT) {
            addChildren(node);
        } else if (node.getNodeKind() == XdmNodeKind.ELEMENT) {
            startElement(node);
            addChildren(node);
            endElement();
        } else if (node.getNodeKind() == XdmNodeKind.COMMENT) {
            comment(node.getStringValue());
        } else if (node.getNodeKind() == XdmNodeKind.TEXT) {
            text(node.getStringValue());
        } else if (node.getNodeKind() == XdmNodeKind.PROCESSING_INSTRUCTION) {
            processingInstruction(node.getNodeName().getLocalName(), node.getStringValue());
        } else {
            throw new UnsupportedOperationException("Unexpected node type");
        }
    }

    public void addChildren(XdmNode node) {
        XdmSequenceIterator<XdmNode> iter = node.axisIterator(Axis.CHILD);
        while (iter.hasNext()) {
            XdmNode child = iter.next();
            addSubtree(child);
        }
    }

    private void startElement(XdmNode node) {
        AttributeMap attrs = node.getUnderlyingNode().attributes();
        startElement(node.getNodeName().getLocalName(), attrs);

    }
    private void startElement(Name name) {
        startElement(name.toString());
    }

    public void startElement(String name) {
        startElement(name, Collections.emptyMap());
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
        AttributeMap amap = EmptyAttributeMap.getInstance();
        if (attributes != null && !attributes.isEmpty()) {
            for (String aname : attributes.keySet()) {
                FingerprintedQName qname = new FingerprintedQName("", NamespaceUri.of(""), aname);
                AttributeInfo ainfo = new AttributeInfo(qname, BuiltInAtomicType.UNTYPED_ATOMIC, attributes.get(aname), VoidLocation.getInstance(), 0);
                amap = amap.put(ainfo);
            }
        }
        startElement(name, amap);
    }

    private void startElement(String name, AttributeMap attributes) {
        try {
            FingerprintedQName qname = new FingerprintedQName("", NamespaceUri.of(""), name);
            receiver.startElement(qname, Untyped.INSTANCE, attributes, NamespaceMap.emptyMap(), VoidLocation.getInstance(), 0);
            elementStack.push(name);
        } catch (XPathException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void endElement(DocTree tree) {
        if (elementStack.isEmpty()) {
            //System.err.println("Ignoring close tag: " + tree);
            return;
        }
        endElement();
    }

    public void endElement() {
        if (elementStack.isEmpty()) {
            //System.err.println("Ignoring close tag");
            return;
        }
        try {
            receiver.endElement();
            elementStack.pop();
        } catch (XPathException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void comment(String text) {
        try {
            receiver.comment(StringView.of(text), VoidLocation.getInstance(), 0);
        } catch (XPathException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void processingInstruction(String target, String data) {
        try {
            receiver.processingInstruction(target, StringView.of(data), VoidLocation.getInstance(), 0);
        } catch (XPathException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void text(String text) {
        try {
            receiver.characters(StringView.of(text), VoidLocation.getInstance(), 0);
        } catch (XPathException ex) {
            throw new RuntimeException(ex);
        }
    }

    // ==================================================================================

    private void prose(String wrapper, List<? extends DocTree> elements) {
        //System.err.println("PROSE:");
        XmlBuilder miniBuilder = new XmlBuilder();
        miniBuilder.startDocument();
        miniBuilder.startElement("body");
        for (DocTree tree : elements) {
            miniBuilder.processTree(tree);
            //System.err.println(tree);
        }
        miniBuilder.endElement();
        miniBuilder.endDocument();
        String miniXml = miniBuilder.getXml();
        //System.err.println("PROSE: " + miniXml);

        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(miniXml.getBytes(StandardCharsets.UTF_8));
            HtmlDocumentBuilder htmlBuilder = new HtmlDocumentBuilder(XmlViolationPolicy.ALTER_INFOSET);
            Document html = htmlBuilder.parse(bais);
            Processor processor = new Processor(false);
            DocumentBuilder xbuilder = processor.newDocumentBuilder();
            XdmNode doc = xbuilder.build(new DOMSource(html));

            String xml = doc.toString();
            if (xml.contains("<body/>")) {
                // nevermind
                return;
            }

            // Reparse to strip out the namespace. Hack.
            int pos = xml.indexOf("<body>");
            xml = xml.substring(pos);
            pos = xml.lastIndexOf(("</body>"));
            xml = xml.substring(0, pos+7);

            xbuilder = processor.newDocumentBuilder();
            bais = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
            SAXSource source = new SAXSource(new InputSource(bais));
            XdmNode node = xbuilder.build(source);

            XPathCompiler compiler = processor.newXPathCompiler();
            XPathExecutable exec = compiler.compile("/body");
            XPathSelector selector = exec.load();
            selector.setContextItem(node);
            XdmValue selection = selector.evaluate();

            startElement(wrapper);
            addChildren((XdmNode) selection);
            endElement();
        } catch (SAXException | SaxonApiException | IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    // ==================================================================================

    public void docTree(DocCommentTree dcTree) {
        if (dcTree == null) {
            //System.err.printf("No comments for: %s%n", elem);
            return;
        }

        processList(dcTree.getBlockTags());

        /*
        System.err.println("====================================");
        for (DocTree t : dcTree.getFirstSentence()) {
            System.err.println(t);
        }
        System.err.println("------------------------------------");
        for (DocTree t : dcTree.getBody()) {
            System.err.println(t);
        }
        System.err.println("====================================");
         */

        prose("purpose",dcTree.getFirstSentence());
        prose("description", dcTree.getBody());
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

        //System.err.println("TREE: " + tree);

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
                endElement(tree);
                break;
            case LINK:
            case LINK_PLAIN:
                LinkTree lkTree = (LinkTree) tree;
                startElement(lkTree.getTagName(), "ref", ((LinkTree) tree).getReference().getSignature());
                endElement();
                break;
            case SINCE:
                SinceTree sTree = (SinceTree) tree;
                startElement(sTree.getTagName());
                processList(sTree.getBody());
                endElement();
                break;
            case VERSION:
                VersionTree vTree = (VersionTree) tree;
                startElement(vTree.getTagName());
                processList(vTree.getBody());
                endElement();
                break;
            case VALUE:
                ValueTree valTree = (ValueTree) tree;
                startElement(valTree.getTagName());
                endElement();
                break;
            case SUMMARY:
                SummaryTree sumTree = (SummaryTree) tree;
                startElement(sumTree.getTagName());
                processList(sumTree.getSummary());
                endElement();
                break;
            case AUTHOR:
                startElement("author");
                for (DocTree name : ((AuthorTree) tree).getName()) {
                    startElement("name");
                    processTree(name);
                    endElement();
                }
                endElement();
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
                endElement();
                break;
            case SEE:
                SeeTree seeTree = (SeeTree) tree;
                startElement(seeTree.getTagName());
                processList(seeTree.getReference());
                endElement();
                break;
            case REFERENCE:
                ReferenceTree refTree = (ReferenceTree) tree;
                startElement("ref");
                text(refTree.getSignature().toString());
                endElement();
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
                endElement();
                break;
            case DEPRECATED:
                DeprecatedTree dtree = (DeprecatedTree) tree;
                startElement("deprecated");
                processList(dtree.getBody());
                endElement();
                break;
            case HIDDEN:
                HiddenTree htree = (HiddenTree) tree;
                startElement("hidden");
                processList(htree.getBody());
                endElement();
                break;
            case THROWS:
            case EXCEPTION:
                ThrowsTree ttree = (ThrowsTree) tree;
                startElement("throws", "exception", ttree.getExceptionName().toString());
                processList(ttree.getDescription());
                endElement();
                break;
            case UNKNOWN_BLOCK_TAG:
                UnknownBlockTagTree ubttree = (UnknownBlockTagTree) tree;
                startElement("unknown", "tagname", ubttree.getTagName());
                processList(ubttree.getContent());
                endElement();
                break;
            case ERRONEOUS:
                ErroneousTree errTree = (ErroneousTree) tree;
                startElement("error");
                text(errTree.toString());
                System.err.println(errTree.getDiagnostic().toString());
                endElement();
                break;
            default:
                System.err.printf("Unexpected tree kind: %s%n", tree.getKind().toString());
                startElement("unexpected", "type", tree.getKind().toString());
                text(tree.toString());
                endElement();
                break;
        }
    }


}
