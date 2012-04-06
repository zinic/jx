package net.jps.jx.xjc;

import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFieldVar;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.Plugin;
import com.sun.tools.xjc.model.*;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.FieldOutline;
import com.sun.tools.xjc.outline.Outline;
import java.util.Collections;
import java.util.List;
import net.jps.jx.annotation.JsonField;
import net.jps.jx.annotation.JsonObjectWrap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

/**
 * 
 * @author zinic
 */
public class JxPlugin extends Plugin {

   private static final Logger LOG = LoggerFactory.getLogger(JxPlugin.class);
   
   private final String jxNamespace;

   public JxPlugin() {
      this("http://jpserver.net/jx");
   }

   public JxPlugin(String jxNamespace) {
      this.jxNamespace = jxNamespace;
   }

   @Override
   public List<String> getCustomizationURIs() {
      return Collections.singletonList(jxNamespace);
   }

   @Override
   public boolean isCustomizationTagName(String nsUri, String localName) {
//      LOG.info("isCustomizationTagName for \"" + nsUri + ":" + localName + "\" - " + (jxNamespace.equals(nsUri) && JxLocalName.isLocalName(localName)));

      return jxNamespace.equals(nsUri) && JxLocalName.isLocalName(localName);
   }

   @Override
   public String getOptionName() {
      return "Xjx";
   }

   @Override
   public String getUsage() {
      return "\t\t-Xjx";
   }

   @Override
   public boolean run(Outline otln, Options optns, ErrorHandler eh) throws SAXException {
//      final JClass collectionClass = otln.getCodeModel().ref(Collection.class);

      for (ClassOutline classOutline : otln.getClasses()) {
         final CClassInfo target = classOutline.target;
         final CCustomizations classCustomizations = target.getCustomizations();

         // Check for class level customizations first
         readClassAnnotations(classOutline, classCustomizations);

         for (FieldOutline fieldOutline : classOutline.getDeclaredFields()) {
            readFieldAnnotations(classOutline, fieldOutline);
         }
      }

      return true;
   }

   public void readClassAnnotations(ClassOutline classOutline, CCustomizations classCustomizations) {
      readWrappedAnnotation(classOutline, classCustomizations);
   }

   public void readFieldAnnotations(ClassOutline classOutline, FieldOutline fieldOutline) {
      readMapAnnotation(classOutline, fieldOutline);
   }

   public void readMapAnnotation(ClassOutline classOutline, FieldOutline fieldOutline) {
      final CPluginCustomization mapAnnotation = fieldOutline.getPropertyInfo().getCustomizations().find(jxNamespace, JxLocalName.ATTRIBUTE_MAP.toString());

      if (mapAnnotation != null && fieldOutline.getPropertyInfo() instanceof CPropertyInfo) {
         final CPropertyInfo attributePropertyInfo = (CPropertyInfo) fieldOutline.getPropertyInfo();

         LOG.info("Found attribute map annotation. Class field \"" + attributePropertyInfo.getName(false) + "\" should be annotated as \"" + mapAnnotation.element.getAttribute("as") + "\"");

         final JDefinedClass jclass = classOutline.implClass;
         final JFieldVar o = jclass.fields().get(fieldOutline.getPropertyInfo().getName(false));
         o.annotate(jclass.owner().ref(JsonField.class)).param("value", mapAnnotation.element.getAttribute("as"));

         mapAnnotation.markAsAcknowledged();
      }
   }

   public void readValueMapAnnotation(ClassOutline classOutline, CCustomizations classCustomizations) {
      final CPluginCustomization wrapCustomization = classCustomizations.find(jxNamespace, JxLocalName.ELEMENT_VALUE_MAP.toString());

      if (wrapCustomization != null) {
         LOG.info("Found value map annotation.");

         handleWrappedType(classOutline);
         wrapCustomization.markAsAcknowledged();
      }
   }

   public void readWrappedAnnotation(ClassOutline classOutline, CCustomizations classCustomizations) {
      final CPluginCustomization wrapCustomization = classCustomizations.find(jxNamespace, JxLocalName.OBJECT_WRAP.toString());

      if (wrapCustomization != null) {
         LOG.info("Found wrap annotation. This class should be annotated.");

         handleWrappedType(classOutline);
         wrapCustomization.markAsAcknowledged();
      }
   }

   public void handleWrappedType(ClassOutline classOutline) {
      final JDefinedClass jclass = classOutline.implClass;
      jclass.annotate(jclass.owner().ref(JsonObjectWrap.class));
   }
}
