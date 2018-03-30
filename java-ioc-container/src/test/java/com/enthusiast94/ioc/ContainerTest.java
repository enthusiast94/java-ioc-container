package com.enthusiast94.ioc;

import com.enthusiast94.ioc.exceptions.IocException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContainerTest {

    private Container container;

    @BeforeEach
    void beforeEach() {
        container = Container.getInstance();
    }

    @Test
    void shouldNotResolveUnregisteredTypes() {
        var exception = assertThrows(IocException.class, () -> container.resolve(String.class));
        assertThat(exception.getMessage(), is("Failed to resolve java.lang.String"));
    }

    @Test
    void shouldNotResolveUnregisteredNames() {
        var exception = assertThrows(IocException.class, () -> container.resolve("unregistered name"));
        assertThat(exception.getMessage(), is("Failed to resolve unregistered name"));
    }

    @Test
    void shouldResolveInstances() {
        container.registerInstance("hello world");
        var resolved = container.resolve(String.class);
        assertThat(resolved, is("hello world"));
    }

    @Test
    void shouldResolveNamedInstances() {
        container.registerInstance("random string", "key");
        var resolved = container.<String>resolve("key");
        assertThat(resolved, is("random string"));
    }

    @Test
    void shouldResolveRegisteredNonSingletonType() {
        container.registerType(ITestInterface.class, TestConcreteClass.class);
        var resolved = container.resolve(ITestInterface.class);
        var resolved2 = container.resolve(ITestInterface.class);
        assertFalse(resolved == resolved2);
    }

    @Test
    void shouldResolveRegisteredSingletonType() {
        container.registerSingletonType(ITestInterface.class, TestConcreteClass.class);
        var resolved = container.resolve(ITestInterface.class);
        var resolved2 = container.resolve(ITestInterface.class);
        assertTrue(resolved == resolved2);
    }

    @Test
    void shouldResolveRegisteredTypeWithNonEmptyConstructor() {
        container.registerType(ClassWithNonEmptyConstructor.class, ClassWithNonEmptyConstructor.class);
        container.registerType(TestConcreteClass.class, TestConcreteClass.class);
        container.registerInstance(1, "blaParam");
        container.resolve(ClassWithNonEmptyConstructor.class);
    }
}