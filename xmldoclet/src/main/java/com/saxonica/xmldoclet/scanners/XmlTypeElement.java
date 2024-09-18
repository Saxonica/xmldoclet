package com.saxonica.xmldoclet.scanners;

import com.saxonica.xmldoclet.builder.XmlProcessor;
import com.saxonica.xmldoclet.utils.TypeUtils;
import com.sun.source.doctree.DocCommentTree;
import com.sun.source.doctree.DocTree;

import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.*;

public abstract class XmlTypeElement extends XmlScanner {
    private final TypeElement element;

    public XmlTypeElement(XmlProcessor xmlproc, TypeElement element) {
        super(xmlproc);
        this.element = element;
    }

    public abstract String typeName();

    public void scan(DocTree tree) {
        String s = element.getQualifiedName().toString();

        Map<String, String> attr = new HashMap<>();
        attr.put("fullname", element.getQualifiedName().toString());
        attr.put("package", element.getEnclosingElement().toString());
        attr.put("type", element.getSimpleName().toString());
        attr.put("nesting", element.getNestingKind().toString().toLowerCase());
        attr.putAll(modifierAttributes(element));

        //System.err.println(typeName() + " " + element.getSimpleName() + " :: " + element.getEnclosingElement());

        builder.startElement(typeName(), attr);

        if (element.getSuperclass() instanceof DeclaredType) {
            Implemented impl = new Implemented();
            updateImplemented(element, impl);
            showSuperclass(element, (DeclaredType) element.getSuperclass(), impl);
        }

        if (!element.getInterfaces().isEmpty()) {
            builder.startElement("interfaces");
            for (TypeMirror tm : element.getInterfaces()) {
                TypeUtils.xmlType(builder, "interfaceref", tm);
            }
            builder.endElement("interfaces");
        }

        if (!element.getTypeParameters().isEmpty()) {
            builder.startElement("typeparams");
            for (TypeParameterElement tp : element.getTypeParameters()) {
                attr.clear();
                attr.put("name", tp.toString());
                builder.startElement("typeparam", attr);
                for (TypeMirror bound : tp.getBounds()) {
                    TypeUtils.xmlType(builder, "type", bound);
                }
                builder.endElement("typeparam");
            }
            builder.endElement("typeparams");
        }

        if (tree instanceof DocCommentTree) {
            DocCommentTree dcTree = (DocCommentTree) tree;
            builder.processList(dcTree.getBlockTags());
            builder.processList("purpose", dcTree.getFirstSentence());
            builder.processList("description", dcTree.getBody());
        }

        builder.xmlscan(element.getEnclosedElements());

        builder.endElement(typeName());
    }

    private void showSuperclass(TypeElement element, DeclaredType superclass, Implemented impl) {
        if (element.getSuperclass() instanceof DeclaredType) {
            String name = ((DeclaredType) element.getSuperclass()).toString();
            if ("java.lang.Object".equals(name)) {
                return;
            }
        }

        builder.startElement("superclass");
        TypeUtils.xmlType(builder, "type", element.getSuperclass());

        List<? extends Element> enclosed = superclass.asElement().getEnclosedElements();

        List<Element> inherited = new ArrayList<>();
        for (Element elem : enclosed) {
            String name = elem.toString();
            if (!elem.getModifiers().contains(Modifier.PRIVATE)) {
                if (elem.getKind() == ElementKind.FIELD) {
                    if (!impl.fields.contains(name)) {
                        impl.fields.add(name);
                        inherited.add(elem);
                    }
                }
                if (elem.getKind() == ElementKind.METHOD) {
                    if (!impl.methods.contains(name)) {
                        impl.methods.add(name);
                        inherited.add(elem);
                    }
                }
            }
        }

        if (!inherited.isEmpty()) {
            builder.startElement("inherited");
            for (Element elem : inherited) {
                Map<String, String> amap = new HashMap<>();
                amap.put("name", elem.toString());
                if (elem.getKind() == ElementKind.FIELD) {
                    builder.startElement("field", amap);
                    builder.endElement("field");
                } else {
                    builder.startElement("method", amap);
                    builder.endElement("method");
                }
            }
            builder.endElement("inherited");
        }

        Element selem = superclass.asElement();
        if (selem instanceof TypeElement) {
            TypeElement setype = (TypeElement) selem;
            TypeMirror sstype = setype.getSuperclass();

            showInterfaces(setype, impl);

            if (sstype.getKind() == TypeKind.DECLARED) {
                showSuperclass(setype, (DeclaredType) sstype, impl);
            }

        }

        builder.endElement("superclass");
    }

    private void showInterfaces(TypeElement element, Implemented impl) {
        if (element.getInterfaces().isEmpty()) {
            return;
        }

        for (TypeMirror tm : element.getInterfaces()) {
            if (tm.getKind() == TypeKind.DECLARED) {
                TypeElement ttm = (TypeElement) ((DeclaredType) tm).asElement();
                Map<String, String> amap = new HashMap<>();
                amap.put("fullname", ttm.toString());
                amap.put("name", ttm.getSimpleName().toString());
                if (ttm.toString().contains(".")) {
                    amap.put("package", ttm.toString().substring(0, ttm.toString().lastIndexOf(".")));
                }

                builder.startElement("interface", amap);

                for (Element celem : ttm.getEnclosedElements()) {
                    if (celem.getKind() == ElementKind.FIELD) {
                        amap = new HashMap<>();
                        amap.put("name", celem.getSimpleName().toString());
                        builder.startElement("field", amap);
                        builder.endElement("field");
                    }
                    if (celem.getKind() == ElementKind.METHOD) {
                        if (!impl.methods.contains(celem.toString())) {
                            amap = new HashMap<>();
                            amap.put("name", celem.toString());
                            builder.startElement("method", amap);
                            builder.endElement("method");
                        }
                    }
                }

                showInterfaces(ttm, impl);
                builder.endElement("interface");
            }
        }
    }

    private void updateImplemented(TypeElement element, Implemented impl) {
        for (Element elem : element.getEnclosedElements()) {
            Set<Modifier> modifiers = elem.getModifiers();
            if (!(modifiers.contains(Modifier.PRIVATE))) {
                ElementKind kind = elem.getKind();
                if (elem.getKind() == ElementKind.FIELD) {
                    impl.fields.add(elem.toString());
                }
                if (elem.getKind() == ElementKind.METHOD) {
                    impl.methods.add(elem.toString());
                }
            }
        }
    }

    private static class Implemented {
        public final Set<String> fields = new HashSet<>();
        public final Set<String> methods = new HashSet<>();
    }
}
