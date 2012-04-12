package net.jps.jx.jackson.mapping;

import net.jps.jx.mapping.MappedField;

/**
 *
 * @author zinic
 */
public class FieldDescriptorImpl implements FieldDescriptor {

    private final ClassDescriptor typeDescriptor;
    private final MappedField mappedField;

    public FieldDescriptorImpl(ClassDescriptor typeDescriptor, MappedField mappedField) {
        this.typeDescriptor = typeDescriptor;
        this.mappedField = mappedField;
    }

    @Override
    public String getName() {
        return mappedField.getName();
    }

    @Override
    public MappedField getMappedField() {
        return mappedField;
    }

    @Override
    public ClassDescriptor getTypeDescriptor() {
        return typeDescriptor;
    }
}
