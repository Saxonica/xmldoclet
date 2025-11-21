package org.example;

import java.util.Iterator;
import java.util.function.Supplier;

/**
 * Implements an iterable over a {@code Supplier<Iterator<T>>} -- if a class can
 * supply iterators on demand, then it can implement {@code Iterable}
 * @param <T>
 */
public class IterableUsingIteratorSupplier<T> implements Iterable<T> {

    private final Supplier<Iterator<T>> iteratorSupplier;

    /**
     * Create an iterable, whose {@link #iterator()} method will get an iterator
     * from the supplier provided.
     * @param iteratorSupplier the supplier of iterators, to be called whenever a
     *                         new iterator is required
     */
    public IterableUsingIteratorSupplier(Supplier<Iterator<T>> iteratorSupplier) {
        this.iteratorSupplier = iteratorSupplier;
    }

    /**
     * Returns an iterator over elements of type {@code T}.
     * @return an Iterator.
     */
    @Override
    public Iterator<T> iterator() {
        return iteratorSupplier.get();
    }
}
