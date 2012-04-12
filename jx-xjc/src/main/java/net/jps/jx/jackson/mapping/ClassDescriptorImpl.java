package net.jps.jx.jackson.mapping;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author zinic
 */
public class ClassDescriptorImpl implements ClassDescriptor {

    private final List<FieldDescriptor> classFields;
    private final Class describedClass;

    public ClassDescriptorImpl(Class describedClass, List<FieldDescriptor> classFields) {
        this.classFields = new LinkedList<FieldDescriptor>(classFields);
        this.describedClass = describedClass;
    }

    @Override
    public FieldDescriptor findField(String fieldName) {
        for (FieldDescriptor descriptor : classFields) {
            if (descriptor.getName().equals(fieldName)) {
                return descriptor;
            }
        }
        
        return null;
    }

    @Override
    public List<FieldDescriptor> getClassFields() {
        return Collections.unmodifiableList(classFields);
    }

    @Override
    public Class getDescribedClass() {
        return describedClass;
    }
}
