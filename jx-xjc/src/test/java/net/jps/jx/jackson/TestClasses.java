package net.jps.jx.jackson;

import java.util.LinkedList;
import java.util.List;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import net.jps.jx.annotation.JsonFieldName;

/**
 *
 * @author zinic
 */
public class TestClasses {

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

   public static class MultiFieldMixedAnnotations {

      @JsonFieldName("doubleVal")
      @XmlElement(name = "doubleValue")
      private double xmlDouble;
      @JsonFieldName("number")
      private Integer jsonNumber;
      @XmlAttribute(name = "not-default")
      private String _default;
      private String stringField;

      public MultiFieldMixedAnnotations() {
      }

      public String getDefault() {
         return _default;
      }

      public void setDefault(String _default) {
         this._default = _default;
      }

      public Integer getJsonNumber() {
         return jsonNumber;
      }

      public void setJsonNumber(Integer jsonNumber) {
         this.jsonNumber = jsonNumber;
      }

      public String getStringField() {
         return stringField;
      }

      public void setStringField(String stringField) {
         this.stringField = stringField;
      }

      public double getXmlDouble() {
         return xmlDouble;
      }

      public void setXmlDouble(Double xmlDouble) {
         this.xmlDouble = xmlDouble;
      }
   }

   public static class CollectionFields {

      private List<OneStringField> stringFields;

      public CollectionFields() {
          stringFields = new LinkedList<OneStringField>();
      }

      public List<OneStringField> getStringFields() {
         return stringFields;
      }

      public void setStringFields(List<OneStringField> stringFields) {
         this.stringFields = stringFields;
      }
   }
}
