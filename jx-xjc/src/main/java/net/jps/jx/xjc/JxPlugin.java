package net.jps.jx.xjc;

import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.Plugin;
import com.sun.tools.xjc.model.*;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.FieldOutline;
import com.sun.tools.xjc.outline.Outline;
import java.util.Collections;
import java.util.List;
import net.jps.jx.Jx;
import net.jps.jx.xjc.strategy.BindingAnnotation;
import net.jps.jx.xjc.strategy.BindingStrategy;
import net.jps.jx.xjc.strategy.XjcBindingAnnotation;
import net.jps.jx.xjc.strategy.rename.RenameStrategy;
import net.jps.jx.xjc.strategy.result.BindingResult;
import net.jps.jx.xjc.strategy.result.JxBindingResult;
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
   private final BindingStrategy[] bindingStrategies;
   private final String namespace;

   public JxPlugin() {
      this(Jx.XML_NAMESPACE);
   }

   public JxPlugin(String jxNamespace) {
      this.namespace = jxNamespace;

      bindingStrategies = new BindingStrategy[]{new RenameStrategy()};
   }

   @Override
   public List<String> getCustomizationURIs() {
      return Collections.singletonList(namespace);
   }

   @Override
   public boolean isCustomizationTagName(String nsUri, String localName) {
      return namespace.equals(nsUri) && JxLocalName.isLocalName(localName);
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

         // Currently only support field level annotations
         for (FieldOutline fieldOutline : classOutline.getDeclaredFields()) {
            readFieldAnnotations(classOutline, fieldOutline);
         }
      }

      return true;
   }

   public void readFieldAnnotations(ClassOutline classOutline, FieldOutline fieldOutline) {
      final CPluginCustomization bindingCustomization = fieldOutline.getPropertyInfo().getCustomizations().find(namespace, JxLocalName.BINDING_STRATEGY.toString());

      if (bindingCustomization != null && fieldOutline.getPropertyInfo() instanceof CPropertyInfo) {
         final BindingResult result = handleBindingAnnotation(bindingCustomization, classOutline, fieldOutline);

         if (result.successful()) {
            bindingCustomization.markAsAcknowledged();
         } else {
            LOG.error("Failure in binding: " + result.message());
            LOG.error("This will be ignored for now.");
            bindingCustomization.markAsAcknowledged();
         }
      }
   }

   public BindingResult handleBindingAnnotation(CPluginCustomization bindingCustomization, ClassOutline classOutline, FieldOutline fieldOutline) {
      final BindingAnnotation bindingAnnotation = new XjcBindingAnnotation(bindingCustomization);
      final String strategyName = bindingAnnotation.strategyName();

      if (bindingAnnotation.strategyName() != null) {
         for (BindingStrategy bindingStrategy : bindingStrategies) {
            if (bindingStrategy.name().equals(strategyName)) {
               return bindingStrategy.bind(bindingAnnotation, classOutline, fieldOutline);

            }
         }
      }

      return JxBindingResult.failure("No binding strategy registered that matches \"" + strategyName + "\"");
   }
}
