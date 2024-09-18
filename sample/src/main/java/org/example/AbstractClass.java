package org.example;

abstract public class AbstractClass implements TestInterface {
    abstract void one(int value);
    abstract void one(int value, int otherValue);
    abstract void one(String value);
    public void foo() {
        System.err.println("impl in AbstractClass");
    }
    public void inAbsr() {
        System.err.println("impl in AbstractClass");
    }
}
