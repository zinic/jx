package net.jps.jx.jackson.mapping;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author zinic
 */
public class ClassDescriptorEntry {

    private final ClassDescriptorBuilder classDescriptorBuilder;
    private final List<FieldDescriptorBuilder> fieldDescriptorBuilders;
    private final Iterator<FieldDescriptorBuilder> nextFieldIterator;

    public ClassDescriptorEntry(ClassDescriptorBuilder classDescriptorBuilder, List<FieldDescriptorBuilder> fieldDescriptorBuilders) {
        this.classDescriptorBuilder = classDescriptorBuilder;
        this.fieldDescriptorBuilders = new LinkedList<FieldDescriptorBuilder>(fieldDescriptorBuilders);
        nextFieldIterator = this.fieldDescriptorBuilders.iterator();
    }

    public List<FieldDescriptorBuilder> getClassFields() {
        return fieldDescriptorBuilders;
    }

    public ClassDescriptorBuilder getClassDescriptorBuilder() {
        return classDescriptorBuilder;
    }

    public boolean hasFieldsToInspect() {
        return nextFieldIterator.hasNext();
    }

    public FieldDescriptorBuilder nextField() {
        return nextFieldIterator.next();
    }
}
