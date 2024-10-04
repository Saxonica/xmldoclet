package org.example;

import java.util.List;

public class ImplABC extends ImplAB implements InterfaceABC {
    @Override
    public void b() {
        System.err.println("ImplABC implements b");
    }

    @Override
    public void c() {
        System.err.println("ImplABC implements c");
    }

    @Override
    public int find(String target, List<String> space) {
        return 7;
    }
}
