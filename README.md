# java-ioc-container

An Inversion of Control (IoC) container for Java.

## How to use? 

#### Getting an instance of the Container: 
```java
var container = Container.getInstance();
```

#### Registering instances by name:

```java
container.registerInstance("random string", "key");
var resolved = container.<String>resolve("key");
```

#### Registering instances by type:

```java
container.registerInstance("hello world");
var resolved = container.resolve(String.class);
```

#### Registering by type:
```java
 container.registerType(ITestInterface.class, TestConcreteClass.class);
 var resolved = container.resolve(ITestInterface.class);
```

#### Registering singletons by type:

```java
container.registerSingletonType(ITestInterface.class, TestConcreteClass.class);
var resolved = container.resolve(ITestInterface.class);
```

#### Using `@InjectionConstructor` and `@InstanceName` annotations to mark a constructor to be used for injection and resolving constructor params by name, respectively:

```java
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
```

```java
container.registerType(ClassWithNonEmptyConstructor.class, ClassWithNonEmptyConstructor.class);
container.registerType(TestConcreteClass.class, TestConcreteClass.class);
container.registerInstance(1, "blaParam");
var resolved = container.resolve(ClassWithNonEmptyConstructor.class);
```

