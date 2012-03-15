package net.jps.jx.jackson.mapping;

import net.jps.jx.mapping.reflection.StaticFieldMapper;
import net.jps.jx.mapping.FieldMapper;
import net.jps.jx.mapping.MappedField;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import net.jps.jx.util.reflection.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author zinic
 */
public class ReaderGraphNodeValue<T> {
   
   public static <T> ReaderGraphNodeValue builderFor(Class<T> instanceClass, FieldMapper fieldMapper) throws ReflectionException {
      return new ReaderGraphNodeValue(new DefaultObjectConstructor<T>(instanceClass).newInstance());
   }
   
   private static final Logger LOG = LoggerFactory.getLogger(ReaderGraphNodeValue.class);
   
   private final Map<String, ReaderGraphNode> objectGraph;
   private final FieldMapper fieldMapper;
   private final T fieldOwnerInstance;

   public ReaderGraphNodeValue(T fieldOwnerInstance) {
      this(fieldOwnerInstance, StaticFieldMapper.getInstance());
   }

   public ReaderGraphNodeValue(T fieldOwnerInstance, FieldMapper fieldMapper) {
      this.fieldOwnerInstance = fieldOwnerInstance;
      this.fieldMapper = fieldMapper;

      objectGraph = buildValueObjectGraph();
   }

   private Map<String, ReaderGraphNode> buildValueObjectGraph() {
      final Map<String, ReaderGraphNode> partialObjectGraph = new HashMap<String, ReaderGraphNode>();

      final Class foInstanceClass = fieldOwnerInstance.getClass();
      
      for (Field field : foInstanceClass.getDeclaredFields()) {
         if (JxAnnotationTool.isInterestedInField(field)) {
            LOG.debug("Mapping field: " + field.getName() + "::" + field.getType().getName() + " on to class: " + foInstanceClass.getName());

            final MappedField mappedField = fieldMapper.mapField(field, fieldOwnerInstance);

            ReaderGraphNodeValue fieldObjectBuilder = null;

            if (fieldMapper.shouldDescend(field.getType())) {
               fieldObjectBuilder = ReaderGraphNodeValue.builderFor(field.getType(), fieldMapper);
            }

            partialObjectGraph.put(mappedField.getName(), new ReaderGraphNode(fieldObjectBuilder, mappedField));
         }
      }

      return partialObjectGraph;
   }


   public T getFieldOwnerInstance() {
      return fieldOwnerInstance;
   }

   public ReaderGraphNode getField(String fieldName) {
      return objectGraph.get(fieldName);
   }
}
