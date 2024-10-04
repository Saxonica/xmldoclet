package org.example;

/**
 * Something about this class
 * @param <T> whatever a T is
 * @param <U> whatever a U is
 */
public class ParameterizedClass<T,U> {
    private T[] _value;
    private U[] _value2;
    public T getT(int key) {
        return _value[key];
    }
    public U getU(int key) {
        return _value2[key];
    }

    /**
     * A test of a method with parametric types.
     * @param value the value
     * @param <V> the type
     */
    public <V> void setV(ParameterizedClass<V,Boolean> value) {
        // nop
    }
}
