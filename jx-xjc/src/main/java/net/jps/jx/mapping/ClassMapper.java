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

    JsonTypeDescriptor describeJsonType(Class type);

    boolean isCollection(Class type);
    
    // TODO: ISP/SRP violation - move this to JxControls
    void writeNumber(Object number, JsonNumberWriter jsonNumberWriter) throws IOException, JxWritingException;

    boolean shouldDescend(Class type);
}
