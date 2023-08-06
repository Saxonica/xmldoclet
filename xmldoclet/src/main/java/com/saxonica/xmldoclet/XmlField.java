package com.saxonica.xmldoclet;

import com.sun.source.util.DocTrees;

/** A field on a class.
 * <p>More description here.</p>
 * @version 9.3
 * @since 2.2
 */
public class XmlField extends XmlVariable {
    /** The tick! With {@value}*/
    protected final int spoon = 17;

    /** The constructor.
     *
     * @param builder the builder
     * @param trees the trees
     */
    public XmlField(XmlBuilder builder, DocTrees trees) {
        super(builder, trees, "field");
    }

    /** {@summary A thing} A test method.
     * <p> don't need to say more than that.</p>
     * <pre class="something">some
     * pre code</pre>
     * <p>I can haz &amp; &copy; <code>code</code> and <em>enphasis</em>.</p>
     * <!-- this is a comment? -->
     * <p>{@code 3 < 5}</p>
     * <p>What about {@link XmlExecutable#builder} and {@linkplain XmlProcessor}</p>
     * <p>Or a {@literal thing}.</p>
     * @see <a href="https://nineml.org">NineML</a>
     * @param spoon the string {@link com.sun.source.doctree.DocTree}
     * @throws IllegalAccessError when
     * @return something
     */
    public int testing(String spoon) {
        return 7;
    }

    private class Spooner<T> {

    }

}
