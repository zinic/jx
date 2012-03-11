package net.jps.jx.util.reflection;

/**
 *
 * @author zinic
 */
public class ReflectionException extends RuntimeException {

   public ReflectionException(String string) {
      super(string);
   }

   public ReflectionException(String string, Throwable thrwbl) {
      super(string, thrwbl);
   }
}
