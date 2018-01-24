package com.enthusiast94.ioc;

public class ClassWithNonEmptyConstructor {

    public TestConcreteClass testConcreteClass;

    @InjectionConstructor
    public ClassWithNonEmptyConstructor(TestConcreteClass testConcreteClass) {
        this.testConcreteClass = testConcreteClass;
    }

    public ClassWithNonEmptyConstructor(TestConcreteClass testConcreteClass, int bla) {
        this.testConcreteClass = testConcreteClass;
    }
}
