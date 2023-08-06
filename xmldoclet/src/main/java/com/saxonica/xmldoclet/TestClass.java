package com.saxonica.xmldoclet;


import net.sf.saxon.serialize.charcode.CharacterSet;
import net.sf.saxon.serialize.charcode.UTF16CharacterSet;
import net.sf.saxon.z.IntIterator;
import net.sf.saxon.z.IntPredicateProxy;

/**
 * A class to hold some static constants and methods associated with processing UTF16 and surrogate pairs
 */

public class TestClass implements CharacterSet {

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

}
