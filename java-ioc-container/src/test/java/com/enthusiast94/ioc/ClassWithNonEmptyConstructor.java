package com.enthusiast94.ioc;

import com.enthusiast94.ioc.annotations.InjectionConstructor;
import com.enthusiast94.ioc.annotations.InstanceName;

public class ClassWithNonEmptyConstructor {

    public TestConcreteClass testConcreteClass;
    public int anInt;

    public ClassWithNonEmptyConstructor(TestConcreteClass testConcreteClass) {
        this.testConcreteClass = testConcreteClass;
        anInt = -1;
    }

    @InjectionConstructor
    public ClassWithNonEmptyConstructor(TestConcreteClass testConcreteClass, @InstanceName("blaParam") int anInt) {
        this.testConcreteClass = testConcreteClass;
        this.anInt = anInt;
    }
}
