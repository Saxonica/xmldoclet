package com.saxonica.xmldoclet;

import com.saxonica.xmldoclet.builder.MarkupBuilder;
import com.sun.source.tree.Tree;

import javax.lang.model.type.*;
import java.util.HashMap;
import java.util.Map;

public class TypeUtils {
    public static void xmlType(XmlProcessor builder, String wrapper, TypeMirror mirror) {
        if (mirror == null) {
            return;
        }

        TypeKind kind = mirror.getKind();
        if (kind.isPrimitive() || kind == TypeKind.VOID) {
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
        builder.startElement(wrapper, attr);
        builder.endElement(wrapper);
    }

    private static void declaredType(XmlProcessor builder, String wrapper, DeclaredType dtype) {
        Map<String, String> attr = new HashMap<>();

        attr.put("name", dtype.asElement().getSimpleName().toString());
        attr.put("fullname", dtype.asElement().toString());
        attr.put("package", dtype.asElement().getEnclosingElement().toString());
        builder.startElement(wrapper, attr);

        for (TypeMirror tm : dtype.getTypeArguments()) {
            attr.clear();
            TypeUtils.xmlType(builder, "param", tm);
        }

        builder.endElement(wrapper);
    }

    private static void wildcardType(XmlProcessor builder, String wrapper, WildcardType wtype) {
        Map<String, String> attr = new HashMap<>();

        builder.startElement(wrapper);
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
