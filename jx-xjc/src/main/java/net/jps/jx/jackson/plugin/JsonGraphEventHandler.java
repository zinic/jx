package net.jps.jx.jackson.plugin;

import net.jps.jx.JxParsingException;
import net.jps.jx.jackson.GraphNode;
import net.jps.jx.mapping.FieldDescriptor;

/**
 * Messy messy...
 * 
 * @author zinic
 */
public interface JsonGraphEventHandler {

   RenderResult selectField(GraphNode currentGraphNode, String fieldName) throws JxParsingException;

   RenderResult startObject(GraphNode currentGraphNode) throws JxParsingException;

   RenderResult endObject(GraphNode currentGraphNode) throws JxParsingException;

   RenderResult startArray(GraphNode currentGraphNode) throws JxParsingException;

   RenderResult endArray(GraphNode currentGraphNode) throws JxParsingException;

   RenderResult setObject(GraphNode currentGraphNode, Object object) throws JxParsingException;

   RenderResult setNumber(GraphNode currentGraphNode, Number number) throws JxParsingException;
}
