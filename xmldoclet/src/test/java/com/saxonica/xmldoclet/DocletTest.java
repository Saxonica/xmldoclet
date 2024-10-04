package com.saxonica.xmldoclet;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.tools.DocumentationTool;
import javax.tools.ToolProvider;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DocletTest {
    @Test
    public void xmldoclet() {
        String[] docletArgs = new String[]{
                "-doclet", XmlDoclet.class.getName(),
                "-docletpath", "build/classes/",
                "-sourcepath", "src/main/java:build/generated/sources/buildConfig/main",
                XmlDoclet.class.getPackageName(),
                "com.saxonica.xmldoclet",
                "com.saxonica.xmldoclet.builder",
                "com.saxonica.xmldoclet.scanners"
        };

        DocumentationTool docTool = ToolProvider.getSystemDocumentationTool();
        docTool.run(System.in, System.out, System.err, docletArgs);
    }

    @Test
    public void sample() {
        String[] docletArgs = new String[]{
                "-doclet", XmlDoclet.class.getName(),
                "-docletpath", "build/classes/",
                "-sourcepath", "../sample/src/main/java",
                "org.example",
                "org.example.packagea",
                "org.example.packageb",
        };

        DocumentationTool docTool = ToolProvider.getSystemDocumentationTool();
        docTool.run(System.in, System.out, System.err, docletArgs);
    }

    // This test is only meaningful locally
    @Disabled
    public void saxonSample() {
        List<String> saxonClasspath = new ArrayList<String>(Arrays.asList("/Volumes/Saxonica/src/saxonica/saxondev/lib/Tidy.jar",
                "/Users/ndw/java",
                "/Users/ndw/.m2/repository/org/apache/ws/commons/axiom/axiom-dom/1.2.15/axiom-dom-1.2.15.jar",
                "/Users/ndw/.m2/repository/org/apache/ws/commons/axiom/axiom-impl/1.2.15/axiom-impl-1.2.15.jar",
                "/Users/ndw/.m2/repository/com/ibm/icu/icu4j/72.1/icu4j-72.1.jar",
                "/Users/ndw/.m2/repository/jline/jline/2.14.6/jline-2.14.6.jar",
                "/Users/ndw/.m2/repository/com/saxonica/xmldoclet/0.10.0/xmldoclet-0.10.0.jar",
                "/Users/ndw/.m2/repository/net/sf/saxon/Saxon-HE/12.3/Saxon-HE-12.3.jar",
                "/Users/ndw/.m2/repository/org/xmlresolver/xmlresolver/6.0.9/xmlresolver-6.0.9.jar",
                "/Users/ndw/.m2/repository/org/xmlresolver/xmlresolver/6.0.9/xmlresolver-6.0.9-data.jar",
                "/Users/ndw/.gradle/caches/modules-2/files-2.1/com.github.javaparser/javaparser-symbol-solver-core/3.25.9/8393927d5027472188d368c698f682a8430eb9d0/javaparser-symbol-solver-core-3.25.9.jar",
                "/Users/ndw/.gradle/caches/modules-2/files-2.1/com.github.javaparser/javaparser-core/3.25.9/ffd55be7e8739c267952b5d1ea3714725840fd7c/javaparser-core-3.25.9.jar",
                "/Users/ndw/.gradle/caches/modules-2/files-2.1/javax.mail/javax.mail-api/1.6.2/17a8151bab44f9c94f34c10db70d95ba3c830eda/javax.mail-api-1.6.2.jar",
                "/Users/ndw/.gradle/caches/modules-2/files-2.1/com.sun.mail/javax.mail/1.6.2/935151eb71beff17a2ffac15dd80184a99a0514f/javax.mail-1.6.2.jar",
                "/Users/ndw/.gradle/caches/modules-2/files-2.1/org.graalvm.nativeimage/svm/22.3.1/17640179095cd558a7a62640fd0cca9996a931b4/svm-22.3.1.jar",
                "/Users/ndw/.gradle/caches/modules-2/files-2.1/org.graalvm.nativeimage/objectfile/22.3.1/6d356eebddfa67be208a8f4602d1e8f04089bf0c/objectfile-22.3.1.jar",
                "/Users/ndw/.gradle/caches/modules-2/files-2.1/org.graalvm.nativeimage/pointsto/22.3.1/7d81b8139cacdf6ca44ab6e496c849ecb5057566/pointsto-22.3.1.jar",
                "/Users/ndw/.gradle/caches/modules-2/files-2.1/org.graalvm.nativeimage/native-image-base/22.3.1/cd8e879dc6c227a98b2ba2e24cd259f2620a70b7/native-image-base-22.3.1.jar",
                "/Users/ndw/.gradle/caches/modules-2/files-2.1/org.graalvm.compiler/compiler/22.3.1/75f7349ec062c1a7d841e9fee7d971adf6cfc132/compiler-22.3.1.jar",
                "/Users/ndw/.gradle/caches/modules-2/files-2.1/org.graalvm.truffle/truffle-api/22.3.1/60ce0b7001a346039dbaaeeda37e080e3f70554f/truffle-api-22.3.1.jar",
                "/Users/ndw/.gradle/caches/modules-2/files-2.1/org.graalvm.sdk/graal-sdk/22.3.1/c2c1e3495b04729abbe717173558cfe3cc8bf6f8/graal-sdk-22.3.1.jar",
                "/Users/ndw/.m2/repository/nu/validator/htmlparser/1.4.16/htmlparser-1.4.16.jar",
                "/Users/ndw/.gradle/caches/modules-2/files-2.1/org.nineml/coffeefilter/3.2.6/86b1d532f8dc2299ffbdd693c3175c42541adc2e/coffeefilter-3.2.6.jar",
                "/Users/ndw/.gradle/caches/modules-2/files-2.1/org.nineml/coffeegrinder/3.2.6/43e55c294986164d1ae22555d0ca7809a55daf85/coffeegrinder-3.2.6.jar",
                "/Users/ndw/.m2/repository/xom/xom/1.3.5/xom-1.3.5.jar",
                "/Users/ndw/.m2/repository/org/jdom/jdom/1.1.3/jdom-1.1.3.jar",
                "/Users/ndw/.m2/repository/org/jdom/jdom2/2.0.6.1/jdom2-2.0.6.1.jar",
                "/Users/ndw/.m2/repository/dom4j/dom4j/1.6.1/dom4j-1.6.1.jar",
                "/Users/ndw/.m2/repository/org/apache/ws/commons/axiom/axiom-api/1.2.15/axiom-api-1.2.15.jar",
                "/Users/ndw/.m2/repository/org/codehaus/woodstox/woodstox-core-asl/4.2.0/woodstox-core-asl-4.2.0.jar",
                "/Users/ndw/.m2/repository/commons-logging/commons-logging/1.2/commons-logging-1.2.jar",
                "/Users/ndw/.gradle/caches/modules-2/files-2.1/org.javassist/javassist/3.30.2-GA/284580b5e42dfa1b8267058566435d9e93fae7f7/javassist-3.30.2-GA.jar",
                "/Users/ndw/.gradle/caches/modules-2/files-2.1/com.google.guava/guava/33.0.0-jre/161ba27964a62f241533807a46b8711b13c1d94b/guava-33.0.0-jre.jar",
                "/Users/ndw/.m2/repository/javax/activation/activation/1.1/activation-1.1.jar",
                "/Users/ndw/.m2/repository/org/relaxng/jing/20220510/jing-20220510.jar",
                "/Users/ndw/.m2/repository/xerces/xercesImpl/2.9.1/xercesImpl-2.9.1.jar",
                "/Users/ndw/.m2/repository/xalan/xalan/2.7.2/xalan-2.7.2.jar",
                "/Users/ndw/.m2/repository/nu/validator/htmlparser/htmlparser/1.4/htmlparser-1.4.jar",
                "/Users/ndw/.m2/repository/org/apache/geronimo/specs/geronimo-activation_1.1_spec/1.1/geronimo-activation_1.1_spec-1.1.jar",
                "/Users/ndw/.m2/repository/jaxen/jaxen/1.1.6/jaxen-1.1.6.jar",
                "/Users/ndw/.m2/repository/org/apache/geronimo/specs/geronimo-stax-api_1.0_spec/1.0.1/geronimo-stax-api_1.0_spec-1.0.1.jar",
                "/Users/ndw/.m2/repository/org/apache/james/apache-mime4j-core/0.7.2/apache-mime4j-core-0.7.2.jar",
                "/Users/ndw/.m2/repository/org/codehaus/woodstox/stax2-api/3.1.1/stax2-api-3.1.1.jar",
                "/Users/ndw/.m2/repository/com/google/guava/failureaccess/1.0.2/failureaccess-1.0.2.jar",
                "/Users/ndw/.m2/repository/com/google/guava/listenablefuture/9999.0-empty-to-avoid-conflict-with-guava/listenablefuture-9999.0-empty-to-avoid-conflict-with-guava.jar",
                "/Users/ndw/.m2/repository/com/google/code/findbugs/jsr305/3.0.2/jsr305-3.0.2.jar",
                "/Users/ndw/.gradle/caches/modules-2/files-2.1/org.checkerframework/checker-qual/3.41.0/8be6df7f1e9bccb19f8f351b3651f0bac2f5e0c/checker-qual-3.41.0.jar",
                "/Users/ndw/.gradle/caches/modules-2/files-2.1/com.google.errorprone/error_prone_annotations/2.23.0/43a27853b6c7d54893e0b1997c2c778c347179eb/error_prone_annotations-2.23.0.jar",
                "/Users/ndw/.m2/repository/isorelax/isorelax/20030108/isorelax-20030108.jar",
                "/Users/ndw/.m2/repository/xalan/serializer/2.7.2/serializer-2.7.2.jar"));

        String cp = String.join(":", saxonClasspath);

        String[] docletArgs = new String[]{
                "-doclet", XmlDoclet.class.getName(),
                "-private",
                "-docletpath", "build/classes/",
                "-classpath", cp,
                "-sourcepath", "/Volumes/Saxonica/src/saxonica/saxondev/build/javadoc_src",
                // "net.sf.saxon.stax",
                // "net.sf.saxon.tree.iter",
                // "net.sf.saxon.tree.util",
                // "net.sf.saxon.tree.wrapper",
                // "net.sf.saxon.tree.jiter",
                // "net.sf.saxon.tree.tiny",
                // "net.sf.saxon.tree",
                // "net.sf.saxon.tree.linked",
                // "net.sf.saxon.trace",
                "net.sf.saxon",
                // "net.sf.saxon.ma",
                // "net.sf.saxon.ma.trie",
                // "net.sf.saxon.ma.map",
                // "net.sf.saxon.ma.json",
                // "net.sf.saxon.ma.arrays",
                // "net.sf.saxon.ma.zeno",
                // "net.sf.saxon.xpath",
                // "net.sf.saxon.s9api",
                // "net.sf.saxon.s9api.streams",
                // "net.sf.saxon.s9api.push",
                // "net.sf.saxon.pull",
                // "net.sf.saxon.om",
                // "net.sf.saxon.value",
                // "net.sf.saxon.gizmo",
                // "net.sf.saxon.expr",
                // "net.sf.saxon.expr.compat",
                // "net.sf.saxon.expr.instruct",
                // "net.sf.saxon.expr.sort",
                // "net.sf.saxon.expr.accum",
                // "net.sf.saxon.expr.elab",
                // "net.sf.saxon.expr.parser",
                // "net.sf.saxon.expr.number",
                // "net.sf.saxon.expr.flwor",
                // "net.sf.saxon.expr.oper",
                // "net.sf.saxon.java",
                // "net.sf.saxon.serialize",
                // "net.sf.saxon.serialize.charcode",
                // "net.sf.saxon.style",
                // "net.sf.saxon.z",
                // "net.sf.saxon.option.dom4j",
                // "net.sf.saxon.option.exslt",
                // "net.sf.saxon.option.axiom",
                // "net.sf.saxon.option.local",
                // "net.sf.saxon.option.xom",
                // "net.sf.saxon.option.jdom2",
                // "net.sf.saxon.option.sql",
                // "net.sf.saxon.regex",
                // "net.sf.saxon.regex.charclass",
                // "net.sf.saxon.sapling",
                // "net.sf.saxon.type",
                // "net.sf.saxon.type.coercion",
                // "net.sf.saxon.jaxp",
                // "net.sf.saxon.transpile",
                // "net.sf.saxon.sxpath",
                // "net.sf.saxon.lib",
                // "net.sf.saxon.dom",
                // "net.sf.saxon.functions",
                // "net.sf.saxon.functions.registry",
                // "net.sf.saxon.functions.hof",
                // "net.sf.saxon.trans",
                // "net.sf.saxon.trans.packages",
                // "net.sf.saxon.trans.rules",
                // "net.sf.saxon.pattern",
                // "net.sf.saxon.pattern.qname",
                // "net.sf.saxon.resource",
                // "net.sf.saxon.query",
                // "net.sf.saxon.event",
                // "net.sf.saxon.str",
                // "javax.xml.xquery",
                // "com.saxonica.xsltextn.instruct",
                // "com.saxonica.xsltextn",
                // "com.saxonica.xsltextn.style",
                // "com.saxonica.xsltextn.pedigree",
                // "com.saxonica.xqj",
                // "com.saxonica.xqj.pull",
                // "com.saxonica",
                "com.saxonica.config",
                // "com.saxonica.config.pe",
                // "com.saxonica.ee.validate",
                // "com.saxonica.ee.domino",
                // "com.saxonica.ee.parallel",
                // "com.saxonica.ee.pin.pin",
                // "com.saxonica.ee.extfn",
                // "com.saxonica.ee.extfn.js",
                // "com.saxonica.ee.update",
                // "com.saxonica.ee.xtupdate",
                // "com.saxonica.ee.config",
                // "com.saxonica.ee.s9api",
                // "com.saxonica.ee.stream",
                // "com.saxonica.ee.stream.om",
                // "com.saxonica.ee.stream.feed",
                // "com.saxonica.ee.stream.adjunct",
                // "com.saxonica.ee.stream.watch",
                // "com.saxonica.ee.optim",
                // "com.saxonica.ee.schema",
                // "com.saxonica.ee.schema.sdoc",
                // "com.saxonica.ee.schema.fsa",
                // "com.saxonica.ee.jaxp",
                // "com.saxonica.ee.trans",
                // "com.saxonica.expr",
                // "com.saxonica.expr.sort",
                // "com.saxonica.serialize",
                // "com.saxonica.functions.extfn",
                // "com.saxonica.functions.extfn.EXPathArchive",
                // "com.saxonica.functions.registry",
                // "com.saxonica.functions.qt4",
                // "com.saxonica.functions.qt4.csv",
                // "com.saxonica.functions.qt4.ixml",
                // "com.saxonica.functions.sql",
                "com.saxonica.trans"
        };

        DocumentationTool docTool = ToolProvider.getSystemDocumentationTool();
        docTool.run(System.in, System.out, System.err, docletArgs);
    }

}
