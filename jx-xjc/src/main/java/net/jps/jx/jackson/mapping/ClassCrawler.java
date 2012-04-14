package net.jps.jx.jackson.mapping;

import java.lang.reflect.Field;
import java.util.*;
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
public class ClassCrawler {

    private static final Logger LOG = LoggerFactory.getLogger(ClassCrawler.class);
    private final ClassMapper fieldMapper;
    private Class trunk;

    public ClassCrawler(ClassMapper fieldMapper, Class trunk) {
        this.fieldMapper = fieldMapper;
        this.trunk = trunk;
    }

    public ClassDescriptor getGraph() {
        final ClassDescriptorBuilder trunkDescriptorBuilder = new ClassDescriptorBuilder(trunk);
        final Stack<ClassDescriptorEntry> classDescriptors = new Stack<ClassDescriptorEntry>(); // three 's' characters can't be wrong!

        classDescriptors.push(new ClassDescriptorEntry(trunkDescriptorBuilder, getFields(trunkDescriptorBuilder)));

        while (!classDescriptors.empty()) {
            final ClassDescriptorEntry classDescriptor = classDescriptors.pop();

            if (classDescriptor.hasFieldsToInspect()) {
                // Not done with this class
                classDescriptors.push(classDescriptor);
                
                // Graph the field
                graphField(classDescriptor, classDescriptors);
            } else {
                // Render this graph node
                renderClassDescriptor(classDescriptor);
            }
        }

        return trunkDescriptorBuilder.toClassDescriptor();
    }

    private void renderClassDescriptor(final ClassDescriptorEntry classDescriptor) {
        final ClassDescriptorBuilder builder = classDescriptor.classDescriptorBuilder();

        for (FieldDescriptorBuilder fieldBuilder : classDescriptor.fieldDescriptorBuilders()) {
            builder.addFieldDescriptor(fieldBuilder.toFieldDescriptor());
        }
    }

    private void graphField(ClassDescriptorEntry classDescriptor, Stack<ClassDescriptorEntry> classDescriptors) {
        // Start in on the next field
        final FieldDescriptorBuilder nextField = classDescriptor.nextField();
        final Class nextFieldType = nextField.getMappedFied().getField().getType();

        if (fieldMapper.shouldDescend(nextFieldType)) {
            final ClassDescriptorBuilder fieldTypeCDBuilder = new ClassDescriptorBuilder(nextFieldType);

            classDescriptors.push(new ClassDescriptorEntry(nextField.getTypeDescriptorBuilder(), getFields(fieldTypeCDBuilder)));
        }
    }

    private List<FieldDescriptorBuilder> getFields(ClassDescriptorBuilder classDescriptorBuilder) {
        final Class classWithFields = classDescriptorBuilder.getDescribedClass();

        if (fieldMapper.shouldDescend(classWithFields)) {
            return filterFields(classWithFields);
        }

        return Collections.emptyList();
    }

    private List<FieldDescriptorBuilder> filterFields(Class classWithFields) {
        final List<FieldDescriptorBuilder> interestingFields = new LinkedList<FieldDescriptorBuilder>();

        for (Field classField : getAllFields(classWithFields)) {
            if (JxAnnotationTool.isInterestedInField(classField)) {
                interestingFields.add(mapField(classField));
            }
        }

        return interestingFields;
    }

    private FieldDescriptorBuilder mapField(Field classField) {
        try {
            final MappedField mappedField = fieldMapper.mapField(classField);
            return new FieldDescriptorBuilder(new ClassDescriptorBuilder(classField.getType()), mappedField);
        } catch (ReflectionException re) {
            LOG.warn("Unable to map field " + classField.getDeclaringClass().getCanonicalName() + "::" + classField.getName() + ". Reason: " + re.getMessage());
            throw re;
        }
    }

    private List<Field> getAllFields(Class classWithFields) {
        final List<Field> allFields = new LinkedList<Field>();

        Class currentClass = classWithFields;

        while (!currentClass.equals(Object.class)) {
            allFields.addAll(Arrays.asList(currentClass.getDeclaredFields()));
            currentClass = currentClass.getSuperclass();
        }

        return allFields;
    }
}
