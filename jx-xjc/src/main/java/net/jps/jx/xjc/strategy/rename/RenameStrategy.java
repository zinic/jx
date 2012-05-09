package net.jps.jx.xjc.strategy.rename;

import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFieldVar;
import com.sun.tools.xjc.model.CPropertyInfo;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.FieldOutline;
import net.jps.jx.annotation.JsonFieldName;
import net.jps.jx.xjc.strategy.BindingAnnotation;
import net.jps.jx.xjc.strategy.result.BindingResult;
import net.jps.jx.xjc.strategy.BindingStrategy;
import net.jps.jx.xjc.strategy.result.JxBindingResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author zinic
 */
public class RenameStrategy implements BindingStrategy {

   private static final Logger LOG = LoggerFactory.getLogger(RenameStrategy.class);

   public RenameStrategy() {
   }

   @Override
   public BindingResult bind(BindingAnnotation bindingAnnotation, ClassOutline classOutline, FieldOutline fieldOutline) {
      final CPropertyInfo attributePropertyInfo = (CPropertyInfo) fieldOutline.getPropertyInfo();
      final String newName = bindingAnnotation.getAttribute("as");

      if (newName != null) {
         LOG.info("Class field \"" + attributePropertyInfo.getName(false) + "\" will be annotated as \"" + newName + "\"");

         final JDefinedClass jclass = classOutline.implClass;
         final JFieldVar o = jclass.fields().get(fieldOutline.getPropertyInfo().getName(false));
         o.annotate(jclass.owner().ref(JsonFieldName.class)).param("value", newName);

         return JxBindingResult.success();
      }

      return JxBindingResult.failure("Expecting an attribute named \"as\" - please review your Jx annotations.");
   }

   @Override
   public String name() {
      return "rename";
   }
}
