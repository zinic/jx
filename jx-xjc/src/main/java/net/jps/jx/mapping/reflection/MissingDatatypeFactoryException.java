package net.jps.jx.mapping.reflection;

/**
 *
 * @author zinic
 */
public class MissingDatatypeFactoryException extends RuntimeException {

   public MissingDatatypeFactoryException(String string, Throwable thrwbl) {
      super(string, thrwbl);
   }
}
