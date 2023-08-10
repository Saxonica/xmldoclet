package com.saxonica.xmldoclet;

import com.saxonica.xmldoclet.builder.HtmlBuilder;
import com.saxonica.xmldoclet.builder.XmlBuilder;
import com.saxonica.xmldoclet.scanners.*;
import com.sun.source.doctree.DocCommentTree;
import com.sun.source.doctree.DocTree;
import com.sun.source.doctree.ReferenceTree;
import jdk.javadoc.doclet.DocletEnvironment;
import net.sf.saxon.s9api.*;
import nu.validator.htmlparser.common.XmlViolationPolicy;
import nu.validator.htmlparser.dom.HtmlDocumentBuilder;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.lang.model.element.*;
import javax.lang.model.util.ElementScanner9;
import javax.tools.JavaFileObject;
import javax.xml.transform.dom.DOMSource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class XmlProcessor extends ElementScanner9<Void, XmlProcessor> {
    public final DocletEnvironment environment;
    final String destinationDir;
    final String outputFilename;
    private final OutputManager manager;
    private final Processor processor;
    private final Stack<Element> elementStack;
    private XmlBuilder builder = null;
    private final Map<String,PackageElement> packageMap;

    XmlProcessor(DocletEnvironment environment, String destinationDir, String outputFilename) {
        processor = new Processor(false);
        this.environment = environment;
        this.destinationDir = destinationDir;
        this.outputFilename = outputFilename;
        this.manager = new OutputManager(destinationDir, outputFilename);
        this.elementStack = new Stack<>();

        packageMap = new HashMap<>();
        for (ModuleElement mod : environment.getElementUtils().getAllModuleElements()) {
            for (Element elem : mod.getEnclosedElements()) {
                if (elem instanceof PackageElement) {
                    packageMap.put(elem.toString(), (PackageElement) elem);
                }
            }
        }
        }

    // ================================================
    // Emulate the builder

    public void startDocument() {
        builder.startDocument();
    }

    public void endDocument() {
        builder.endDocument();
    }

    public void startElement(String name) {
        builder.startElement(name);
    }

    public void startElement(String name, Map<String,String> attributes) {
        builder.startElement(name, attributes);
    }

    public void endElement(String name) {
        builder.endElement(name);
    }

    public void comment(String text) {
        builder.comment(text);
    }

    public void processingInstruction(String target, String data) {
        builder.processingInstruction(target, data);
    }

    public void text(String text) {
        builder.text(text);
    }

    // ================================================
    // HTML methods

    public void html(DocTree element) {
        html(null, element);
    }

    public void html(String wrapper, DocTree element) {
        html(wrapper, Collections.singletonList(element));
    }

    public void html(List<? extends DocTree> elements) {
        html(null, elements);
    }

    public void html(String wrapper, List<? extends DocTree> elements) {
        if (elements.isEmpty()) {
            return;
        }

        HtmlBuilder miniBuilder = new HtmlBuilder(this);
        miniBuilder.startDocument();
        miniBuilder.startElement("body");
        miniBuilder.processList(elements);
        miniBuilder.endElement("body");
        miniBuilder.endDocument();
        String miniXml = miniBuilder.getXml();

        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(miniXml.getBytes(StandardCharsets.UTF_8));
            HtmlDocumentBuilder htmlBuilder = new HtmlDocumentBuilder(XmlViolationPolicy.ALTER_INFOSET);
            Document html = htmlBuilder.parse(bais);
            DocumentBuilder xbuilder = processor.newDocumentBuilder();
            XdmNode doc = xbuilder.build(new DOMSource(html));

            String xml = doc.toString();
            if (xml.contains("<body/>")) {
                // nevermind
                return;
            }

            XPathCompiler compiler = processor.newXPathCompiler();
            XPathExecutable exec = compiler.compile("/*/*:body");
            XPathSelector selector = exec.load();
            selector.setContextItem(doc);
            XdmValue selection = selector.evaluate();

            if (selection instanceof XdmNode) {
                if (wrapper != null) {
                    builder.startElement(wrapper);
                }
                builder.addSubtree((XdmNode) selection);
                if (wrapper != null) {
                    builder.endElement(wrapper);
                }
            }
        } catch (SAXException | SaxonApiException | IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    // ================================================

    public void processList(List<? extends DocTree> elements) {
        builder.processList(elements);
    }

    // ================================================

    void scan() {
        Set<? extends Element> elements = environment.getIncludedElements();

        builder = new XmlBuilder(this, processor);
        builder.startDocument();
        for (Element element : elements) {
            xmlscan(element);
        }
        builder.endDocument();

        manager.beginOutput();
        manager.output(builder.getXml());
        manager.endOutput();
    }

    public void xmlscan(List<? extends Element> elements) {
        for (Element element : elements) {
            xmlscan(element);
        }
    }

    public void xmlscan(Element element) {
        elementStack.push(element);

        final XmlScanner scanner;
        switch (element.getKind()) {
            case PACKAGE:
                System.err.println("Loading " + element);
                scanner = new XmlPackage(this, (PackageElement) element);
                break;
            case CLASS:
                System.err.println("Loading " + element);
                scanner = new XmlClass(this, (TypeElement) element);
                break;
            case INTERFACE:
                System.err.println("Loading " + element);
                scanner = new XmlInterface(this, (TypeElement) element);
                break;
            case ENUM:
                scanner = new XmlEnum(this, (TypeElement) element);
                break;
            case ENUM_CONSTANT:
                scanner = new XmlEnumConstant(this, (VariableElement) element);
                break;
            case CONSTRUCTOR:
                scanner = new XmlConstructor(this, (ExecutableElement) element);
                break;
            case METHOD:
                scanner = new XmlMethod(this, (ExecutableElement) element);
                break;
            case PARAMETER:
                scanner = new XmlParameter(this, (VariableElement) element);
                break;
            case FIELD:
                scanner = new XmlField(this, (VariableElement) element);
                break;
            case ANNOTATION_TYPE:
                scanner = new XmlAnnotation(this, element);
                break;
            case MODULE:
                System.err.println("Loading " + element);
                scanner = new XmlModule(this, (ModuleElement) element);
                break;
            default:
                System.err.printf("Unknown element: %s%n", element.getKind());
                scanner = null;
                break;
        }

        if (scanner != null) {
            DocCommentTree dcTree = environment.getDocTrees().getDocCommentTree(element);
            scanner.scan(dcTree);
        }

        elementStack.pop();
    }

    public ResolvedReference resolveReference(ReferenceTree ref) {
        String rpackage = null;
        String rclass = null;
        String rmethod = null;
        String rfield = null;

        String signature = ref.getSignature();

        int pos = signature.indexOf("#");
        if (pos >= 0) {
            String fragid = signature.substring(pos+1);
            if (fragid.contains("(")) {
                rmethod = fragid;
            } else {
                rfield = fragid;
            }
            signature = signature.substring(0, pos);
        }

        TypeElement classelem = null;
        pos = signature.lastIndexOf(".");
        if (pos >= 0) {
            rpackage = signature.substring(0, pos);
            rclass = signature.substring(pos+1);

            PackageElement cpkg = packageMap.getOrDefault(rpackage, null);
            if (cpkg != null) {
                for (Element elem : cpkg.getEnclosedElements()) {
                    if (elem.getKind() == ElementKind.CLASS || elem.getKind() == ElementKind.INTERFACE) {
                        if (rclass.equals(elem.getSimpleName().toString())) {
                            classelem = (TypeElement) elem;
                            break;
                        }
                    }
                }
            }
        } else {
            rclass = signature;
        }

        if (rclass.isEmpty()) {
            rclass = find("");
            classelem = findClass("");
        }

        if (rpackage == null) {
            rpackage = find(rclass);
            classelem = findClass(rclass);
        }

        PackageElement pkgelem = packageMap.get(rpackage);

        // For some reason the ReferenceTree doesn't expose the parameter types, even though it has them.
        // Worse still, it's really a pain to find what they refer to. This is a bit hacky/heuristic.
        final ResolvedReference resolution;
        if (rmethod != null) {
            pos = rmethod.indexOf("(");
            String arglist = rmethod.substring(pos+1, rmethod.length()-1);
            rmethod = rmethod.substring(0, pos);

            resolution = new ResolvedReference(signature, rpackage, rclass, rmethod, rfield);
            if (!arglist.isEmpty()) {
                String[] args = arglist.split(",\\s*");
                for (String arg : args) {
                    String aclass = find(arg);
                    if (aclass == null && pkgelem != null) {
                        aclass = findInPackage(pkgelem, arg);
                    }
                    if (aclass == null) {
                        aclass = findInAllPackages(arg);
                    }
                    resolution.addArg(arg, aclass);
                }
            }
        } else {
            resolution = new ResolvedReference(signature, rpackage, rclass, rmethod, rfield);
        }

        return resolution;
    }

    private TypeElement findClass(String className) {
        for (int pos = elementStack.size()-1; pos >= 0; pos--) {
            Element elem = elementStack.get(pos);
            if (elem.getKind() == ElementKind.CLASS || elem.getKind() == ElementKind.INTERFACE) {
                TypeElement t = (TypeElement) elem;
                Element enc = t.getEnclosingElement();
                if (className.isEmpty()) {
                    return t;
                }
                if (className.equals(t.getSimpleName().toString())) {
                    return t;
                }
                for (Element child : elem.getEnclosedElements()) {
                    if (child.getKind() == ElementKind.CLASS || child.getKind() == ElementKind.INTERFACE) {
                        if (className.equals(child.getSimpleName().toString())) {
                            return (TypeElement) elem;
                        }
                    }
                }
                if (enc != null && enc.getKind() == ElementKind.PACKAGE) {
                    return findClassInPackage((PackageElement) enc, className);
                }
            }
        }
        return null;
    }

    private String find(String className) {
        String fpackage = null;
        for (int pos = elementStack.size()-1; pos >= 0; pos--) {
            Element elem = elementStack.get(pos);
            if (elem.getKind() == ElementKind.CLASS|| elem.getKind() == ElementKind.INTERFACE) {
                TypeElement t = (TypeElement) elem;
                Element enc = t.getEnclosingElement();
                if (className.isEmpty()) {
                    return elem.getSimpleName().toString();
                }
                if (className.equals(t.getSimpleName().toString())) {
                    return enc.toString();
                }
                for (Element child : elem.getEnclosedElements()) {
                    if (child.getKind() == ElementKind.CLASS || child.getKind() == ElementKind.INTERFACE) {
                        if (className.equals(child.getSimpleName().toString())) {
                            return elem.toString();
                        }
                    }
                }
                if (fpackage == null && enc != null && enc.getKind() == ElementKind.PACKAGE) {
                    fpackage = findInPackage((PackageElement) enc, className);
                }
            }
        }
        return fpackage;
    }

    private String findInPackage(PackageElement pkg, String className) {
        for (Element enc : pkg.getEnclosedElements()) {
            if (enc.getKind() == ElementKind.CLASS || enc.getKind() == ElementKind.INTERFACE) {
                TypeElement t = (TypeElement) enc;
                if (className.equals(t.getSimpleName().toString())) {
                    return enc.toString();
                }
            }
        }
        return null;
    }

    private TypeElement findClassInPackage(PackageElement pkg, String className) {
        for (Element enc : pkg.getEnclosedElements()) {
            if (enc.getKind() == ElementKind.CLASS || enc.getKind() == ElementKind.INTERFACE) {
                TypeElement t = (TypeElement) enc;
                if (className.equals(t.getSimpleName().toString())) {
                    return t;
                }
            }
        }
        return null;
    }

    // This is a total hack
    private String findInAllPackages(String className) {
        String resolved = null;
        for (PackageElement pkg : packageMap.values()) {
            // and so is this
            if (pkg.toString().startsWith("java.") || pkg.toString().startsWith("javax.")) {
                for (Element enc : pkg.getEnclosedElements()) {
                    if (enc.getKind() == ElementKind.CLASS || enc.getKind() == ElementKind.INTERFACE) {
                        TypeElement t = (TypeElement) enc;
                        if (className.equals(t.getSimpleName().toString())) {
                            if (resolved != null) {
                                System.err.println("DUP: " + enc.toString());
                            }
                            resolved = enc.toString();
                        }
                    }
                }
            }
        }

        return resolved;
    }


}
