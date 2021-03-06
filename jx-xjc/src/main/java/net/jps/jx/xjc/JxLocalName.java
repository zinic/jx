package net.jps.jx.xjc;

/**
 *
 * @author zinic
 */
public enum JxLocalName {

   BINDING_STRATEGY("bind");

   public static boolean isLocalName(String localName) {
      return getMatchingLocalName(localName) != null;
   }

   public static JxLocalName getMatchingLocalName(String localName) {
      for (JxLocalName ln : values()) {
         if (ln.toString().equals(localName)) {
            return ln;
         }
      }

      return null;
   }
   
   private final String localName;

   private JxLocalName(String localName) {
      this.localName = localName;
   }

   @Override
   public String toString() {
      return localName;
   }
}
