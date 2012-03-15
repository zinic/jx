package net.jps.jx.mapping;

import net.jps.jx.json.JsonTypeDescriptor;
import java.io.IOException;
import java.lang.reflect.Field;
import net.jps.jx.JxWritingException;
import net.jps.jx.util.reflection.ReflectionException;

/**
 *
 * @author zinic
 */
public interface FieldMapper {

   MappedField mapField(Field f, Object fieldOwnerInstance) throws ReflectionException;
   
   JsonTypeDescriptor getJsonType(Object object) throws ReflectionException;
   
   void writeNumber(Object number, JsonNumberWriter jsonNumberWriter) throws IOException, JxWritingException, ReflectionException;
   
   boolean shouldDescend(Class type);
}
