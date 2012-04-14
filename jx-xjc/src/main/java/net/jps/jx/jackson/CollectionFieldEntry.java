package net.jps.jx.jackson;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import net.jps.jx.jackson.mapping.FieldDescriptor;
import net.jps.jx.mapping.MappedCollectionField;
import net.jps.jx.util.reflection.ReflectionException;

/**
 *
 * @author zinic
 */
public class CollectionFieldEntry {

    private final MappedCollectionField mappedCollection;
    private final Class collectionValueClass;

    public CollectionFieldEntry(FieldDescriptor collectionFieldDescriptor) {
        this.mappedCollection = (MappedCollectionField) collectionFieldDescriptor.getMappedField();
        collectionValueClass = getCollectionType(mappedCollection.getField());
    }

    public MappedCollectionField getMappedCollection() {
        return mappedCollection;
    }

    public Class getCollectionValueClass() {
        return collectionValueClass;
    }
    
    public void add(Object collection, Object value) {
        mappedCollection.addMethodFor(collection).add(value);
    }

    private static Class getCollectionType(Field fieldRef) {
        final Type collectionValueType = fieldRef.getGenericType();

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
}
