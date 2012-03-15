package net.jps.jx.jackson;

import net.jps.jx.annotation.JsonObjectWrap;

/**
 *
 * @author zinic
 */
public class TestClasses {

   @JsonObjectWrap
   public static class OneStringField {

      private String stringField;

      public OneStringField() {
      }

      public String getStringField() {
         return stringField;
      }

      public void setStringField(String stringField) {
         this.stringField = stringField;
      }
   }
}
