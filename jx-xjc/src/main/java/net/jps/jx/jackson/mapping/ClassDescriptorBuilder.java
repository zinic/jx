package net.jps.jx.jackson.mapping;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author zinic
 */
public class ClassDescriptorBuilder {

    private final List<FieldDescriptor> describedFields;
    private final Class describedClass;

    public ClassDescriptorBuilder(Class describedClass) {
        this.describedClass = describedClass;
        describedFields = new LinkedList<FieldDescriptor>();
    }

    public void addFieldDescriptor(FieldDescriptor fieldDescriptor) {
        describedFields.add(fieldDescriptor);
    }

    public Class getDescribedClass() {
        return describedClass;
    }

    public ClassDescriptor toClassDescriptor() {
        return new ClassDescriptorImpl(describedClass, describedFields);
    }
}
