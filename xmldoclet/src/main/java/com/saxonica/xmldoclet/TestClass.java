package com.saxonica.xmldoclet;


import com.sun.source.doctree.ReferenceTree;
import jdk.javadoc.doclet.DocletEnvironment;
import jdk.javadoc.doclet.Reporter;
import net.sf.saxon.serialize.charcode.CharacterSet;

import java.util.Locale;

/**
 * A class to hold some static constants and methods associated with processing UTF16 and surrogate pairs
 */

public class TestClass implements CharacterSet {
    /** The tick! With {@value}*/
    protected final int spoon = 17;

    private static final TestClass theInstance = new TestClass();

    /**
     * Private constructor to force the singular instance to be used
     */

    private TestClass() {
    }

    /**
     * Get the singular instance of this class
     *
     * @return the singular instance of this class
     */

    public static TestClass getInstance() {
        return theInstance;
    }

    @Override
    public boolean inCharset(int c) {
        return true;
    }

    @Override
    public String getCanonicalName() {
        return "UTF-16";
    }


    public static final int NONBMP_MIN = 0x10000;
    public static final int NONBMP_MAX = 0x10FFFF;

    public static final char SURROGATE1_MIN = (char)0xD800;
    public static final char SURROGATE1_MAX = (char)0xDBFF;
    public static final char SURROGATE2_MIN = (char)0xDC00;
    public static final char SURROGATE2_MAX = (char)0xDFFF;

    /**
     * Return the non-BMP character corresponding to a given surrogate pair
     * surrogates.
     *
     * @param high The high surrogate.
     * @param low  The low surrogate.
     * @return the Unicode codepoint represented by the surrogate pair
     */
    public static int combinePair(char high, char low) {
        return (high - SURROGATE1_MIN) * 0x400 + (low - SURROGATE2_MIN) + NONBMP_MIN;
    }

    public void foo(Class<? extends Object> spoon) {

    }

    /** {@summary A thing} A test method.
     * <p> don't need to say more than that.</p>
     * <pre class="something">some
     * pre code</pre>
     * <p>I can haz &amp; &copy; <code>code</code> and <em>enphasis</em>.</p>
     * <!-- this is a comment? -->
     * <p>{@code 3 < 5}</p>
     * <p>What about {@link XmlProcessor} here?</p>
     * <p>What about testing {@link com.saxonica.xmldoclet.scanners.XmlExecutableElement#builder builder} and {@linkplain XmlProcessor}.</p>
     * <p>What about testing {@link XmlProcessor#comment(String)}, {@link #combinePair(char, char)}</p>
     * <p>And {@link com.saxonica.xmldoclet.builder.MarkupBuilder}</p>
     * <p>Or {@linkplain XmlProcessor#resolveReference(ReferenceTree)}, {@link jdk.javadoc.doclet.Doclet#run(DocletEnvironment)}</p>
     * <p>{@link jdk.javadoc.doclet.Doclet#init(Locale, Reporter)}</p>
     * <p>{@link Spooner#m()}</p>
     * <p>Try {@link Spooner}</p>
     * <p>Try {@link DocletOption}, {@link com.saxonica.xmldoclet}</p>
     * <p>Or a {@literal thing}.</p>
     * @see <a href="https://nineml.org">NineML</a>
     * @param spoon the string {@link com.sun.source.doctree.DocTree}
     * @throws IllegalAccessError when
     * @see jdk.javadoc.doclet.Doclet#init(Locale, Reporter)
     * @return something
     */
    public int testing(String spoon) {
        return 7;
    }

    private class Spooner<T> {
        public void m() {

        }
    }
}
