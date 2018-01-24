package com.enthusiast94.ioc;

public class ClassWithNonEmptyConstructor {

    public TestConcreteClass testConcreteClass;

    public ClassWithNonEmptyConstructor(TestConcreteClass testConcreteClass) {
        this.testConcreteClass = testConcreteClass;
    }
}
