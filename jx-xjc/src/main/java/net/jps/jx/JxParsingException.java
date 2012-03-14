package net.jps.jx;

/**
 *
 * @author zinic
 */
public class JxParsingException extends Exception {

   public JxParsingException(String string, Throwable thrwbl) {
      super(string, thrwbl);
   }

   public JxParsingException(String string) {
      super(string);
   }
}
