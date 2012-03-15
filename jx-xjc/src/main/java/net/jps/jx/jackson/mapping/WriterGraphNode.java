package net.jps.jx.jackson.mapping;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.Queue;
import net.jps.jx.mapping.FieldMapper;
import net.jps.jx.mapping.MappedField;
import net.jps.jx.util.reflection.JxAnnotationTool;

/**
 *
 * @author zinic
 */
public class WriterGraphNode {

   private final Queue<MappedField> valueObjectFields;
   private final Object valueObject;
   private final Boolean shouldDescend;
   private Boolean wrapped;

   public WriterGraphNode(Object valueObject, FieldMapper fieldMapper) {
      this.shouldDescend = fieldMapper.shouldDescend(valueObject.getClass());
      this.valueObject = valueObject;

      wrapped = false;
      
      valueObjectFields = new LinkedList<MappedField>();

      if (shouldDescend) {
         for (Field valueObjectField : valueObject.getClass().getDeclaredFields()) {
            if (JxAnnotationTool.isInterestedInField(valueObjectField)) {
               valueObjectFields.add(fieldMapper.mapField(valueObjectField, valueObject));
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
