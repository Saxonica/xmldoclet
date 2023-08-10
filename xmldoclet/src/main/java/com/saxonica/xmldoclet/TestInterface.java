package com.saxonica.xmldoclet;

public interface TestInterface {
    void foo();

    /**
     * This is a bar.
     * <p>See als {@link #foo()}.</p>
     */
    void bar();
}
