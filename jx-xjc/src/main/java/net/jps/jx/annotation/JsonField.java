package net.jps.jx.annotation;

import java.lang.annotation.*;

/**
 *
 * @author zinic
 */
@Inherited
@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = {ElementType.FIELD})
public @interface JsonField {

   public String value() default "";
}
