package net.jps.jx;

/**
 *
 * @author zinic
 */
public class JxWritingException extends Exception {

   public JxWritingException(String string, Throwable thrwbl) {
      super(string, thrwbl);
   }

   public JxWritingException(String string) {
      super(string);
   }
}
