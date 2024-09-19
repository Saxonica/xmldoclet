package org.example;

public class ImplABC extends ImplAB implements InterfaceABC {
    @Override
    public void b() {
        System.err.println("ImplABC implements b");
    }

    @Override
    public void c() {
        System.err.println("ImplABC implements c");
    }
}
