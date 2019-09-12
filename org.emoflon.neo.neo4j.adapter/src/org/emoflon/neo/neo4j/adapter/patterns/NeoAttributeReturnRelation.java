package org.emoflon.neo.neo4j.adapter.patterns;

import java.util.ArrayList;

import org.emoflon.neo.emsl.eMSL.ModelPropertyStatement;

public class NeoAttributeReturnRelation {
	
	private ArrayList<ModelPropertyStatement> elemProps;
	private ArrayList<NeoAttributeExpression> elemAttrExpr;
	private ArrayList<NeoAttributeExpression> elemAttrAsgn;
	
	public ArrayList<ModelPropertyStatement> getElemProps() {
		return elemProps;
	}

	public ArrayList<NeoAttributeExpression> getElemAttrExpr() {
		return elemAttrExpr;
	}

	public ArrayList<NeoAttributeExpression> getElemAttrAsgn() {
		return elemAttrAsgn;
	}

	public NeoAttributeReturnRelation(ArrayList<ModelPropertyStatement> elemProps, ArrayList<NeoAttributeExpression> elemAttrExpr,
			ArrayList<NeoAttributeExpression> elemAttrAsgn) {
		super();
		this.elemProps = elemProps;
		this.elemAttrExpr = elemAttrExpr;
		this.elemAttrAsgn = elemAttrAsgn;
	}

}
