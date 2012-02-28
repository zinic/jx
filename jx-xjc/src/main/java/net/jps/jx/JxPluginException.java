package net.jps.jx;

/**
 *
 * @author zinic
 */
public class JxPluginException extends RuntimeException {

   public JxPluginException(String string, Throwable thrwbl) {
      super(string, thrwbl);
   }

   public JxPluginException(String string) {
      super(string);
   }
}
