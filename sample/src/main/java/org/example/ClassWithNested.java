package org.example;

public abstract class ClassWithNested implements InterfaceA, InterfaceB {
    /**
     * Something goes here
     * @return true
     */
    public boolean isSomething() {
        return true;
    }

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
        }
    }
}
