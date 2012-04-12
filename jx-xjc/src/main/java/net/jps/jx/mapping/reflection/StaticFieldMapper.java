package net.jps.jx.mapping.reflection;

import net.jps.jx.mapping.ClassMapper;
import net.jps.jx.mapping.JsonNumberWriter;
import net.jps.jx.mapping.MappedField;
import net.jps.jx.json.JsonTypeDescriptor;
import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import net.jps.jx.JxWritingException;
import net.jps.jx.json.JsonType;
import net.jps.jx.util.reflection.ClassTools;

/**
 *
 * @author zinic
 */
public class StaticFieldMapper implements ClassMapper {

   private static final Class[] STRING_CLASSES = new Class[]{XMLGregorianCalendar.class, String.class, Enum.class},
           ARRAY_CLASSES = new Class[]{Iterable.class},
           BOOLEAN_CLASSES = new Class[]{Boolean.class},
           NUMBER_CLASSES = new Class[]{short.class, Short.class, int.class, Integer.class, long.class, Long.class,
                              BigInteger.class, float.class, Float.class, double.class, Double.class, BigDecimal.class},
           
           // TODO:Enhancement - This should be changed to a generated white-list. Maybe like a class graph prescan?
           DEFAULT_NON_DESCENT_CLASSES = new Class[]{Class.class, String.class, Collection.class, Enum.class, XMLGregorianCalendar.class};
   private static final StaticFieldMapper MAPPER_INSTANCE;

   static {
      try {
         MAPPER_INSTANCE = new StaticFieldMapper(DatatypeFactory.newInstance());
      } catch (DatatypeConfigurationException dce) {
         throw new MissingDatatypeFactoryException("Failed to init a datatype factory. "
                 + "Please see, http://docs.oracle.com/javase/1.5.0/docs/api/javax/xml/datatype/DatatypeFactory.html for more information.", dce);
      }
   }

   public static ClassMapper getInstance() {
      return MAPPER_INSTANCE;
   }
   private final DatatypeFactory datatypeFactory;

   public StaticFieldMapper(DatatypeFactory datatypeFactory) {
      this.datatypeFactory = datatypeFactory;
   }

   @Override
   public JsonTypeDescriptor describeJsonType(Object value) {
      if (value == null) {
         return new JsonTypeDescriptor(null, JsonType.NULL);
      }

      final Class javaType = value.getClass();

      if (ClassTools.classIsAssignableTo(javaType, STRING_CLASSES)) {
         return new JsonTypeDescriptor(javaType, JsonType.STRING);
      }

      if (ClassTools.classMatches(javaType, NUMBER_CLASSES)) {
         return new JsonTypeDescriptor(javaType, JsonType.NUMBER);
      }

      if (ClassTools.classMatches(javaType, BOOLEAN_CLASSES)) {
         return new JsonTypeDescriptor(javaType, JsonType.BOOLEAN);
      }

      if (ClassTools.classIsAssignableTo(javaType, ARRAY_CLASSES)) {
         return new JsonTypeDescriptor(javaType, JsonType.ARRAY);
      }

      return new JsonTypeDescriptor(javaType, JsonType.OBJECT);
   }

   @Override
   public void writeNumber(Object fieldValue, JsonNumberWriter jsonNumberWriter) throws IOException, JxWritingException {
      if (fieldValue instanceof Short) {
         jsonNumberWriter.writeNumber(((Short) fieldValue).shortValue());
      } else if (fieldValue instanceof Integer) {
         jsonNumberWriter.writeNumber(((Integer) fieldValue).intValue());
      } else if (fieldValue instanceof Long) {
         jsonNumberWriter.writeNumber(((Long) fieldValue).longValue());
      } else if (fieldValue instanceof BigInteger) {
         jsonNumberWriter.writeNumber((BigInteger) fieldValue);
      } else if (fieldValue instanceof Float) {
         jsonNumberWriter.writeNumber(((Float) fieldValue).floatValue());
      } else if (fieldValue instanceof Double) {
         jsonNumberWriter.writeNumber(((Double) fieldValue).doubleValue());
      } else if (fieldValue instanceof BigDecimal) {
         jsonNumberWriter.writeNumber((BigDecimal) fieldValue);
      } else {
         throw new JxWritingException("Unsupported number class: " + fieldValue.getClass().getName());
      }
   }

   @Override
   public MappedField mapField(Field field) {
      final Class fieldType = field.getType();

      if (Collection.class.isAssignableFrom(fieldType)) {
         return new ReflectiveMappedCollection(field);
      } else if (Enum.class.isAssignableFrom(fieldType)) {
         return new ReflectiveMappedEnumeration(field);
      } else if (XMLGregorianCalendar.class.isAssignableFrom(fieldType)) {
         return new ReflectiveMappedXmlCalendar(field, datatypeFactory);
      } else {
         return new ReflectiveMappedField(field);
      }
   }

   @Override
   public boolean shouldDescend(Class type) {
      if (!type.isPrimitive() && !ClassTools.classMatches(type, NUMBER_CLASSES)) {
         return !ClassTools.classIsAssignableTo(type, DEFAULT_NON_DESCENT_CLASSES);
      }

      return false;
   }
}
