package com.saxonica.xmldoclet.builder;

import com.saxonica.xmldoclet.utils.VoidLocation;
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
import org.xml.sax.SAXException;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.dom.DOMSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class XmlBuilder extends MarkupBuilder {
    private Receiver receiver = null;
    private XdmDestination destination = null;

    public XmlBuilder(XmlProcessor xmlProcessor) {
        super(xmlProcessor);
    }

    @Override
    public void startDocument() {
        Controller controller = new Controller(xmlProcessor.processor.getUnderlyingConfiguration());
        destination = new XdmDestination();

        PipelineConfiguration pipe = controller.makePipelineConfiguration();
        receiver = destination.getReceiver(pipe, new SerializationProperties());
        receiver.setPipelineConfiguration(pipe);

        receiver.setSystemId("https://example.com/");
        receiver = new ComplexContentOutputter(new NamespaceReducer(receiver));
        try {
            receiver.open();
            receiver.startDocument(0);
        } catch (XPathException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void endDocument() {
        try {
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
            endElement(node.getNodeName().getLocalName());
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
        AttributeMap amap = EmptyAttributeMap.getInstance();
        for (AttributeInfo info : node.getUnderlyingNode().attributes().asList()) {
            FingerprintedQName fqname = new FingerprintedQName("", NamespaceUri.of(""), info.getNodeName().getLocalPart());
            AttributeInfo newInfo = new AttributeInfo(fqname, info.getType(), info.getValue(), info.getLocation(), info.getProperties());
            //AttributeInfo newInfo = new AttributeInfo(info.getNodeName(), info.getType(), info.getValue(), info.getLocation(), info.getProperties());
            amap = amap.put(newInfo);
        }

        try {
            FingerprintedQName qname = new FingerprintedQName("", NamespaceUri.of(node.getNodeName().getNamespace()), node.getNodeName().getLocalName());
            receiver.startElement(qname, Untyped.INSTANCE, amap, node.getUnderlyingNode().getAllNamespaces(), VoidLocation.getInstance(), 0);
        } catch (XPathException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void startElement(String name, Map<String, String> attributes) {
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
            //System.err.printf("S: %s%n", name);
            receiver.startElement(qname, Untyped.INSTANCE, attributes, NamespaceMap.emptyMap(), VoidLocation.getInstance(), 0);
        } catch (XPathException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void endElement(String name) {
        try {
            //System.err.printf("E: %s%n", name);
            receiver.endElement();
        } catch (XPathException | IllegalStateException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void text(String text) {
        try {
            receiver.characters(StringView.of(text), VoidLocation.getInstance(), 0);
        } catch (XPathException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void comment(String text) {
        try {
            receiver.comment(StringView.of(text), VoidLocation.getInstance(), 0);
        } catch (XPathException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void processingInstruction(String target, String data) {
        try {
            receiver.processingInstruction(target, StringView.of(data), VoidLocation.getInstance(), 0);
        } catch (XPathException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public String getXml() {
        XdmNode doc = destination.getXdmNode();
        SerializationProperties serprop = new SerializationProperties();
        serprop.setProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        serprop.setProperty(OutputKeys.INDENT, "yes");
        Serializer serializer = xmlProcessor.processor.newSerializer();
        serializer.setOutputProperties(serprop);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        serializer.setOutputStream(baos);
        try {
            serializer.serializeNode(doc);
        } catch (SaxonApiException ex) {
            throw new RuntimeException(ex);
        }
        return baos.toString(StandardCharsets.UTF_8);
    }

    // ================================================
    // HTML methods

    public void html(DocTree element) {
        html(null, element);
    }

    public void html(String wrapper, DocTree element) {
        html(wrapper, Collections.singletonList(element));
    }

    public void html(List<? extends DocTree> elements) {
        html(null, elements);
    }

    public void html(String wrapper, List<? extends DocTree> elements) {
        if (elements.isEmpty()) {
            return;
        }

        HtmlBuilder miniBuilder = new HtmlBuilder(xmlProcessor);
        miniBuilder.startDocument();
        miniBuilder.startElement("body");
        miniBuilder.processList(elements);
        miniBuilder.endElement("body");
        miniBuilder.endDocument();
        String miniXml = miniBuilder.getXml();

        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(miniXml.getBytes(StandardCharsets.UTF_8));
            HtmlDocumentBuilder htmlBuilder = new HtmlDocumentBuilder(XmlViolationPolicy.ALTER_INFOSET);
            Document html = htmlBuilder.parse(bais);
            DocumentBuilder xbuilder = xmlProcessor.processor.newDocumentBuilder();
            XdmNode doc = xbuilder.build(new DOMSource(html));

            String xml = doc.toString();
            if (xml.contains("<body/>")) {
                // nevermind
                return;
            }

            XPathCompiler compiler = xmlProcessor.processor.newXPathCompiler();
            XPathExecutable exec = compiler.compile("/*/*:body");
            XPathSelector selector = exec.load();
            selector.setContextItem(doc);
            XdmValue selection = selector.evaluate();

            if (selection instanceof XdmNode) {
                if (wrapper != null) {
                    startElement(wrapper);
                }
                addSubtree((XdmNode) selection);
                if (wrapper != null) {
                    endElement(wrapper);
                }
            }
        } catch (SAXException | SaxonApiException | IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    // ================================================

    public void processTree(DocTree tree) {
        if (tree == null) {
            return;
        }

        Map<String,String> attr = new HashMap<>();
        switch (tree.getKind()) {
            case TEXT:
                text(((TextTree) tree).getBody());
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
                html(sTree.getBody());
                endElement(sTree.getTagName());
                break;
            case VERSION:
                VersionTree vTree = (VersionTree) tree;
                startElement(vTree.getTagName());
                html(vTree.getBody());
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
                html(((AuthorTree) tree).getName());
                endElement("author");
                break;
            case PARAM:
            case RETURN:
                // Handled in enclosing class
                break;
            case DEPRECATED:
                DeprecatedTree depTree = (DeprecatedTree) tree;
                startElement(depTree.getTagName());
                html(depTree.getBody());
                endElement(depTree.getTagName());
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
                html(seeTree.getReference());
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
            case HIDDEN:
                HiddenTree htree = (HiddenTree) tree;
                startElement("hidden");
                html(htree.getBody());
                endElement("hidden");
                break;
            case THROWS:
            case EXCEPTION:
                ThrowsTree ttree = (ThrowsTree) tree;
                startElement("throws", list2map("exception", ttree.getExceptionName().toString()));
                html(ttree.getDescription());
                endElement("throws");
                break;
            case UNKNOWN_BLOCK_TAG:
                UnknownBlockTagTree ubttree = (UnknownBlockTagTree) tree;
                startElement("unknown", list2map("tagname", ubttree.getTagName()));
                html(ubttree.getContent());
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
                System.err.printf("Unexpected XML tree kind: %s%n", tree.getKind().toString());
                startElement("unexpected", list2map("type", tree.getKind().toString()));
                text(tree.toString());
                endElement("unexpected");
                break;
        }
    }
}
