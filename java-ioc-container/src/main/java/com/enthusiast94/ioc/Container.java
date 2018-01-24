package com.enthusiast94.ioc;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unchecked")
public class Container {

    private final Map<String, Registration> typedRegistrations = new ConcurrentHashMap<>();
    private final Map<String, Object> resolvedSingletonInstances = new ConcurrentHashMap<>();

    public <F, T extends F> void registerType(Class<F> from, Class<T> to) {
        this.typedRegistrations.put(from.getName(), new Registration.TypeRegistration<>(from, to, false));
    }

    public <F, T extends F> void registerSingletonType(Class<F> from, Class<T> to) {
        this.typedRegistrations.put(from.getName(), new Registration.TypeRegistration<>(from, to, true));
    }

    public <F> void registerInstance(F instance) {
        typedRegistrations.put(instance.getClass().getName(),
                new Registration.TypedInstanceRegistration<>((Class<F>) instance.getClass(), instance));
    }

    public <F> void registerInstance(F instance, String name) {
        typedRegistrations.put(name, new Registration.NamedInstanceRegistration<>((Class<F>) instance.getClass(), name, instance));
    }

    public <T> T resolve(Class<T> typeToResolve) {
        return resolve(typeToResolve.getName());
    }

    public <T> T resolve(String nameToResolve) {
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
                    Optional<Constructor> longestConstructor = Arrays.stream(typeRegistration.to.getDeclaredConstructors())
                            .max(Comparator.comparingInt(Constructor::getParameterCount));

                    if (!longestConstructor.isPresent()) {
                        throw new IocException("No public constructors found for type: " + typeRegistration.to.getName());
                    }


                    Class[] parameterTypes = longestConstructor.get().getParameterTypes();
                    Object[] parameters = new Object[parameterTypes.length];
                    for (int i = 0; i < parameterTypes.length; i++) {
                        Class parameterType = parameterTypes[i];
                        parameters[i] = resolve(parameterType);
                    }

                    instance = (T) longestConstructor.get().newInstance(parameters);
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

}
