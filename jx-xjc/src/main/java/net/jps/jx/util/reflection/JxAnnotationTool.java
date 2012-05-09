package net.jps.jx.util.reflection;

import java.lang.reflect.Field;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAnyElement;

/**
 *
 * @author zinic
 */
public final class JxAnnotationTool {

   /**
    * Ignore anyElement and anyAttribute marked fields for now
    *
    * @param f
    * @return
    */
   public static boolean isInterestedInField(Field f) {
      return f.getAnnotation(XmlAnyElement.class) == null && f.getAnnotation(XmlAnyAttribute.class) == null;
   }

   private JxAnnotationTool() {
   }
}
