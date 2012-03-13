package net.jps.jx.util.reflection;

import java.lang.reflect.Field;
import javax.xml.datatype.DatatypeFactory;

/**
 *
 * @author zinic
 */
public class ReflectiveMappedXmlCalendar extends ReflectiveMappedField {

   private final DatatypeFactory datatypeFactory;

   public ReflectiveMappedXmlCalendar(Field fieldRef, Object instanceRef, DatatypeFactory datatypeFactory) {
      super(fieldRef, instanceRef);

      this.datatypeFactory = datatypeFactory;
   }

   @Override
   public void set(Object dateString) {
      if (!(dateString instanceof String)) {
         throw new IllegalArgumentException("XML dates may only be set with their string representations.");
      }

      super.set(datatypeFactory.newXMLGregorianCalendar((String) dateString));
   }
}
