package org.example;

public class EmptyIterator implements SequenceIterator {
    @Override
    public Object next() {
        return null;
    }

    private static class OfNodes extends EmptyIterator implements AxisIterator {

        public final static OfNodes THE_INSTANCE = new OfNodes();
        @Override
        public String next() {
            return null;
        }
    }
}
