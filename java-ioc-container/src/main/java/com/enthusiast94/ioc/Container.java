package com.enthusiast94.ioc;

import com.enthusiast94.ioc.annotations.InjectionConstructor;
import com.enthusiast94.ioc.annotations.InstanceName;
import com.enthusiast94.ioc.exceptions.IocException;
import com.google.common.annotations.VisibleForTesting;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public final class Container {

    private static Container instance;
    private final Map<String, Registration> typedRegistrations = new HashMap<>();
    private final Map<String, Object> resolvedSingletonInstances = new HashMap<>();

    @VisibleForTesting
    private Container() {}

    public synchronized static Container getInstance() {
        if (instance == null) {
            instance = new Container();
        }

        return instance;
    }

    public synchronized <F, T extends F> void registerType(Class<F> from, Class<T> to) {
        this.typedRegistrations.put(from.getName(), new Registration.TypeRegistration<>(from, to, false));
    }

    public synchronized <F, T extends F> void registerSingletonType(Class<F> from, Class<T> to) {
        this.typedRegistrations.put(from.getName(), new Registration.TypeRegistration<>(from, to, true));
    }

    public synchronized <F> void registerInstance(F instance) {
        typedRegistrations.put(instance.getClass().getName(),
                new Registration.TypedInstanceRegistration<>((Class<F>) instance.getClass(), instance));
    }

    public synchronized <F> void registerInstance(F instance, String name) {
        typedRegistrations.put(name, new Registration.NamedInstanceRegistration<>((Class<F>) instance.getClass(),
                name, instance));
    }

    public synchronized <T> T resolve(Class<T> typeToResolve) {
        return resolve(typeToResolve.getName());
    }

    public synchronized <T> T resolve(String nameToResolve) {
        var registration = typedRegistrations.get(nameToResolve);

        if (registration == null) {
            throw new IocException("Failed to resolve " + nameToResolve);
        }

        if (registration instanceof Registration.TypeRegistration) {
            var typeRegistration = (Registration.TypeRegistration) registration;
                T instance = null;

                if (typeRegistration.isSingleton) {
                    instance = (T) resolvedSingletonInstances.get(nameToResolve);
                }

                if (instance == null) {
                    instance = createInstance(getInjectionConstructor(typeRegistration.to));
                }

                if (typeRegistration.isSingleton) {
                    resolvedSingletonInstances.put(nameToResolve, instance);
                }

                return instance;
        } else if (registration instanceof Registration.TypedInstanceRegistration) {
            return (T) ((Registration.TypedInstanceRegistration) registration).instance;

        } else if (registration instanceof Registration.NamedInstanceRegistration) {
            return (T) ((Registration.NamedInstanceRegistration) registration).instance;

        } else {
            throw new IocException("Cannot handle registrations of type: " + registration.getClass().getName());
        }
    }

    private Constructor getInjectionConstructor(Class type) {
        var declaredConstructors = type.getDeclaredConstructors();
        if (declaredConstructors.length == 0) {
            throw new IocException("No public constructors found for type: " + type.getName());
        }

        var annotatedConstructors = Arrays.stream(declaredConstructors)
                .filter(constructor -> constructor.getDeclaredAnnotation(InjectionConstructor.class) != null)
                .collect(Collectors.toList());

        if (annotatedConstructors.isEmpty()) {
            return Arrays.stream(declaredConstructors)
                    .max(Comparator.comparingInt(Constructor::getParameterCount)).get();
        }

        if (annotatedConstructors.size() > 1) {
            throw new IocException("At most one constructor can be annotated with @" +
                    InjectionConstructor.class.getSimpleName());
        }

        return annotatedConstructors.get(0);
    }

    private <T> T createInstance(Constructor constructor) {
        var parameterTypes = constructor.getParameters();
        var parameters = new Object[parameterTypes.length];
        for (var i = 0; i < parameterTypes.length; i++) {
            var parameter = parameterTypes[i];
            var instanceNameAnnotation = parameter.getDeclaredAnnotation(InstanceName.class);
            if (instanceNameAnnotation != null) {
                parameters[i] = resolve(instanceNameAnnotation.value());
            } else {
                parameters[i] = resolve(parameter.getType());
            }
        }

        try {
            return (T) constructor.newInstance(parameters);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new IocException(e);
        }
    }

}
