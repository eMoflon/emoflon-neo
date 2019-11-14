package org.emoflon.neo.engine.api.patterns;

public interface IMatch {
	IPattern<?> getPattern();
	
	long[] getNodeIDs();
	long[] getRelIDs();
	
	boolean containsNode(String nodeName);
	boolean containsRel(String relName);
	
	long getNodeIDFor(String nodeName);
	long getRelIDFor(String relName);
	
	String getNameOfNode(long nodeID);
	String getNameOfRel(long relID);
}
