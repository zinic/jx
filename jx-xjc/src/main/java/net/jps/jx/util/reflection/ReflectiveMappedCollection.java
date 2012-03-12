package net.jps.jx.util.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import net.jps.jx.jackson.mapping.ObjectGraphBuilder;

/**
 *
 * @author zinic
 */
public class ReflectiveMappedCollection extends ReflectiveMappedField implements MappedCollection {

   private final Class collectionValueClass;
   private final Method addValueMethod;

   public ReflectiveMappedCollection(Field fieldRef, Object instanceRef) {
      super(fieldRef, instanceRef);

      try {
         addValueMethod = fieldRef.getType().getMethod("add", Object.class);
      } catch (Exception ex) {
         throw new ReflectionException("Unable to locate add(...) method on collection: " + getInstanceClass().getName());
      }

      collectionValueClass = getCollectionType();
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
   public ObjectGraphBuilder newCollectionValue() {
      final ObjectGraphBuilder newObjectGraphBuilder = ObjectGraphBuilder.builderFor(collectionValueClass);
      add(newObjectGraphBuilder.getObjectInstance());
      
      return ObjectGraphBuilder.builderFor(collectionValueClass);
   }

   @Override
   public void add(Object obj) {
      invoke(addValueMethod, get());
   }
}
