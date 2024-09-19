package org.example;

public class IterImpl implements SequenceIterator {
    public boolean hasNext() {
        return false;
    }

    @Override
    public Object next() {
        return null;
    }

    @Override
    public void close() {
        // nop
    }
}
