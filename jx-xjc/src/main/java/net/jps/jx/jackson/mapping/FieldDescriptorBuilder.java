package net.jps.jx.jackson.mapping;

import net.jps.jx.mapping.impl.FieldDescriptorImpl;
import net.jps.jx.mapping.FieldDescriptor;
import net.jps.jx.mapping.MappedField;

/**
 *
 * @author zinic
 */
public class FieldDescriptorBuilder {

    private final ClassDescriptorBuilder typeDescriptorBuilder;
    private final MappedField mappedFied;

    public FieldDescriptorBuilder(ClassDescriptorBuilder typeDescriptorBuilder, MappedField mappedFied) {
        this.typeDescriptorBuilder = typeDescriptorBuilder;
        this.mappedFied = mappedFied;
    }

    public ClassDescriptorBuilder getTypeDescriptorBuilder() {
        return typeDescriptorBuilder;
    }

    public MappedField getMappedFied() {
        return mappedFied;
    }

    public FieldDescriptor toFieldDescriptor() {
        return new FieldDescriptorImpl(typeDescriptorBuilder.toClassDescriptor(), mappedFied);
    }
}
