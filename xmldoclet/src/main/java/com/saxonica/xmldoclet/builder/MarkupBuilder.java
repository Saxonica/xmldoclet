package com.saxonica.xmldoclet.builder;

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

    public abstract void processTree(DocTree tree);

    protected void handleRef(String wrapper, ReferenceTree ref, List<? extends DocTree> label) {
        ResolvedReference resolved = xmlProcessor.resolveReference(ref);
        Map<String,String> typeAttr = new HashMap<>();

        StringBuilder href = new StringBuilder();
        if (label == null || label.isEmpty()) {
            typeAttr.put("class", "ref");
        } else {
            typeAttr.put("class", "ref label");
        }
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

        if (label == null || label.isEmpty()) {
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

    protected Map<String,String> list2map(String... attr) {
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
