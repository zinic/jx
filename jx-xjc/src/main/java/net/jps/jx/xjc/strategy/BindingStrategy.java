package net.jps.jx.xjc.strategy;

import net.jps.jx.xjc.strategy.result.BindingResult;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.FieldOutline;

/**
 *
 * @author zinic
 */
public interface BindingStrategy {

   BindingResult bind(BindingAnnotation bindingAnnotation, ClassOutline classOutline, FieldOutline fieldOutline);
   
   String name();
}
