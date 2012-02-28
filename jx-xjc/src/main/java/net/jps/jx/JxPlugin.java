package net.jps.jx;

import com.sun.codemodel.JDefinedClass;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.Plugin;
import com.sun.tools.xjc.model.CClassInfo;
import com.sun.tools.xjc.model.CCustomizations;
import com.sun.tools.xjc.model.CPluginCustomization;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.Outline;
import java.util.Collections;
import java.util.List;
import net.jps.jx.annotation.JxWrapObject;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

/**
 *
 * @author zinic
 */
public class JxPlugin extends Plugin {

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
      System.out.println("isCustomizationTagName for \"" + nsUri + ":" + localName + "\" - " + (jxNamespace.equals(nsUri) && JxLocalName.isLocalName(localName)));
      
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
      for (ClassOutline classOutline : otln.getClasses()) {
         final CClassInfo target = classOutline.target;
         final CCustomizations classCustomizations = target.getCustomizations();
         
         // Check for wrap customization
         final CPluginCustomization wrapCustomization = classCustomizations.find(jxNamespace, JxLocalName.OBJECT_WRAP.toString());
         
         if (wrapCustomization != null) {
            System.out.println("Found wrap annotation. This class should be annotated.");
            
            handleWrappedType(classOutline);
            wrapCustomization.markAsAcknowledged();
         }
      }

      return true;
   }
   
   public void handleWrappedType(ClassOutline classOutline) {
      final JDefinedClass jclass = classOutline.implClass;
      jclass.annotate(jclass.owner().ref(JxWrapObject.class));
   }
}
