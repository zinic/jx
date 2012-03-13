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
         if (instanceClass.isInterface()) {
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
}
