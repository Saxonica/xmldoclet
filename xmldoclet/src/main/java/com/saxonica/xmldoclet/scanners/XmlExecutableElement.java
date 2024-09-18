package com.saxonica.xmldoclet.scanners;

import com.saxonica.xmldoclet.utils.TypeUtils;
import com.saxonica.xmldoclet.builder.XmlProcessor;
import com.sun.source.doctree.*;

import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.*;

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
            attr.put("name", element.toString());
        }

        Map<String,DeclaredType> thrownTypes = new HashMap<>();
        for (TypeMirror ttype : element.getThrownTypes()) {
            Element telem = ((DeclaredType) ttype).asElement();
            // N.B. I'd like to use the fully qualified name here (it's what toString() returns),
            // but I can't work out how to get the fully qualified name from the ThrowsTree.
            // Note that we try to be careful in the matching code below.
            thrownTypes.put(telem.getSimpleName().toString(), (DeclaredType) ttype);
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

                        if (block.getKind() == DocTree.Kind.THROWS) {
                            String name = ((ThrowsTree) block).getExceptionName().getSignature();
                            // The name could be either com.example.Exception or just Exception.
                            // If either of those occurs in thrownTypes, we want to remove it.
                            String removeKey = null;
                            for (String key : thrownTypes.keySet()) {
                                if (name.equals(key) || name.equals(thrownTypes.get(key).toString())) {
                                    removeKey = key;
                                }
                            }
                            if (removeKey != null) {
                                thrownTypes.remove(removeKey);
                            }
                        }
                        break;
                }
            }

            // Were there some block comments, but also some undocumented exceptions?
            if (!thrownTypes.isEmpty()) {
                for (String name : thrownTypes.keySet()) {
                    Map<String,String> tattr = new HashMap<>();
                    tattr.put("exception", thrownTypes.get(name).toString());
                    builder.startElement("throws", tattr);
                    builder.endElement("throws");
                }
                thrownTypes.clear();
            }

            builder.processList("purpose", dcTree.getFirstSentence());
            builder.processList("description", dcTree.getBody());
        }

        // Are there undocumented exceptions (but also there were no block comments at all)?
        if (!thrownTypes.isEmpty()) {
            for (String name : thrownTypes.keySet()) {
                Map<String,String> tattr = new HashMap<>();
                tattr.put("exception", name);
                builder.startElement("throws", tattr);
                builder.endElement("throws");
            }
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
