package org.example;

import java.util.List;

public abstract class ClassWithNested implements InterfaceA, InterfaceB {
    /**
     * Something goes here
     * @return true
     */
    public boolean isSomething() {
        return true;
    }

    /**
     * Parameterised subclass to accept items of a particular item type
     * @param <T> the item type of the items returned
     */
    public abstract static class Nested<T extends Impl> extends ClassWithNested implements InterfaceAB {
        protected T[] items;
        public Nested(T[] items) {
            this.items = items;
        }
        public void a() {}

        public static class DoublyNested extends ClassWithNested {
            public DoublyNested() {}
            public void a() {}
            public void b() {}
            public int find(String target, List<String> space) {
                return 7;
            }
        }
    }
}
