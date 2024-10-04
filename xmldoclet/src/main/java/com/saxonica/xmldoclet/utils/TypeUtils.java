package com.saxonica.xmldoclet.utils;

import com.saxonica.xmldoclet.builder.XmlProcessor;

import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.*;
import java.util.HashMap;
import java.util.Map;

public class TypeUtils {
    /**
     * Construct an XML representation of the type.
     * <p>Creates a type element on the builder.</p>
     * @param builder The processor
     * @param wrapper The name of the element to create for the type
     * @param mirror The type
     */
    public static void xmlType(XmlProcessor builder, String wrapper, TypeMirror mirror) {
        if (mirror == null) {
            return;
        }

        TypeKind kind = mirror.getKind();
        if (kind.isPrimitive() || kind == TypeKind.VOID || kind == TypeKind.TYPEVAR) {
            TypeUtils.primitiveType(builder, wrapper, mirror);
            return;
        }

        if (mirror instanceof DeclaredType) {
            TypeUtils.declaredType(builder, wrapper, (DeclaredType) mirror);
            return;
        }

        if (mirror instanceof WildcardType) {
            TypeUtils.wildcardType(builder, "wildcard", (WildcardType) mirror);
            return;
        }

        if (mirror instanceof ArrayType) {
            TypeUtils.arrayType(builder, "array", (ArrayType) mirror);
            return;
        }

        System.err.println("Unexpected xmlType: " + mirror);
    }

    private static void primitiveType(XmlProcessor builder, String wrapper, TypeMirror ptype) {
        Map<String, String> attr = new HashMap<>();
        attr.put("name", ptype.toString());
        if (ptype.getKind() == TypeKind.TYPEVAR) {
            attr.put("kind", "typevar");
        } else if (ptype.getKind() == TypeKind.VOID) {
            attr.put("kind", "void");
        } else {
            attr.put("kind", "primitive");
        }
        builder.startElement(wrapper, attr);
        builder.endElement(wrapper);
    }

    private static void declaredType(XmlProcessor builder, String wrapper, DeclaredType dtype) {
        Map<String, String> attr = new HashMap<>();

        attr.put("name", getType(dtype.asElement()));
        attr.put("fullname", dtype.asElement().toString());
        attr.put("package", getPackage(dtype.asElement()));
        builder.startElement(wrapper, attr);

        for (TypeMirror tm : dtype.getTypeArguments()) {
            attr.clear();
            TypeUtils.xmlType(builder, "param", tm);
        }

        builder.endElement(wrapper);
    }

    /**
     * Find the element's package.
     * <p>For nested classes, we may have to look up several times.</p>
     * @param element The starting element
     * @return the package name
     */
    public static String getPackage(Element element) {
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
     * @param element The starting Welement
     * @return The type name
     */
    public static String getType(Element element) {
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

    private static void wildcardType(XmlProcessor builder, String wrapper, WildcardType wtype) {
        Map<String, String> attr = new HashMap<>();

        attr.put("signature", wtype.toString());

        builder.startElement(wrapper, attr);
        TypeUtils.xmlType(builder, "extends", wtype.getExtendsBound());
        TypeUtils.xmlType(builder, "super", wtype.getSuperBound());
        builder.endElement(wrapper);
    }

    private static void arrayType(XmlProcessor builder, String wrapper, ArrayType atype) {
        Map<String, String> attr = new HashMap<>();

        builder.startElement(wrapper);
        TypeUtils.xmlType(builder, "component", atype.getComponentType());
        builder.endElement(wrapper);
    }

}
