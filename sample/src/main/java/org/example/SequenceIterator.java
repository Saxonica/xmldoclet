package org.example;

import java.io.Closeable;

public interface SequenceIterator extends Closeable  {
    public Object next();
    default void close() {}
}
