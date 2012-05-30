package net.jps.jx.jackson;

import java.util.HashMap;
import java.util.Map;
import net.jps.jx.mapping.ClassDescriptor;
import net.jps.jx.mapping.FieldDescriptor;

/**
 *
 * @author zinic
 */
public class GraphNode {

   private final Map<Object, Object> nodeAttributes;
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
      
      nodeAttributes = new HashMap<Object, Object>();
   }

   public void putAttribute(Object key, Object value) {
      nodeAttributes.put(key, value);
   }
   
   public Object getAttribute(Object key) {
      return nodeAttributes.get(key);
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
