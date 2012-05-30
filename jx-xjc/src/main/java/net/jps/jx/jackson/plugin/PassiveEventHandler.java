package net.jps.jx.jackson.plugin;

import net.jps.jx.JxParsingException;
import net.jps.jx.jackson.GraphNode;
import net.jps.jx.mapping.FieldDescriptor;

/**
 *
 * @author zinic
 */
public class PassiveEventHandler implements JsonGraphEventHandler {

   public static GraphNode newGraphNode(GraphContext graphContext, GraphNode graphNode, FieldDescriptor field) {
      final Object presetFieldInstance = graphNode.get(field);
      final Object fieldInstance = presetFieldInstance == null ? graphContext.newInstance(field.getTypeDescriptor().getDescribedClass()) : presetFieldInstance;

      return new GraphNode(fieldInstance, field);
   }
   
   @Override
   public RenderResult endArray(GraphNode currentGraphNode) throws JxParsingException {
      return Results.deferNode(currentGraphNode);
   }

   @Override
   public RenderResult endObject(GraphNode currentGraphNode) throws JxParsingException {
      return Results.deferNode(currentGraphNode);
   }

   @Override
   public RenderResult selectField(GraphNode currentGraphNode, String fieldName) throws JxParsingException {
      return Results.deferNode(currentGraphNode);
   }

   @Override
   public RenderResult setNumber(GraphNode currentGraphNode, Number number) throws JxParsingException {
      return Results.deferNode(currentGraphNode);
   }

   @Override
   public RenderResult setObject(GraphNode currentGraphNode, Object object) throws JxParsingException {
      return Results.deferNode(currentGraphNode);
   }

   @Override
   public RenderResult startArray(GraphNode currentGraphNode) throws JxParsingException {
      return Results.deferNode(currentGraphNode);
   }

   @Override
   public RenderResult startObject(GraphNode currentGraphNode) throws JxParsingException {
      return Results.deferNode(currentGraphNode);
   }
}
