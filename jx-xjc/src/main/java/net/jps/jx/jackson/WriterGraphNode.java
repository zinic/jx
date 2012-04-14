package net.jps.jx.jackson;

import java.util.Iterator;
import net.jps.jx.jackson.mapping.ClassDescriptor;
import net.jps.jx.jackson.mapping.FieldDescriptor;

/**
 *
 * @author zinic
 */
public class WriterGraphNode extends GraphNode {

    private final Iterator<FieldDescriptor> fieldDescriptorIterator;

    public WriterGraphNode(Object instance, ClassDescriptor classDescriptor) {
        super(instance, classDescriptor);
        
        fieldDescriptorIterator = classDescriptor.getClassFields().iterator();
    }

    public WriterGraphNode(Object instance, FieldDescriptor fieldDescriptor) {
        super(instance, fieldDescriptor);
        
        fieldDescriptorIterator = fieldDescriptor.getTypeDescriptor().getClassFields().iterator();
    }
    
    public FieldDescriptor nextField() {
        return fieldDescriptorIterator.next();
    }
    
    public boolean hasNextField() {
        return fieldDescriptorIterator.hasNext();
    }
}
