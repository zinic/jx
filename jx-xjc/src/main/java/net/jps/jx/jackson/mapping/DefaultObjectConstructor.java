package net.jps.jx.jackson.mapping;

import com.sun.org.apache.xerces.internal.jaxp.datatype.DatatypeFactoryImpl;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import net.jps.jx.util.reflection.ReflectionException;

/**
 *
 * @author zinic
 */
public class DefaultObjectConstructor<T> {

   private final DatatypeFactory datatypeFactory;
   private final Class<T> instanceClass;

   public DefaultObjectConstructor(Class<T> instanceClass) {
      this(new DatatypeFactoryImpl(), instanceClass);
   }

   public DefaultObjectConstructor(DatatypeFactory datatypeFactory, Class<T> instanceClass) {
      this.datatypeFactory = datatypeFactory;
      this.instanceClass = instanceClass;
   }

   public T newInstance() {
      try {
         if (isPrimative()) {
            return constructPrimativeWrapper();
         } else if (instanceClass.isInterface()) {
            if (List.class.equals(instanceClass)) {
               return (T) new ArrayList();
            } else if (Collection.class.equals(instanceClass)) {
               return (T) new HashSet();
            } else {
               throw new ReflectionException("Unable to create instance for interface: " + instanceClass.getName());
            }
         } else if (Enum.class.isAssignableFrom(instanceClass)) {
            final T[] enumConstants = instanceClass.getEnumConstants();

            return enumConstants[0];
         } else if (XMLGregorianCalendar.class.equals(instanceClass)) {
            return (T) datatypeFactory.newXMLGregorianCalendar();
         } else {
            return instanceClass.newInstance();
         }
      } catch (IllegalAccessException iae) {
         throw new ReflectionException("Unable to access constructor for invocation. Target class: " + instanceClass.getName(), iae);
      } catch (IllegalArgumentException iae) {
         throw new ReflectionException("Illegal argument caught by underlying constrctor during reflective call. Target class: " + instanceClass.getName(), iae);
      } catch (InstantiationException ie) {
         throw new ReflectionException("Constructor invocation failed. Target class: " + instanceClass.getName(), ie);
      }
   }

   public T constructPrimativeWrapper() {
      Object instanceObject = null;

      if (Double.class.equals(instanceClass)) {
         instanceObject = Double.valueOf(Double.MIN_VALUE);
      } else if (Float.class.equals(instanceClass)) {
         instanceObject = Float.valueOf(Float.MIN_VALUE);
      } else if (Long.class.equals(instanceClass)) {
         instanceObject = Long.valueOf(Long.MIN_VALUE);
      } else if (Integer.class.equals(instanceClass)) {
         instanceObject = Integer.valueOf(Integer.MIN_VALUE);
      } else if (Short.class.equals(instanceClass)) {
         instanceObject = Short.valueOf(Short.MIN_VALUE);
      } else if (Boolean.class.equals(instanceClass)) {
         instanceObject = Boolean.FALSE;
      }

      return (T) instanceObject;
   }

   public boolean isPrimative() {
      boolean primative = false;

      if (Float.class.equals(instanceClass)) {
         primative = true;
      } else if (Double.class.equals(instanceClass)) {
         primative = true;
      } else if (Short.class.equals(instanceClass)) {
         primative = true;
      } else if (Integer.class.equals(instanceClass)) {
         primative = true;
      } else if (Long.class.equals(instanceClass)) {
         primative = true;
      } else if (Boolean.class.equals(instanceClass)) {
         primative = true;
      }

      return primative;
   }
}
