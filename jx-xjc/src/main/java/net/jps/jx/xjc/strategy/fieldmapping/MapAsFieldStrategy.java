package net.jps.jx.xjc.strategy.fieldmapping;

import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFieldVar;
import com.sun.tools.xjc.model.CPropertyInfo;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.FieldOutline;
import net.jps.jx.annotation.SequenceObjectMapping;
import net.jps.jx.xjc.strategy.BindingAnnotation;
import net.jps.jx.xjc.strategy.BindingStrategy;
import net.jps.jx.xjc.strategy.result.BindingResult;
import net.jps.jx.xjc.strategy.result.JxBindingResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author zinic
 */
public class MapAsFieldStrategy implements BindingStrategy {

   private static final Logger LOG = LoggerFactory.getLogger(MapAsFieldStrategy.class);

   public MapAsFieldStrategy() {
   }

   @Override
   public BindingResult bind(BindingAnnotation bindingAnnotation, ClassOutline classOutline, FieldOutline fieldOutline) {
      final CPropertyInfo attributePropertyInfo = (CPropertyInfo) fieldOutline.getPropertyInfo();
      
      final String fieldName = bindingAnnotation.getAttribute("field-name");
      final String valueName = bindingAnnotation.getAttribute("value-field");
      
      if (fieldName != null) {
         LOG.info("Class field \"" + attributePropertyInfo.getName(false) + "\" will be annotated as a field with the field name being the value of the field \"" + fieldName + "\"");

         final JDefinedClass jclass = classOutline.implClass;
         final JFieldVar targetField = jclass.fields().get(fieldOutline.getPropertyInfo().getName(false));
         final JAnnotationUse annotation = targetField.annotate(jclass.owner().ref(SequenceObjectMapping.class));
 
         annotation.param("fieldNameTarget", fieldName);
         
         if (valueName != null && valueName.length() > 0) {
            annotation.param("valueTarget", valueName);
         }

         return JxBindingResult.success();
      }

      return JxBindingResult.failure("Expecting an attribute named \"field-name\" - please review your Jx annotations.");
   }

   @Override
   public String name() {
      return "map-as-field";
   }
}
