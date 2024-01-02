package com.saxonica.xmldoclet.builder;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.saxonica.xmldoclet.BuildConfig;
import com.saxonica.xmldoclet.scanners.*;
import com.sun.source.doctree.DocCommentTree;
import com.sun.source.doctree.DocTree;
import com.sun.source.doctree.ReferenceTree;
import jdk.javadoc.doclet.DocletEnvironment;
import net.sf.saxon.s9api.*;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementScanner9;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

public class XmlProcessor extends ElementScanner9<Void, XmlProcessor> {
    private static final String ROOT = "doclet";
    private static final String NS = "https://saxonica.com/ns/doclet";

    public final DocletEnvironment environment;
    public final Processor processor;
    private final Stack<Element> elementStack;
    private final String destinationDir;
    private final String outputFilename;
    private XmlBuilder builder = null;
    private final List<String> sourcepath;
    private final Map<String, TypeElement> classMap;
    private final Map<TypeElement,List<ImportDeclaration>> importDeclarations;
    private final Map<String,PackageElement> packageMap;

    public XmlProcessor(DocletEnvironment environment, List<String> sourcepath, String destinationDir, String outputFilename) {
        processor = new Processor(false);
        this.environment = environment;
        this.destinationDir = destinationDir;

        if (outputFilename == null) {
            this.outputFilename = "doclet.xml";
        } else {
            this.outputFilename = outputFilename;
        }

        this.elementStack = new Stack<>();
        this.sourcepath = new ArrayList<>(sourcepath);
        this.importDeclarations = new HashMap<>();
        this.classMap = new HashMap<>();
        packageMap = new HashMap<>();
        System.err.printf("Loading packages from the environment%n");
        int count = 0;
        for (ModuleElement mod : environment.getElementUtils().getAllModuleElements()) {
            for (Element elem : mod.getEnclosedElements()) {
                if (elem instanceof PackageElement) {
                    packageMap.put(elem.toString(), (PackageElement) elem);
                    count++;
                }
            }
        }
        System.err.printf("Loaded %d packages from the environment%n", count);
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

    public void processList(List<? extends DocTree> tree) {
        builder.processList(tree);
    }

    public void processList(String wrapper, List<? extends DocTree> elements) {
        if (elements.isEmpty()) {
            return;
        }
        builder.startElement(wrapper);
        builder.html(elements);
        builder.endElement(wrapper);
    }

    public void processTree(DocTree tree) {
        builder.processTree(tree);
    }

    // ================================================

    public void scan() {
        Set<? extends Element> elements = environment.getIncludedElements();

        System.err.println("Parsing Java sources...");
        int count = 0;

        // Parse the sources to find the import declarations...
        for (Element elem : elements) {
            if (elem.getKind() == ElementKind.PACKAGE) {
                PackageElement pkg = (PackageElement) elem;
                for (Element child : pkg.getEnclosedElements()) {
                    if (child.getKind() == ElementKind.CLASS || child.getKind() == ElementKind.INTERFACE) {
                        TypeElement klass = (TypeElement) child;
                        classMap.put(klass.getQualifiedName().toString(), klass);
                        // Java knows what the sourcefile for klass is, but it won't tell me. :-(
                        File source = null;
                        try {
                            String javaFile = klass.toString().replace(".", "/");
                            javaFile += ".java";
                            for (String dir : sourcepath) {
                                source = new File(dir + "/" + javaFile);
                                if (source.exists()) {
                                    CompilationUnit compilationUnit = StaticJavaParser.parse(source);
                                    importDeclarations.put(klass, compilationUnit.getImports());
                                    count++;
                                    break;
                                }
                            }
                        } catch (Exception ex) {
                            System.err.println("Failed to parse source: " + source);
                        }
                    }
                }
            }
        }

        System.err.printf("Parsed %d sources%n", count);

        builder = new XmlBuilder(this);
        startDocument();
        for (Element element : elements) {
            if (element.getKind() == ElementKind.CLASS) {
                TypeElement xelem = (TypeElement) element;
                if (xelem.getNestingKind() == NestingKind.TOP_LEVEL) {
                    // Don't output nested, inner classes at the top level. They'll automatically
                    // be output in the surrounding class
                    xmlscan(element);
                }
            } else {
                xmlscan(element);
            }
        }
        endDocument();

        String path = destinationDir == null ? "." : destinationDir;
        if (outputFilename != null) {
            try {
                File outputFile = new File(path + "/" + outputFilename);
                File outputDir = outputFile.getParentFile();
                outputDir.mkdirs();
                PrintStream outputStream = new PrintStream(outputFile);
                outputStream.printf("<%s xmlns='%s'>%n", ROOT, NS);
                outputStream.printf("<admin created='%s' version='%s' schemaVersion='%s' hash='%s'/>%n",
                        BuildConfig.PUB_DATE, BuildConfig.VERSION, BuildConfig.SCHEMA_VERSION,
                        BuildConfig.PUB_HASH);
                outputStream.println(builder.getXml());
                outputStream.printf("</%s>%n", ROOT);
                outputStream.close();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
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
                scanner = new XmlPackage(this, (PackageElement) element);
                break;
            case CLASS:
                //System.err.println("Scanning: " + element);
                scanner = new XmlClass(this, (TypeElement) element);
                break;
            case INTERFACE:
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

        pos = signature.lastIndexOf(".");
        if (pos >= 0) {
            rpackage = signature.substring(0, pos);
            rclass = signature.substring(pos+1);
        } else {
            rclass = signature;
        }

        if (rclass.isEmpty()) {
            // This is a #foo reference to something in the current class...
            TypeElement current = currentClass();
            rclass = current == null ? null : current.getSimpleName().toString();
        }

        // If rclass is a simple name, find it in the imports
        if (rclass != null && !rclass.contains(".")) {
            if (currentPackage() != null && currentClass() != null) {
                String fqn = resolveClass(currentPackage(), currentClass(), rclass);
                if (fqn != null) {
                    pos = fqn.lastIndexOf(".");
                    rpackage = fqn.substring(0, pos);
                    rclass = fqn.substring(pos+1);
                }
            }
        }

        if (rpackage == null) {
            PackageElement current = currentPackage();
            rpackage = current == null ? null : current.getQualifiedName().toString();
            String fqn = currentPackage() + "." + currentClass();
            if (classMap.containsKey(fqn)) {
                fqn = findClassInSources(currentClass(), rclass);
                if (fqn != null) {
                    pos = fqn.lastIndexOf(".");
                    if (pos < 0) {
                        rpackage = "";
                        rclass = fqn;
                    } else {
                        rpackage = fqn.substring(0, pos);
                        rclass = fqn.substring(pos+1);
                    }
                }
            }
        }

        if (rmethod == null) {
            return new ResolvedReference(ref.getSignature(), rpackage, rclass, rmethod, rfield);
        }

        pos = rmethod.indexOf("(");
        String argstr = rmethod.substring(pos+1, rmethod.length()-1);
        rmethod = rmethod.substring(0, pos);
        String[] args = argstr.trim().isEmpty() ? new String[0] : argstr.split(",\\s*");

        ResolvedReference resolved = new ResolvedReference(ref.getSignature(), rpackage, rclass, rmethod, rfield);
        for (String arg : args) {
            PackageElement curpkg = currentPackage();
            TypeElement curcls = currentClass();

            final String fqn;
            if (curpkg != null && curcls != null) {
                fqn = resolveClass(curpkg, curcls, arg);
            } else {
                fqn = null;
            }

            //System.err.printf("%s => %s (%s, %s, %s)%n", arg, fqn, currentPackage(), currentClass(), classMap.containsKey(currentClass()));
            resolved.addArg(arg, fqn);
        }

        return resolved;
    }

    public String resolveClass(PackageElement currentPackage, TypeElement currentClass, String signature) {
        return resolveClass(currentPackage.getQualifiedName().toString(), currentClass.getQualifiedName().toString(), signature);
    }

    public String resolveClass(String currentPackage, String currentClass, String signature) {
        String rpackage = null;
        String rclass = null;

        int pos = signature.lastIndexOf(".");
        if (pos >= 0) {
            rpackage = signature.substring(0, pos);
            rclass = signature.substring(pos+1);
        } else {
            rclass = signature;
        }

        if (rclass.isEmpty()) {
            rclass = currentClass;
        }

        if (rpackage == null) {
            rpackage = currentPackage;
        } else {
            // We got a fully qualified signature to start with
            return signature;
        }

        //System.err.printf(":: %s%n", rclass);
        String fqn = null;
        if (rclass.contains(".")) {
            // If it's a qualified name, assume it's fully qualified.
            fqn = rclass;
        } else if ("byte".equals(rclass) || "short".equals(rclass) || "int".equals(rclass) || "long".equals(rclass)
                || "float".equals(rclass) || "double".equals(rclass) || "boolean".equals(rclass) || "char".equals(rclass)) {
            fqn = rclass;
        } else if (classMap.containsKey(currentClass)) {
            fqn = findClassInSources(currentClass(), rclass);
        } else if (packageMap.containsKey(currentPackage)) {
            fqn = findClassInPackage(currentPackage, rclass);
        }

        return fqn;
    }

    private String findClassInSources(TypeElement currentClass, String rclass) {
        String fqn = null;
        TypeElement classElem = classMap.get(currentClass.getQualifiedName().toString());
        for (ImportDeclaration decl : importDeclarations.get(classElem)) {
            if (decl.isAsterisk()) {
                //System.err.printf("\tC: %s.*%n", decl.getNameAsString());
                fqn = findClassInPackage(decl.getNameAsString(), rclass);
                if (fqn != null) {
                    break;
                }
            } else {
                //System.err.printf("\tC: %s%n", decl.getName());
                if (rclass.equals(decl.getName().getId())) {
                    fqn = decl.getNameAsString();
                    break;
                }
            }
        }

        if (fqn == null) {
            PackageElement pkg = (PackageElement) currentClass.getEnclosingElement();
            fqn = findClassInPackage(pkg.getQualifiedName().toString(), rclass);
        }

        if (fqn == null) {
            fqn = findClassInPackage("java.lang", rclass);
        }

        //System.err.printf("\t\t::%s%n", fqn);
        return fqn;
    }

    private String findClassInPackage(String currentPackage, String rclass) {
        String fqn = null;
        if (packageMap.containsKey(currentPackage)) {
            PackageElement pkg = packageMap.get(currentPackage);
            //System.err.printf("\tP: %s%n", pkg.toString());
            for (Element child : pkg.getEnclosedElements()) {
                if (child.getKind() == ElementKind.CLASS || child.getKind() == ElementKind.INTERFACE) {
                    TypeElement tclass = (TypeElement) child;
                    if (rclass.equals(tclass.getSimpleName().toString())) {
                        fqn = tclass.getQualifiedName().toString();
                        break;
                    }
                }
            }
        } else {
            //System.err.printf("\tP: %s -- not found%n", currentPackage);
        }

        //System.err.printf("\t\t::%s%n", fqn);
        return fqn;
    }

    public TypeElement currentClass() {
        for (int pos = elementStack.size()-1; pos >= 0; pos--) {
            Element elem = elementStack.get(pos);
            if (elem.getKind() == ElementKind.CLASS || elem.getKind() == ElementKind.INTERFACE) {
                return (TypeElement) elem;
            }
        }
        return null;
    }

    public VariableElement currentField() {
        for (int pos = elementStack.size()-1; pos >= 0; pos--) {
            Element elem = elementStack.get(pos);
            if (elem.getKind() == ElementKind.FIELD) {
                return (VariableElement) elem;
            }
        }
        return null;
    }

    public PackageElement currentPackage() {
        for (int pos = elementStack.size()-1; pos >= 0; pos--) {
            Element elem = elementStack.get(pos);
            if (elem.getKind() == ElementKind.CLASS || elem.getKind() == ElementKind.INTERFACE) {
                Element pkg = elem.getEnclosingElement();
                // What about modules?
                while (pkg != null) {
                    if (pkg.getKind() == ElementKind.PACKAGE) {
                        return (PackageElement) pkg;
                    }
                    pkg = pkg.getEnclosingElement();
                }
            }
        }
        return null;
    }

    private TypeMirror findClass(TypeElement currentClass, String className) {
        String fqn = null;
        TypeElement classElem = classMap.get(currentClass.getQualifiedName().toString());
        if (classElem == null) {
            System.err.printf("No sources for %s%n", currentClass.getQualifiedName().toString());
            return null;
        }
        for (ImportDeclaration decl : importDeclarations.get(classElem)) {
            if (decl.isAsterisk()) {
                //System.err.printf("\tC: %s.*%n", decl.getNameAsString());
                fqn = findClassInPackage(decl.getNameAsString(), className);
                if (fqn != null) {
                    break;
                }
            } else {
                //System.err.printf("\tC: %s%n", decl.getName());
                if (className.equals(decl.getName().getId())) {
                    fqn = decl.getNameAsString();
                    break;
                }
            }
        }

        if (fqn == null) {
            fqn = findClassInPackage("java.lang", className);
        }

        //System.err.printf("\t\t::%s%n", fqn);
        return null;
    }

}
