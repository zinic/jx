package net.jps.jx.jackson;

import net.jps.jx.mapping.ClassDescriptor;
import net.jps.jx.mapping.FieldDescriptor;
import net.jps.jx.util.reflection.JxAnnotationTool;

/**
 *
 * @author zinic
 */
public class GraphNode {

    private final ClassDescriptor typeDescriptor;
    private final FieldDescriptor fieldDescriptor;
    private final Object instance;

    public GraphNode(Object instance, FieldDescriptor fieldDescriptor) {
        this(instance, fieldDescriptor.getTypeDescriptor(), fieldDescriptor);
    }
    
    public GraphNode(Object instance, ClassDescriptor classDescriptor) {
        this(instance, classDescriptor, null);
    }
    
    private GraphNode(Object instance, ClassDescriptor classDescriptor, FieldDescriptor fieldDescriptor) {
        this.typeDescriptor = classDescriptor;
        this.fieldDescriptor = fieldDescriptor;
        this.instance = instance;
    }

    public Object get(FieldDescriptor instanceField) {
        return instanceField.getMappedField().getAccessorFor(instance).get();
    }

    public void set(FieldDescriptor instanceField, Object value) {
        instanceField.getMappedField().getSetterFor(instance).set(value);
    }

    public Object getInstance() {
        return instance;
    }

    public ClassDescriptor getClassDescriptor() {
        return typeDescriptor;
    }

    public boolean isField() {
        return fieldDescriptor != null;
    }

    public FieldDescriptor getFieldDescriptor() {
        return fieldDescriptor;
    }
}
