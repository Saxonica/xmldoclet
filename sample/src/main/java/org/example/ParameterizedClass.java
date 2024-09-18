package org.example;

public class ParameterizedClass<T> {
    private T[] _value;
    public T get(int key) {
        return _value[key];
    }
}
