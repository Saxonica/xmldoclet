package com.saxonica.xmldoclet.builder;

import com.saxonica.xmldoclet.XmlProcessor;

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
}
