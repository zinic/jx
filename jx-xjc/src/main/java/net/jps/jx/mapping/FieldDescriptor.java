package net.jps.jx.mapping;

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
