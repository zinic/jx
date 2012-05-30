package net.jps.jx.jackson.plugin;

import net.jps.jx.jackson.GraphNode;
import net.jps.jx.jackson.plugin.operation.RenderAction;
import net.jps.jx.jackson.plugin.operation.RenderOperation;

/**
 *
 * @author zinic
 */
public final class Results {

   private static final RenderResult EXIT_RESULT = new RenderResultImpl(RenderChainAction.EXIT, null);
   private static final RenderResult CONTINUE_RESULT = new RenderResultImpl(RenderChainAction.CONTINUE, null);

   public static RenderResult exitRenderChain() {
      return EXIT_RESULT;
   }

   public static RenderResult continueRenderChain() {
      return CONTINUE_RESULT;
   }

   public static RenderResult pushThenExit(GraphNode node) {
      final RenderResultImpl result = new RenderResultImpl(RenderChainAction.EXIT);
      result.addAction(new RenderAction(RenderOperation.PUSH, node));

      return result;
   }

   public static RenderResult deferNode(GraphNode node) {
      final RenderResultImpl result = new RenderResultImpl(RenderChainAction.CONTINUE);
      result.addAction(new RenderAction(RenderOperation.PUSH, node));

      return result;
   }

   private Results() {
   }
}
