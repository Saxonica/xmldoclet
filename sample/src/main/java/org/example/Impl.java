package org.example;

import java.util.List;

abstract public class Impl implements InterfaceAB, InterfaceA {
    /**
     * The Impl impl.
     * @param target target
     * @param space search space
     * @return location
     */
    @Override
    public int find(String target, List<String> space) {
        return -1;
    }

}
