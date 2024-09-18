package org.example;

public class AlternateImplementation extends Implementation {
    @Override
    void one(int value, int otherValue) {
        System.err.println("alternate: " + value + ", " + otherValue);
    }

    @Override
    void one(String value) {
        // nop
    }

    @Override
    public void foo() {
        System.err.println("alternate foo");
    }

    @Override
    public void baz() {
        System.err.println("alternate baz");
    }
}
