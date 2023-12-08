package org.example;

import jdk.javadoc.doclet.DocletEnvironment;
import jdk.javadoc.doclet.Reporter;
import net.sf.saxon.lib.Feature;
import net.sf.saxon.serialize.charcode.CharacterSet;

import java.util.*;

/**
 * A class to hold some static constants and methods associated with processing UTF16 and surrogate pairs
 */

public class TestClass implements CharacterSet {
    public static final TestClass CONSTANTVALUE = new TestClass();

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
    public static final char NULL = (char)0x0;
    public static final char LT = (char)0x3C;
    public static final char SOH = (char)0x1;

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
     * <p>What about {@link Sample} here?</p>
     * <p>What about testing {@link org.example.TestClass#testing(String)} and {@linkplain Sample}.</p>
     * <p>What about testing {@link #combinePair(char, char)}</p>
     * <p>And {@link com.saxonica.xmldoclet.builder.MarkupBuilder}</p>
     * <p>Or {@linkplain TestInterface#foo()}, {@link jdk.javadoc.doclet.Doclet#run(DocletEnvironment)}</p>
     * <p>{@link jdk.javadoc.doclet.Doclet#init(Locale, Reporter)}</p>
     * <p>{@link Spooner#m()}</p>
     * <p>Try {@link Spooner}</p>
     * <p>Or a {@literal thing}.</p>
     * @see <a href="https://example.org">Example.org</a>
     * @param spoon the string {@link com.sun.source.doctree.DocTree}
     * @throws IllegalAccessError when something goes wrong
     * @throws NullPointerException: this is an error. No colon is allowed in the exception name.
     * @see jdk.javadoc.doclet.Doclet#init(Locale, Reporter)
     * @see net.sf.saxon.s9api.Processor#getConfigurationProperty(Feature)
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
