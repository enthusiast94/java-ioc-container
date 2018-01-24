package com.enthusiast94.ioc;

class Registration<F> {
    final Class<F> from;

    Registration(Class<F> from) {
        this.from = from;
    }

    static class TypeRegistration<F, T extends F> extends Registration<F> {

        final Class<T> to;
        final boolean isSingleton;

        TypeRegistration(Class<F> from, Class<T> to, boolean isSingleton) {
            super(from);
            this.to = to;
            this.isSingleton = isSingleton;
        }
    }

    static class TypedInstanceRegistration<F> extends Registration<F> {

        final F instance;

        TypedInstanceRegistration(Class<F> from, F instance) {
            super(from);
            this.instance = instance;
        }
    }

    static class NamedInstanceRegistration<F> extends Registration<F> {

        final String name;
        final F instance;

        NamedInstanceRegistration(Class<F> from, String name, F instance) {
            super(from);
            this.name = name;
            this.instance = instance;
        }
    }
}
