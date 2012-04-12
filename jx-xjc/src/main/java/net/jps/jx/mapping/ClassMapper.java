package net.jps.jx.mapping;

import net.jps.jx.json.JsonTypeDescriptor;
import java.io.IOException;
import java.lang.reflect.Field;
import net.jps.jx.JxWritingException;

/**
 *
 * @author zinic
 */
public interface ClassMapper {

    MappedField mapField(Field f);

    JsonTypeDescriptor describeJsonType(Object object);

    void writeNumber(Object number, JsonNumberWriter jsonNumberWriter) throws IOException, JxWritingException;

    boolean shouldDescend(Class type);
}
