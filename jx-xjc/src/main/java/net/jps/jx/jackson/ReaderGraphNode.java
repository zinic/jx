package net.jps.jx.jackson;

import net.jps.jx.jackson.mapping.ClassDescriptor;
import net.jps.jx.jackson.mapping.FieldDescriptor;
import net.jps.jx.mapping.MappedField;
import net.jps.jx.util.reflection.JxAnnotationTool;

/**
 *
 * @author zinic
 */
public class ReaderGraphNode {

    private final ClassDescriptor classDescriptor;
    private final MappedField mappedField;
    private final Object instance;
    private final boolean wrapJsonObject;
    private boolean wrapped;

    public ReaderGraphNode(Object instance, FieldDescriptor fieldDescriptor) {
        this(instance, fieldDescriptor.getTypeDescriptor(), fieldDescriptor.getMappedField());
    }
    
    public ReaderGraphNode(Object instance, ClassDescriptor classDescriptor) {
        this(instance, classDescriptor, null);
    }
    
    private ReaderGraphNode(Object instance, ClassDescriptor classDescriptor, MappedField mappedField) {
        this.classDescriptor = classDescriptor;
        this.instance = instance;
        this.mappedField = mappedField;

        wrapJsonObject = JxAnnotationTool.shouldWrapClass(classDescriptor.getDescribedClass());

        wrapped = false;
    }

    public Object getInstance() {
        return instance;
    }

    public ClassDescriptor getClassDescriptor() {
        return classDescriptor;
    }

    public boolean isField() {
        return mappedField != null;
    }

    public MappedField getMappedField() {
        return mappedField;
    }

    public boolean shouldWrap() {
        return wrapJsonObject && !wrapped;
    }

    public boolean wasWrapped() {
        return wrapJsonObject && wrapped;
    }

    public void wrapped() {
        this.wrapped = Boolean.TRUE;
    }
}
