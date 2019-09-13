package org.emoflon.neo.neo4j.adapter.patterns;

import java.util.ArrayList;

import org.emoflon.neo.neo4j.adapter.common.NeoProperty;

public class NeoAttributeReturn {
	
	private ArrayList<NeoProperty> elemProps;
	private ArrayList<NeoAttributeExpression> elemAttrExpr;
	private ArrayList<NeoAttributeExpression> elemAttrAsgn;
	
	public ArrayList<NeoProperty> getElemProps() {
		return elemProps;
	}

	public ArrayList<NeoAttributeExpression> getElemAttrExpr() {
		return elemAttrExpr;
	}

	public ArrayList<NeoAttributeExpression> getElemAttrAsgn() {
		return elemAttrAsgn;
	}

	public NeoAttributeReturn(ArrayList<NeoProperty> elemProps, ArrayList<NeoAttributeExpression> elemAttrExpr,
			ArrayList<NeoAttributeExpression> elemAttrAsgn) {
		super();
		this.elemProps = elemProps;
		this.elemAttrExpr = elemAttrExpr;
		this.elemAttrAsgn = elemAttrAsgn;
	}

}
