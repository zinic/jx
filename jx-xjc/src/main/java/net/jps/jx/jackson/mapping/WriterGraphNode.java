package net.jps.jx.jackson.mapping;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import net.jps.jx.mapping.ClassMapper;
import net.jps.jx.mapping.MappedField;
import net.jps.jx.util.reflection.JxAnnotationTool;
import net.jps.jx.util.reflection.ReflectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author zinic
 */
public class WriterGraphNode {

    private static final Logger LOG = LoggerFactory.getLogger(WriterGraphNode.class);
    private final Queue<MappedField> valueObjectFields;
    private final Object valueObject;
    private final Boolean shouldDescend;
    private Boolean wrapped;

    public WriterGraphNode(Object valueObject, ClassMapper fieldMapper) {
        this.shouldDescend = fieldMapper.shouldDescend(valueObject.getClass());
        this.valueObject = valueObject;

        wrapped = false;

        valueObjectFields = new LinkedList<MappedField>();

        if (shouldDescend) {
            final List<Field> allObjectFields = new LinkedList<Field>();

            Class classWithFields = valueObject.getClass();

            while (!classWithFields.equals(Object.class)) {
                allObjectFields.addAll(Arrays.asList(classWithFields.getDeclaredFields()));
                classWithFields = classWithFields.getSuperclass();
            }

            for (Field valueObjectField : allObjectFields) {
                if (JxAnnotationTool.isInterestedInField(valueObjectField)) {
                    try {
//                        valueObjectFields.add(fieldMapper.mapField(valueObjectField, valueObject));
                    } catch (ReflectionException re) {
                        LOG.warn("Unable to map field " + valueObject.getClass().getCanonicalName()
                                + "::" + valueObjectField.getName() + ". Reason: " + re.getMessage());
                    }
                }
            }
        }
    }

    public boolean shouldWrap() {
        return JxAnnotationTool.shouldWrapClass(valueObject.getClass()) && !wrapped;
    }

    public boolean wasWrapped() {
        return JxAnnotationTool.shouldWrapClass(valueObject.getClass()) && wrapped;
    }

    public void wrapped() {
        wrapped = true;
    }

    public boolean isIterable() {
        return Iterable.class.isAssignableFrom(valueObject.getClass());
    }

    public Object getValueObject() {
        return valueObject;
    }

    public boolean hasNextField() {
        return shouldDescend && !valueObjectFields.isEmpty();
    }

    public MappedField nextMappedField() {
        return valueObjectFields.poll();
    }
}
