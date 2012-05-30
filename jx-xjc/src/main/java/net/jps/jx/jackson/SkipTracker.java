package net.jps.jx.jackson;

/**
 *
 * @author zinic
 */
public class SkipTracker {

   private int skipDepth;
   private boolean skipOnce;

   public SkipTracker() {
      skipDepth = 0;
   }

   public void skipOnce() {
      skipOnce = true;
   }
   
   public boolean shouldSkip() {
      if (skipOnce) {
         skipOnce = false;
         
         return true;
      }
      
      return skipDepth > 0;
   }

   public void descend() {
      skipDepth++;
   }

   public void ascend() {
      skipDepth--;
   }
}