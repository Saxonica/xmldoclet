package com.saxonica.xmldoclet.scanners;

import com.saxonica.xmldoclet.XmlProcessor;
import com.sun.source.doctree.DocTree;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import java.util.HashMap;
import java.util.Map;

public abstract class XmlScanner {
    protected final XmlProcessor builder;

    public XmlScanner(XmlProcessor xmlproc) {
        this.builder = xmlproc;
    }

    public abstract void scan(DocTree tree);

    public Map<String,String> modifierAttributes(Element element) {
        Map<String,String> attr = new HashMap<>();
        for (Modifier modifier : element.getModifiers()) {
            switch (modifier) {
                case PUBLIC:
                case PROTECTED:
                case PRIVATE:
                    attr.put("access", modifier.toString());
                    break;
                default:
                    attr.put(modifier.toString(), "true");
                    break;
            }
        }
        return attr;
    }
}
