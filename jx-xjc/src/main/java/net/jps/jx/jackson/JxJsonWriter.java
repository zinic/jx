package net.jps.jx.jackson;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import javax.xml.datatype.XMLGregorianCalendar;
import net.jps.jx.annotation.JsonObjectWrap;
import net.jps.jx.util.reflection.JxAnnotationTool;
import net.jps.jx.util.reflection.GetterMethodNotFoundException;
import net.jps.jx.jackson.mapping.MappedField;
import net.jps.jx.util.reflection.ReflectionException;
import net.jps.jx.util.reflection.ReflectiveMappedField;
import net.jps.jx.jaxb.JaxbConstants;
import org.codehaus.jackson.JsonGenerator;

/**
 *
 * Playing with my own version of auto-serializing JSON writer for JAXB
 * annotated entities
 *
 * This is just an experiment with Jackson at a lower API in order to acquire
 * more fine-grained control of the JSON that is produced during the
 * serialization process.
 *
 * This is far from complete and has no tests.
 *
 * @author zinic
 */
public class JxJsonWriter {

   private final JsonGenerator jsonGenerator;
   private final Object objectBeingWritten;
   private final Class objectClass;

   public JxJsonWriter(JsonGenerator jsonGenerator, Object objectBeingWritten) {
      this(jsonGenerator, objectBeingWritten, objectBeingWritten.getClass());
   }

   private JxJsonWriter(JsonGenerator jsonGenerator, Object objectBeingWritten, Class objectClass) {
      this.jsonGenerator = jsonGenerator;
      this.objectBeingWritten = objectBeingWritten;
      this.objectClass = objectClass;
   }

   public void write() throws IOException {
      final boolean shouldWrap = JxAnnotationTool.shouldWrapClass(objectClass);

      // Entity start
      if (shouldWrap) {
         jsonGenerator.writeStartObject();
      }

      for (Field field : objectClass.getDeclaredFields()) {
         if (JxAnnotationTool.isInterestedInField(field)) {
            writeField(field);
         }
      }

      // Close entity
      if (shouldWrap) {
         jsonGenerator.writeEndObject();
      }
   }

   public void writeField(Field field) throws IOException {
      final MappedField mappedField = new ReflectiveMappedField(field, objectBeingWritten);

      try {
         writeMappedField(mappedField);
      } catch (GetterMethodNotFoundException gmnfe) {
         throw new ReflectionException(gmnfe.getMessage(), gmnfe);
      }
   }

   public void writeMappedField(MappedField mappedField) throws IOException, GetterMethodNotFoundException {
      final Object fieldValue = mappedField.get();
      
      if (fieldValue != null) {
         jsonGenerator.writeFieldName(mappedField.getName());

         final Class fieldValueClass = fieldValue.getClass();

         if (Enum.class.isAssignableFrom(fieldValueClass)) {
            writeObject(callGetter("value", fieldValue, fieldValueClass));
         } else if (Iterable.class.isAssignableFrom(fieldValueClass)) {
            jsonGenerator.writeStartArray();

            for (Object o : (Iterable) fieldValue) {
               writeObject(o);
            }

            jsonGenerator.writeEndArray();
         } else {
            writeObject(fieldValue);
         }
      } else {
         // Ignore nulls for now
      }
   }

   public void writeObject(Object fieldValue) throws IOException, GetterMethodNotFoundException {
      final Class fieldClass = fieldValue.getClass();

      if (classMatches(fieldClass, String.class)) {
         jsonGenerator.writeString((String) fieldValue);
      } else if (classMatches(fieldClass, Boolean.class)) {
         jsonGenerator.writeBoolean((Boolean) fieldValue);
      } else if (classMatches(fieldClass, Short.class)) {
         jsonGenerator.writeNumber(((Short) fieldValue).intValue());
      } else if (classMatches(fieldClass, Integer.class)) {
         jsonGenerator.writeNumber(((Integer) fieldValue).intValue());
      } else if (classMatches(fieldClass, Long.class)) {
         jsonGenerator.writeNumber(((Long) fieldValue).longValue());
      } else if (classMatches(fieldClass, BigInteger.class)) {
         jsonGenerator.writeNumber((BigInteger) fieldValue);
      } else if (classMatches(fieldClass, Float.class)) {
         jsonGenerator.writeNumber(((Float) fieldValue).floatValue());
      } else if (classMatches(fieldClass, Double.class)) {
         jsonGenerator.writeNumber(((Double) fieldValue).doubleValue());
      } else if (classMatches(fieldClass, BigDecimal.class)) {
         jsonGenerator.writeNumber((BigDecimal) fieldValue);
      } else if (XMLGregorianCalendar.class.isAssignableFrom(fieldClass)) {
         jsonGenerator.writeString(((XMLGregorianCalendar) fieldValue).toString());
      } else {
         jsonGenerator.writeStartObject();
         
         new JxJsonWriter(jsonGenerator, fieldValue).write();
         
         jsonGenerator.writeEndObject();
      }
   }

   public boolean classMatches(Class type, Class... classes) {
      for (Class c : classes) {
         if (type.equals(c)) {
            return true;
         }
      }

      return false;
   }

   public Object callGetter(String getterMethodName, Object o, Class type) throws GetterMethodNotFoundException {
      for (Method m : type.getMethods()) {
         if (getterMethodName.equals(m.getName())) {
            try {
               return m.invoke(o);
            } catch (IllegalAccessException iae) {
               throw new ReflectionException("Unable to access method for invocation. Target method: " + getterMethodName, iae);
            } catch (IllegalArgumentException iae) {
               throw new ReflectionException("Illegal argument caught by underlying method during reflective call. Target method: " + getterMethodName, iae);
            } catch (InvocationTargetException ite) {
               throw new ReflectionException("Method invocation failed. Target method: " + getterMethodName, ite);
            }
         }
      }

      throw new GetterMethodNotFoundException("Unable to find getter method for field \"" + type.getCanonicalName() + "\". Looking for: " + getterMethodName);
   }
}
