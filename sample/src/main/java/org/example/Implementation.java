package org.example;

public abstract class Implementation extends AbstractClass {
    @Override
    public void foo() {
        // nop
    }

    @Override
    public void bar() {
        // nop
    }

    @Override
    void one(int value) {
        System.err.println("int: " + value);
    }

    @Override
    void one(int value, int otherValue) {
        System.err.println("int: " + value + ", " + otherValue);
    }

    void inImpl() {
        System.err.println("In impl");
    }
}
