package net.jps.jx.jackson.mapping;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
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

                // Start in on the next field
                final FieldDescriptorBuilder nextField = classDescriptor.nextField();
                final Class nextFieldType = nextField.getMappedFied().getField().getType();
                
                if (fieldMapper.shouldDescend(nextFieldType)) {
                    final ClassDescriptorBuilder fieldTypeCDBuilder = new ClassDescriptorBuilder(nextFieldType);
                    
                    classDescriptors.push(new ClassDescriptorEntry(nextField.getTypeDescriptorBuilder(), getFields(fieldTypeCDBuilder)));
                }
            } else {
                final ClassDescriptorBuilder builder = classDescriptor.getClassDescriptorBuilder();
                
                for (FieldDescriptorBuilder fieldBuilder : classDescriptor.getClassFields()) {
                    builder.addField(fieldBuilder.toFieldDescriptor());
                }
            }
        }

        return trunkDescriptorBuilder.toClassDescriptor();
    }

    private List<FieldDescriptorBuilder> getFields(ClassDescriptorBuilder classDescriptorBuilder) {
        final List<FieldDescriptorBuilder> interestingFields = new LinkedList<FieldDescriptorBuilder>();
        final Class classWithFields = classDescriptorBuilder.getDescribedClass();
        
        if (fieldMapper.shouldDescend(classWithFields)) {
            for (Field classField : getAllFields(classWithFields)) {
                if (JxAnnotationTool.isInterestedInField(classField)) {
                    try {
                        final MappedField mappedField = fieldMapper.mapField(classField);
                        interestingFields.add(new FieldDescriptorBuilder(new ClassDescriptorBuilder(classField.getType()), mappedField));
                    } catch (ReflectionException re) {
                        LOG.warn("Unable to map field " + classWithFields.getCanonicalName()
                                + "::" + classField.getName() + ". Reason: " + re.getMessage());
                    }
                }
            }
        }

        return interestingFields;
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
