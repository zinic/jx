package net.jps.jx.jackson;

import java.util.List;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import net.jps.jx.annotation.JsonField;
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

   @JsonObjectWrap
   public static class MultiFieldMixedAnnotations {

      @JsonField("doubleVal")
      @XmlElement(name = "doubleValue")
      private double xmlDouble;
      @JsonField("number")
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

   @JsonObjectWrap
   public static class CollectionFields {

      private List<OneStringField> stringFields;

      public CollectionFields() {
      }

      public List<OneStringField> getStringFields() {
         return stringFields;
      }

      public void setStringFields(List<OneStringField> stringFields) {
         this.stringFields = stringFields;
      }
   }
}
