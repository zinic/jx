package net.jps.jx.util.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import net.jps.jx.annotation.JsonField;
import net.jps.jx.jackson.mapping.MappedField;
import net.jps.jx.jaxb.JaxbConstants;

/**
 *
 * @author zinic
 */
public class ReflectiveMappedField implements MappedField {

   private final XmlAttribute xmlAttributeAnnotation;
   private final XmlElement xmlElementAnnotation;
   private final JsonField jsonFieldAnnotation;
   private final Class instanceClass;
   
   private final Object instanceRef;
   private final Field fieldRef;
   private final Method getterRef, setterRef;

   public ReflectiveMappedField(Field fieldRef, Object instanceRef) {
      this.fieldRef = fieldRef;
      this.instanceRef = instanceRef;
      
      instanceClass = instanceRef.getClass();

      xmlElementAnnotation = (XmlElement) fieldRef.getAnnotation(XmlElement.class);
      xmlAttributeAnnotation = (XmlAttribute) fieldRef.getAnnotation(XmlAttribute.class);
      jsonFieldAnnotation = (JsonField) fieldRef.getAnnotation(JsonField.class);

      getterRef = findGetter();
      setterRef = findSetter();
   }

   @Override
   public String getName() {
      return jsonFieldAnnotation != null ? jsonFieldAnnotation.value() : getJaxbNameForField();
   }
   
   @Override
   public boolean canSet() {
      return setterRef != null;
   }
   
   @Override
   public boolean isCollection() {
      return Collection.class.isAssignableFrom(instanceClass);
   }

   private String getJaxbNameForField() {
      if (xmlAttributeAnnotation != null) {
         return JaxbConstants.JAXB_DEFAULT_NAME.equals(xmlAttributeAnnotation.name()) ? fieldRef.getName() : xmlAttributeAnnotation.name();
      }

      if (xmlElementAnnotation != null) {
         return JaxbConstants.JAXB_DEFAULT_NAME.equals(xmlElementAnnotation.name()) ? fieldRef.getName() : xmlElementAnnotation.name();
      }

      return fieldRef.getName();
   }

   private Method findGetter() {
      final String getterMethodName = formatGetterMethodName(fieldRef.getName(), instanceClass);

      for (Method method : instanceClass.getMethods()) {
         if (method.getName().equals(getterMethodName)) {
            return method;
         }
      }

      throw new ReflectionException("Unable to find getter method: " + getterMethodName + "() for field: " + fieldRef.getName());
   }

   private Method findSetter() {
      final String setterMethodName = formatSetterMethodName(fieldRef.getName());

      for (Method method : instanceClass.getMethods()) {
         if (method.getName().equals(setterMethodName)) {
            return method;
         }
      }

      return null;
   }

   private String formatGetterMethodName(String name, Class fieldType) {
      final StringBuilder methodNameBuilder = formatFieldName(name);

      final String getterPrefix = fieldType.equals(boolean.class) || fieldType.equals(Boolean.class) ? "is" : "get";
      methodNameBuilder.insert(0, getterPrefix);

      return methodNameBuilder.toString();
   }

   private String formatSetterMethodName(String name) {
      final StringBuilder methodNameBuilder = formatFieldName(name);
      methodNameBuilder.insert(0, "set");

      return methodNameBuilder.toString();
   }

   private StringBuilder formatFieldName(String name) {
      final StringBuilder nameBuilder = new StringBuilder(name);

      // JAXB espcaes field names that clash with reserved keywords with a '_' character
      if (nameBuilder.charAt(0) == '_') {
         nameBuilder.deleteCharAt(0);
      }

      final char fieldNameFirstChar = nameBuilder.charAt(0);

      if (Character.isLowerCase(fieldNameFirstChar)) {
         nameBuilder.setCharAt(0, Character.toUpperCase(fieldNameFirstChar));
      }

      return nameBuilder;
   }

   @Override
   public Object get() {
      try {
         return getterRef.invoke(instanceRef);
      } catch (IllegalAccessException iae) {
         throw new ReflectionException("Unable to access method for invocation. Target method: " + getterRef.getName(), iae);
      } catch (IllegalArgumentException iae) {
         throw new ReflectionException("Illegal argument caught by underlying method during reflective call. Target method: " + getterRef.getName(), iae);
      } catch (InvocationTargetException ite) {
         throw new ReflectionException("Method invocation failed. Target method: " + getterRef.getName(), ite);
      }
   }

   @Override
   public void set(Object value) {
      final Class valueClass = value.getClass();

      if (!fieldRef.getType().isAssignableFrom(valueClass)) {
         throw new IllegalArgumentException("Value class being set: " + valueClass.getName() + " is not assignable to class: " + fieldRef.getType().getName());
      }

      try {
         setterRef.invoke(instanceRef, value);
      } catch (IllegalAccessException iae) {
         throw new ReflectionException("Unable to access method for invocation. Target method: " + setterRef.getName(), iae);
      } catch (IllegalArgumentException iae) {
         throw new ReflectionException("Illegal argument caught by underlying method during reflective call. Target method: " + setterRef.getName(), iae);
      } catch (InvocationTargetException ite) {
         throw new ReflectionException("Method invocation failed. Target method: " + setterRef.getName(), ite);
      }
   }
}
