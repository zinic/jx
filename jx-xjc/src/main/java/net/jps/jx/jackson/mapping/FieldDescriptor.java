package net.jps.jx.jackson.mapping;

import net.jps.jx.mapping.MappedField;

/**
 *
 * @author zinic
 */
public interface FieldDescriptor {

    String getName();
    
    MappedField getMappedField();

    ClassDescriptor getTypeDescriptor();
}
