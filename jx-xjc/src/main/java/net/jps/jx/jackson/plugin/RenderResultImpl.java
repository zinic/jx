package net.jps.jx.jackson.plugin;

import java.util.LinkedList;
import java.util.List;
import net.jps.jx.jackson.plugin.operation.RenderAction;

/**
 *
 * @author zinic
 */
public class RenderResultImpl implements RenderResult {

   private final List<RenderAction> renderActions;
   private RenderChainAction chainAction;

   public RenderResultImpl(RenderChainAction requestedAction) {
      this(requestedAction, new LinkedList<RenderAction>());
   }

   public RenderResultImpl(RenderChainAction requestedAction, List<RenderAction> renderActions) {
      this.chainAction = requestedAction;
      this.renderActions = renderActions;
   }

   public void setChainAction(RenderChainAction newAction) {
      chainAction = newAction;
   }

   @Override
   public RenderChainAction chainAction() {
      return chainAction;
   }

   @Override
   public List<RenderAction> renderActions() {
      return renderActions;
   }

   public void addAction(RenderAction renderAction) {
      renderActions.add(renderAction);
   }

   @Override
   public boolean hasRenderActions() {
      return renderActions != null && !renderActions.isEmpty();
   }
}
