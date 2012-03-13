package net.jps.jx.util.reflection;

import net.jps.jx.jackson.mapping.MappedEnumeration;
import java.lang.reflect.Field;

/**
 *
 * @author zinic
 */
public class ReflectiveMappedEnumeration extends ReflectiveMappedField implements MappedEnumeration {

   public ReflectiveMappedEnumeration(Field fieldRef, Object instanceRef) {
      super(fieldRef, instanceRef);
   }

   @Override
   public void set(Object enumerationValue) {
      if (!(enumerationValue instanceof String)) {
         throw new IllegalArgumentException("Enumerations may only be set with their string representations.");
      }

      final Class fieldType = getFieldRef().getType();
      final Enum[] enumConstants = (Enum[]) fieldType.getEnumConstants();

      if (enumConstants != null && enumConstants.length > 0) {
         for (Enum enumConstant : enumConstants) {
            System.out.println("Checking \"" + enumerationValue + "\" against \"" + enumConstant.name() + "\"");

            if (enumConstant.name().equals(enumerationValue)) {
               super.set(enumConstant);
               return;
            }
         }
      }
      
      throw new ReflectionException("Unable to map enumeration value: " + enumerationValue + " to enumeration class: " + fieldType.getName());
   }
}
