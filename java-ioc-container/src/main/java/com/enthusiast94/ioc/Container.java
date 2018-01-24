package com.enthusiast94.ioc;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public final class Container {

    private final Map<String, Registration> typedRegistrations = new HashMap<>();
    private final Map<String, Object> resolvedSingletonInstances = new HashMap<>();

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
        Registration registration = typedRegistrations.get(nameToResolve);

        if (registration == null) {
            throw new IocException("Failed to resolve " + nameToResolve);
        }

        if (registration instanceof Registration.TypeRegistration) {
            try {
                Registration.TypeRegistration typeRegistration = (Registration.TypeRegistration) registration;
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
            } catch (Exception e) {
                throw new IocException(e);
            }
        } else if (registration instanceof Registration.TypedInstanceRegistration) {
            return (T) ((Registration.TypedInstanceRegistration) registration).instance;
        } else if (registration instanceof Registration.NamedInstanceRegistration) {
            return (T) ((Registration.NamedInstanceRegistration) registration).instance;
        } else {
            throw new IocException("Cannot handle registrations of type: " + registration.getClass().getName());
        }
    }

    private Constructor getInjectionConstructor(Class type) {
        Constructor[] declaredConstructors = type.getDeclaredConstructors();
        if (declaredConstructors.length == 0) {
            throw new IocException("No public constructors found for type: " + type.getName());
        }

        List<Constructor> annotatedConstructors = Arrays.stream(declaredConstructors)
                .filter(constructor -> constructor.getAnnotation(InjectionConstructor.class) != null)
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

    private <T> T createInstance(Constructor longestConstructor) throws InstantiationException, IllegalAccessException, java.lang.reflect.InvocationTargetException {
        Class[] parameterTypes = longestConstructor.getParameterTypes();
        Object[] parameters = new Object[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            Class parameterType = parameterTypes[i];
            parameters[i] = resolve(parameterType);
        }

        return (T) longestConstructor.newInstance(parameters);
    }

}
