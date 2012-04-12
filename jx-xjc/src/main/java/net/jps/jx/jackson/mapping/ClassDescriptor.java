package net.jps.jx.jackson.mapping;

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
