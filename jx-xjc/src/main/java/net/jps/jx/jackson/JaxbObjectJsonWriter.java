package net.jps.jx.jackson;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.datatype.XMLGregorianCalendar;
import net.jps.jx.annotation.JsonField;
import net.jps.jx.annotation.JsonObjectWrap;
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
public class JaxbObjectJsonWriter {

   private static final String JAXB_DEFAULT_NAME = "##default";
   private final JsonGenerator jsonGenerator;
   private final Object objectBeingWritten;
   private final Class objectClass;

   public JaxbObjectJsonWriter(JsonGenerator jsonGenerator, Object objectBeingWritten) {
      this(jsonGenerator, objectBeingWritten, objectBeingWritten.getClass());
   }

   private JaxbObjectJsonWriter(JsonGenerator jsonGenerator, Object objectBeingWritten, Class objectClass) {
      this.jsonGenerator = jsonGenerator;
      this.objectBeingWritten = objectBeingWritten;
      this.objectClass = objectClass;
   }

   public void write() throws IOException {
      final boolean shouldWrap = shouldWrap();

      // Entity start
      if (shouldWrap) {
         jsonGenerator.writeStartObject();
      }

      for (Field f : objectClass.getDeclaredFields()) {
         if (isInterestedInField(f)) {
            writeField(f);
         }
      }

      // Close entity
      if (shouldWrap) {
         jsonGenerator.writeEndObject();
      }
   }

   /**
    * Ignore anyElement and anyAttribute marked fields
    *
    * @param f
    * @return
    */
   public boolean isInterestedInField(Field f) {
      return f.getAnnotation(XmlAnyElement.class) == null && f.getAnnotation(XmlAnyAttribute.class) == null;
   }

   public boolean shouldWrap() {
      return objectClass.getAnnotation(JsonObjectWrap.class) != null;
   }

   public String getJsonNameForField(Field f) {
      final JsonField jsonFieldAnnotation = (JsonField) f.getAnnotation(JsonField.class);

      return jsonFieldAnnotation != null ? jsonFieldAnnotation.value() : getJaxbNameForField(f);
   }

   public String getJaxbNameForField(Field f) {
      final XmlAttribute xmlAttributeAnnotation = (XmlAttribute) f.getAnnotation(XmlAttribute.class);
      final XmlElement xmlElementAnnotation = (XmlElement) f.getAnnotation(XmlElement.class);

      if (xmlAttributeAnnotation != null) {
         return JAXB_DEFAULT_NAME.equals(xmlAttributeAnnotation.name()) ? f.getName() : xmlAttributeAnnotation.name();
      }

      if (xmlElementAnnotation != null) {
         return JAXB_DEFAULT_NAME.equals(xmlElementAnnotation.name()) ? f.getName() : xmlElementAnnotation.name();
      }

      return f.getName();
   }

   public void writeField(Field f) throws IOException {
      final String getterMethodName = formatGetterMethodName(f.getName(), objectClass);

      try {
         writeField(f, callGetter(getterMethodName, objectBeingWritten, objectClass));
      } catch (GetterMethodNotFoundException gmnfe) {
         throw new ReflectionException(gmnfe.getMessage(), gmnfe);
      }
   }

   public void writeField(Field f, Object fieldValue) throws IOException, GetterMethodNotFoundException {
      if (fieldValue != null) {
         System.out.println(getJsonNameForField(f));
         
         jsonGenerator.writeFieldName(getJsonNameForField(f));

         final Class fieldClass = fieldValue.getClass();

         if (Enum.class.isAssignableFrom(fieldClass)) {
            writeObject(callGetter("value", fieldValue, fieldClass));
         } else if (Iterable.class.isAssignableFrom(fieldClass)) {
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
         
         new JaxbObjectJsonWriter(jsonGenerator, fieldValue).write();
         
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

   public Object callGetterForField(String name, Class fieldType) throws GetterMethodNotFoundException {
      final String getterMethodName = formatGetterMethodName(name, fieldType);

      return callGetter(getterMethodName, objectBeingWritten, fieldType);
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

   public String formatGetterMethodName(String name, Class fieldType) {
      final StringBuilder methodNameBuilder = new StringBuilder(name);

      // JAXB espcaes field names that clash with reserved keywords with a '_' character
      if (methodNameBuilder.charAt(0) == '_') {
         methodNameBuilder.deleteCharAt(0);
      }

      final char fieldNameFirstChar = methodNameBuilder.charAt(0);

      if (Character.isLowerCase(fieldNameFirstChar)) {
         methodNameBuilder.setCharAt(0, Character.toUpperCase(fieldNameFirstChar));
      }

      final String getterPrefix = fieldType.equals(boolean.class) || fieldType.equals(Boolean.class) ? "is" : "get";
      methodNameBuilder.insert(0, getterPrefix);

      return methodNameBuilder.toString();
   }
}
