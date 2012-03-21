package net.jps.jx.mapping;

import net.jps.jx.json.JsonTypeDescriptor;
import java.io.IOException;
import java.lang.reflect.Field;
import net.jps.jx.JxWritingException;

/**
 *
 * @author zinic
 */
public interface FieldMapper {

   MappedField mapField(Field f, Object fieldOwnerInstance);

   JsonTypeDescriptor getJsonType(Object object);

   void writeNumber(Object number, JsonNumberWriter jsonNumberWriter) throws IOException, JxWritingException;

   boolean shouldDescend(Class type);
}
