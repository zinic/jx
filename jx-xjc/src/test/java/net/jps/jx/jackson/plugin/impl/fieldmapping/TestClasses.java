package net.jps.jx.jackson.plugin.impl.fieldmapping;

import java.util.LinkedList;
import java.util.List;
import net.jps.jx.annotation.SequenceObjectMapping;

/**
 *
 * @author zinic
 */
public class TestClasses {

   public static class SequenceMappedAsObject {

      @SequenceObjectMapping(fieldNameTarget = "key")
      private List<MappingElement> mappings;

      public List<MappingElement> getMappings() {
         if (mappings == null) {
            mappings = new LinkedList<MappingElement>();
         }

         return mappings;
      }
      
      public void setMappings(List<MappingElement> newMappings) {
         mappings = newMappings;
      }
   }

   public static class MappingElement {

      private String key;
      private String value;

      public String getKey() {
         return key;
      }

      public void setKey(String key) {
         this.key = key;
      }

      public String getValue() {
         return value;
      }

      public void setValue(String value) {
         this.value = value;
      }
   }
}
