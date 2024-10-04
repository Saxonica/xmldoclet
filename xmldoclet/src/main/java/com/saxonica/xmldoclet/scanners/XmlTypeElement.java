package com.saxonica.xmldoclet.scanners;

import com.saxonica.xmldoclet.builder.XmlProcessor;
import com.saxonica.xmldoclet.utils.TypeUtils;
import com.sun.source.doctree.DocCommentTree;
import com.sun.source.doctree.DocTree;
import com.sun.source.doctree.ParamTree;

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

        String pkgName = getPackage(element);
        String typeName = getType(element);

        Map<String, String> attr = new HashMap<>();
        attr.put("fullname", element.getQualifiedName().toString());
        attr.put("package", pkgName);
        attr.put("type", typeName);
        attr.put("nesting", element.getNestingKind().toString().toLowerCase());
        attr.putAll(modifierAttributes(element));

        //System.err.println(typeName() + " " + element.getSimpleName() + " :: " + element.getEnclosingElement());

        builder.startElement(typeName(), attr);

        Implemented impl = new Implemented();
        updateImplemented(element, impl);

        if (element.getSuperclass() instanceof DeclaredType) {
            showSuperclass(element, (DeclaredType) element.getSuperclass(), impl);
        }

        if (!element.getInterfaces().isEmpty()) {
            builder.startElement("interfaces");
            for (TypeMirror tm : element.getInterfaces()) {
                // Reset the implemented list each time
                Implemented classimpl = new Implemented(impl);
                updateImplemented(element, classimpl);
                showInterfaces(element, (DeclaredType) tm, impl);
            }
            builder.endElement("interfaces");
        }

        if (!element.getTypeParameters().isEmpty()) {
            builder.startElement("typeparams");
            for (TypeParameterElement tp : element.getTypeParameters()) {
                String tpname = tp.getSimpleName().toString();
                attr.clear();
                attr.put("name", tp.toString());
                builder.startElement("typeparam", attr);
                for (TypeMirror bound : tp.getBounds()) {
                    TypeUtils.xmlType(builder, "type", bound);
                }

                if (tree instanceof DocCommentTree) {
                    DocCommentTree dcTree = (DocCommentTree) tree;
                    for (DocTree tag : dcTree.getBlockTags()) {
                        if (tag instanceof ParamTree) {
                            String tname = ((ParamTree) tag).getName().toString();
                            if (tpname.equals(tname)) {
                                builder.processList("purpose", ((ParamTree) tag).getDescription());
                            }
                        }
                    }
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

    /**
     * Find the element's package.
     * <p>For nested classes, we may have to look up several times.</p>
     * @return the package name
     */
    private String getPackage(Element element) {
        Element enclosing = element.getEnclosingElement();

        if (enclosing == null) {
            return "";
        }

        if (enclosing instanceof PackageElement) {
            return enclosing.toString();
        }

        return getPackage(enclosing);
    }

    /**
     * Find the name of this type; that's our ancestor names if this is a nested class.
     * @param element The element
     * @return The type name
     */
    private String getType(Element element) {
        Element enclosing = element.getEnclosingElement();
        if (enclosing instanceof TypeElement) {
            String stype = getType(enclosing);
            if (!"".equals(stype)) {
                return stype + "." + element.getSimpleName().toString();
            }
            return element.getSimpleName().toString();
        }
        return element.getSimpleName().toString();
    }

    /**
     * Find the implemented interfaces
     * <p>This includes the interfaces of any classes we extend.</p>
     * @param element the starting element
     * @return list of interfaces
     */
    private List<TypeMirror> getInterfaces(TypeElement element) {
        List<TypeMirror> interfaces = new ArrayList<>(element.getInterfaces());

        TypeMirror superClass = element.getSuperclass();
        if (superClass instanceof DeclaredType) {
            Element superElem = ((DeclaredType) superClass).asElement();
            if (superElem instanceof TypeElement) {
                interfaces.addAll(getInterfaces((TypeElement) superElem));
            }
        }

        return interfaces;
    }

    private void showSuperclass(TypeElement element, DeclaredType superclass, Implemented impl) {
        if ("java.lang.Object".equals(superclass.toString())) {
            return;
        }
        /*
        if (element.getSuperclass() instanceof DeclaredType) {
            String name = ((DeclaredType) element.getSuperclass()).toString();
            if ("java.lang.Object".equals(name)) {
                return;
            }
        }
         */

        builder.startElement("superclass");
        TypeUtils.xmlType(builder, "type", superclass);

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

            // Show the interfaces of superclasses
            List<? extends TypeMirror> ifaces = ((TypeElement) selem).getInterfaces();
            if (!ifaces.isEmpty()) {
                builder.startElement("interfaces");
                for (TypeMirror tm : ifaces) {
                    // Reset the implemented list each time
                    Implemented classimpl = new Implemented(impl);
                    updateImplemented(element, classimpl);
                    showInterfaces(element, (DeclaredType) tm, impl);
                }
                builder.endElement("interfaces");
            }

            if (sstype.getKind() == TypeKind.DECLARED) {
                showSuperclass(setype, (DeclaredType) sstype, impl);
            }
        }

        builder.endElement("superclass");
    }

    private void showInterfaces(TypeElement element, DeclaredType xinter, Implemented impl) {
        Map<String, String> attr = new HashMap<>();
        attr.put("name", xinter.asElement().getSimpleName().toString());
        attr.put("fullname", xinter.asElement().toString());
        attr.put("package", xinter.asElement().getEnclosingElement().toString());
        builder.startElement("interface", attr);

        for (TypeMirror tm : xinter.getTypeArguments()) {
            attr.clear();
            TypeUtils.xmlType(builder, "param", tm);
        }

        List<? extends Element> enclosed = xinter.asElement().getEnclosedElements();

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

        Element selem = xinter.asElement();
        if (selem instanceof TypeElement) {
            TypeElement setype = (TypeElement) selem;
            TypeMirror sstype = setype.getSuperclass();

            if (!setype.getInterfaces().isEmpty()) {
                for (TypeMirror tm : setype.getInterfaces()) {
                    showInterfaces(element, (DeclaredType) tm, impl);
                }
            }
        }

        builder.endElement("interface");
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
        public Implemented() {}
        public Implemented(Implemented impl) {
            fields.addAll(impl.fields);
            methods.addAll(impl.methods);
        }
        public final Set<String> fields = new HashSet<>();
        public final Set<String> methods = new HashSet<>();
    }
}
