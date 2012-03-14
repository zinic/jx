package net.jps.jx.util.reflection.mapping;

import net.jps.jx.jackson.mapping.MappedCollection;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import net.jps.jx.jackson.mapping.ObjectGraphBuilder;
import net.jps.jx.util.reflection.ReflectionException;

/**
 *
 * @author zinic
 */
public class ReflectiveMappedCollection extends ReflectiveMappedField implements MappedCollection {

   private final Class collectionValueClass;
   private final Method addValueMethod;

   public ReflectiveMappedCollection(Field fieldRef, Object instanceRef) {
      super(fieldRef, instanceRef);

      if (hasSetter()) {
         final Object newCollectionInstance = newCollection(getFieldRef().getType());
         set(newCollectionInstance);
      }

      final Object collectionReference = get();

      try {
         addValueMethod = collectionReference.getClass().getMethod("add", Object.class);
      } catch (Exception ex) {
         throw new ReflectionException("Unable to locate add(...) method on collection: " + getInstanceClass().getName(), ex);
      }

      collectionValueClass = getCollectionType();
   }

   private Object newCollection(Class collectionClass) {
      if (List.class.isAssignableFrom(collectionClass)) {
         return new ArrayList();
      } else if (Collection.class.isAssignableFrom(collectionClass)) {
         return new HashSet();
      }

      throw new ReflectionException("Unknown collection type: " + collectionClass.getName());
   }

   private Class getCollectionType() {
      final Type collectionValueType = getFieldRef().getGenericType();

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
   public void add(Object obj) {
      invoke(get(), addValueMethod, obj);
   }
}
