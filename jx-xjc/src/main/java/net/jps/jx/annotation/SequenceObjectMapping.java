package net.jps.jx.annotation;

import java.lang.annotation.*;

@Inherited
@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = {ElementType.FIELD})
public @interface SequenceObjectMapping {

   public static final String DEFAULT_VALUE_TARGET = "#VALUE";

   String fieldNameTarget() default "";

   String valueTarget() default DEFAULT_VALUE_TARGET;
}
