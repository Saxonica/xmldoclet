package com.saxonica.xmldoclet.scanners;

import com.saxonica.xmldoclet.utils.TypeUtils;
import com.saxonica.xmldoclet.builder.XmlProcessor;
import com.sun.source.doctree.*;

import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class XmlExecutableElement extends XmlScanner {
    private final ExecutableElement element;

    public XmlExecutableElement(XmlProcessor xmlproc, ExecutableElement element) {
        super(xmlproc);
        this.element = element;
    }

    public abstract String typeName();

    public void scan(DocTree tree) {
        Map<String,String> attr = new HashMap<>();

        // Hack
        if (!"constructor".equals(typeName())) {
            attr.put("name", element.getSimpleName().toString());
        }

        StringBuilder fullparam = new StringBuilder();
        StringBuilder fullsig = new StringBuilder();
        fullsig.append(element.getEnclosingElement().asType().toString());
        fullsig.append("#");
        if ("constructor".equals(typeName())) {
            fullsig.append("<init>");
        } else {
            fullsig.append(element.getSimpleName().toString());
        }
        fullsig.append("(");
        int count = 1;
        for (VariableElement velem: element.getParameters()) {
            if (count > 1) {
                fullparam.append(",");
            }
            fullparam.append(velem.asType().toString());
            count++;
        }
        fullsig.append(fullparam.toString());
        fullsig.append(")");

        attr.putAll(modifierAttributes(element));
        attr.put("fullsig", fullsig.toString());

        builder.startElement(typeName(), attr);

        ReturnTree returns = null;
        if (tree instanceof DocCommentTree) {
            DocCommentTree dcTree = (DocCommentTree) tree;
            for (DocTree block : dcTree.getBlockTags()) {
                switch (block.getKind()) {
                    case PARAM:
                        // Don't process the param tags here, we'll do them below
                        break;
                    case RETURN:
                        // Don't process the return tag here, we'll do it below
                        returns = (ReturnTree) block;
                        break;
                    default:
                        builder.processTree(block);
                        break;
                }
            }
            builder.processList("purpose", dcTree.getFirstSentence());
            builder.processList("description", dcTree.getBody());
        }

        builder.xmlscan(element.getParameters());

        if (!"constructor".equals(typeName()) && element.getReturnType() != null) {
            builder.startElement("return");
            if (returns != null) {
                builder.processList("purpose", returns.getDescription());
            }
            TypeUtils.xmlType(builder, "type", element.getReturnType());
            builder.endElement("return");

            if (builder.currentClass() != null) {
                ExecutableElement superex = findSuper(builder.currentClass().getSuperclass());
                if (superex != null) {
                    String oclass = ((TypeElement) superex.getEnclosingElement()).getQualifiedName().toString();
                    StringBuilder sb = new StringBuilder();
                    StringBuilder ssb = new StringBuilder();
                    count = 1;
                    for (VariableElement param : element.getParameters()) {
                        if (count > 1) {
                            sb.append(",");
                            ssb.append(",");
                        }
                        PackageElement curpkg = builder.currentPackage();
                        String fqn = builder.resolveClass(builder.currentPackage(), builder.currentClass(), param.asType().toString());
                        sb.append(fqn);
                        if (fqn != null) {
                            int pos = fqn.lastIndexOf(".");
                            ssb.append(fqn.substring(pos+1));
                        } else {
                            ssb.append(fqn);
                        }
                        count++;
                    }
                    attr.clear();
                    attr.put("method", oclass + "#" + element.getSimpleName() + "(" + sb + ")");
                    builder.startElement("overrides", attr);
                    builder.text((superex.getEnclosingElement()).getSimpleName().toString());
                    builder.text("#");
                    builder.text(element.getSimpleName().toString());
                    builder.text("(");
                    builder.text(ssb.toString());
                    builder.text(")");
                    builder.endElement("overrides");
                }

                superex = findImplements(builder.currentClass().getInterfaces());
                if (superex != null) {
                    attr.clear();
                    String iimpl = ((TypeElement) superex.getEnclosingElement()).getQualifiedName().toString();
                    iimpl += "#" + element.getSimpleName().toString();
                    iimpl += "(" + fullparam.toString() + ")";
                    attr.put("interface", iimpl);
                    builder.startElement("implements", attr);
                    builder.endElement("implements");
                }

            }
        }

        builder.endElement(typeName());
    }

    private ExecutableElement findSuper(TypeMirror supercls) {
        if ("java.lang.Object".equals(supercls.toString())) {
            return null; // not interesting
        }

        if (!(supercls instanceof DeclaredType)) {
            return null; // wat ???
        }

        DeclaredType declType = (DeclaredType) supercls;
        TypeElement decl = (TypeElement) declType.asElement();
        for (Element elem : decl.getEnclosedElements()) {
            if (elem.getKind() == ElementKind.METHOD) {
                ExecutableElement ex = (ExecutableElement) elem;
                if (element.getSimpleName().equals(ex.getSimpleName())
                        && element.getParameters().size() == ex.getParameters().size()) {
                    boolean same = true;
                    for (int index = 0; index < element.getParameters().size(); index++) {
                        VariableElement p1 = element.getParameters().get(index);
                        VariableElement p2 = ex.getParameters().get(index);
                        if (!sameType(p1.asType(), p2.asType())) {
                            same = false;
                            break;
                        }
                    }
                    if (same) {
                        return ex;
                    }
                }
            }
        }

        return findSuper(decl.getSuperclass());
    }

    private ExecutableElement findImplements(List<? extends TypeMirror> ifaces) {
        for (TypeMirror iface : ifaces) {
            if (iface instanceof DeclaredType) {
                DeclaredType declType = (DeclaredType) iface;
                TypeElement decl = (TypeElement) declType.asElement();
                for (Element elem : decl.getEnclosedElements()) {
                    if (elem.getKind() == ElementKind.METHOD) {
                        ExecutableElement ex = (ExecutableElement) elem;
                        if (element.getSimpleName().equals(ex.getSimpleName())) {
                            return ex;
                        }
                    }
                }
                ExecutableElement eex = findSuper(decl.getSuperclass());
                if (eex != null) {
                    return eex;
                }
            }
        }

        return null;
    }

    private boolean sameType(TypeMirror t1, TypeMirror t2) {
        return t1.equals(t2);
    }

}
