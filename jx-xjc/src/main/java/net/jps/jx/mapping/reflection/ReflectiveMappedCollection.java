package net.jps.jx.mapping.reflection;

import net.jps.jx.mapping.MappedCollectionField;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import net.jps.jx.mapping.method.CollectionAddMethod;
import net.jps.jx.util.reflection.ReflectionException;

/**
 *
 * @author zinic
 */
public class ReflectiveMappedCollection extends ReflectiveMappedField implements MappedCollectionField {

    private final Class collectionValueClass;
    private final Method addValueMethod;

    public ReflectiveMappedCollection(Field fieldRef) {
        super(fieldRef);

        try {
            addValueMethod = fieldRef.getType().getMethod("add", Object.class);
        } catch (Exception ex) {
            throw new ReflectionException("Unable to locate add(...) method on collection: " + fieldRef.getType().getName(), ex);
        }

        collectionValueClass = getCollectionType();
    }

    private Class getCollectionType() {
        final Type collectionValueType = getField().getGenericType();

        if (collectionValueType instanceof ParameterizedType) {
            final ParameterizedType pType = (ParameterizedType) collectionValueType;
            final Type[] typeArguments = pType.getActualTypeArguments();

            if (typeArguments.length > 0 && typeArguments[0] != null && typeArguments[0] instanceof Class) {
                return (Class) typeArguments[0];
            }

            throw new ReflectionException("Unable to extract run time type information from collection generic template. Not enough infomration present.");
        }

        throw new ReflectionException("Unable to extract run time type information from collection generic template. No template present.");
    }

    @Override
    public Class getCollectionValueClass() {
        return collectionValueClass;
    }

    @Override
    public CollectionAddMethod addMethodFor(final Object addTarget) {
        return new CollectionAddMethod() {

            @Override
            public void add(Object obj) {
                invoke(addTarget, addValueMethod, obj);
            }
        };
    }
}
