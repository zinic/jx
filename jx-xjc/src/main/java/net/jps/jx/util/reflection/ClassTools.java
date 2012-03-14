package net.jps.jx.util.reflection;

/**
 *
 * @author zinic
 */
public final class ClassTools {

   private ClassTools() {
   }

   public static boolean classMatches(Class type, Class... classes) {
      return findMatchingClass(type, classes) != null;
   }

   public static boolean classIsAssignableTo(Class type, Class... classes) {
      return findClassAssignableTo(type, classes) != null;
   }

   public static Class findMatchingClass(Class type, Class... classes) {
      for (Class c : classes) {
         if (type.equals(c)) {
            return c;
         }
      }

      return null;
   }

   public static Class findClassAssignableTo(Class type, Class... classes) {
      for (Class c : classes) {
         if (c.isAssignableFrom(type)) {
            return c;
         }
      }

      return null;
   }
}
