package net.jps.jx.mapping;

import net.jps.jx.mapping.FieldDescriptor;
import java.util.List;

/**
 *
 * @author zinic
 */
public interface ClassDescriptor<T> {

    List<FieldDescriptor> getClassFields();
    
    FieldDescriptor findField(String fieldName);

    Class<T> getDescribedClass();
}
