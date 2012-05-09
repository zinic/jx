package net.jps.jx.xjc.strategy;

import com.sun.tools.xjc.model.CPluginCustomization;

/**
 *
 * @author zinic
 */
public class XjcBindingAnnotation implements BindingAnnotation {

   private final CPluginCustomization pluginCustomization;

   public XjcBindingAnnotation(CPluginCustomization pluginCustomization) {
      this.pluginCustomization = pluginCustomization;
   }

   @Override
   public String getAttribute(String attributeName) {
      return pluginCustomization.element.getAttribute(attributeName);
   }

   @Override
   public String strategyName() {
      return getAttribute("strategy");
   }
}
