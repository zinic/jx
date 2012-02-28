package net.jps.jx;

/**
 *
 * @author zinic
 */
public enum JxLocalName {
   
   OBJECT_WRAP("wrap"),
   ELEMENT_VALUE_NAME("value-name");
   
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
