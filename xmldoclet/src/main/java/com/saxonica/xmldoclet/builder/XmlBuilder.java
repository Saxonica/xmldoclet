package com.saxonica.xmldoclet.builder;

import com.saxonica.xmldoclet.VoidLocation;
import com.saxonica.xmldoclet.XmlProcessor;
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

import javax.xml.transform.OutputKeys;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class XmlBuilder extends MarkupBuilder {
    private final Processor processor;
    private Receiver receiver = null;
    private XdmDestination destination = null;

    public XmlBuilder(XmlProcessor xmlProcessor, Processor processor) {
        super(xmlProcessor);
        this.processor = processor;
    }

    @Override
    public void startDocument() {
        Controller controller = new Controller(processor.getUnderlyingConfiguration());
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
        Serializer serializer = processor.newSerializer();
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
}
