package org.emoflon.neo.emsl.util;

import java.util.ArrayList;

import org.emoflon.neo.emsl.eMSL.Entity;
import org.emoflon.neo.emsl.eMSL.ModelNodeBlock;
import org.emoflon.neo.emsl.eMSL.ModelPropertyStatement;
import org.emoflon.neo.emsl.eMSL.ModelRelationStatement;
import org.emoflon.neo.emsl.eMSL.SuperType;

public class FlattenerException extends Exception {
	private static final long serialVersionUID = -9007303619385845276L;
	private Entity entity;
	private SuperType superEntity;

	private FlattenerErrorType errorType;

	private ArrayList<String> alreadyRefinedPatternNames;

	private ModelPropertyStatement property1;
	private ModelPropertyStatement property2;

	private ModelNodeBlock nodeBlock;
	
	private ModelRelationStatement relation;

	/**
	 * Constructor for the case of a detected infinite loop.
	 * 
	 * @param entity                     that was to be flattened and caused the
	 *                                   exception.
	 * @param type                       of error that occurred.
	 * @param alreadyRefinedPatternNames names of patterns that were already
	 *                                   defined.
	 */
	public FlattenerException(Entity entity, FlattenerErrorType type, ArrayList<String> alreadyRefinedPatternNames) {
		this.entity = entity;
		this.errorType = type;
		this.alreadyRefinedPatternNames = alreadyRefinedPatternNames;
	}
	
	// very basic constructor, used for ModelRelationStatement path limits
	public FlattenerException(Entity entity, FlattenerErrorType type) {
		this.entity = entity;
		this.errorType = type;
	}

	// for not mergeable property statements
	public FlattenerException(Entity entity, FlattenerErrorType type, ModelPropertyStatement property1,
			ModelPropertyStatement property2, SuperType superEntity) {
		this.entity = entity;
		this.errorType = type;
		this.property1 = property1;
		this.property2 = property2;
		this.superEntity = superEntity;
	}

	// for not mergeable nodeblocks
	public FlattenerException(Entity entity, FlattenerErrorType type, ModelNodeBlock nodeBlock) {
		this.entity = entity;
		this.errorType = type;
		this.nodeBlock = nodeBlock;
	}

	// for superEntities with when condition
	public FlattenerException(Entity entity, FlattenerErrorType type, SuperType superEntity) {
		this.entity = entity;
		this.errorType = type;
		this.superEntity = superEntity;
	}
	
	// for non-resolvable proxies of relation statements
	public FlattenerException(Entity entity, FlattenerErrorType type, ModelRelationStatement relation) {
		this.entity = entity;
		this.errorType = type;
		this.relation = relation;
	}

	public ModelPropertyStatement getProperty1() {
		return property1;
	}

	public ModelPropertyStatement getProperty2() {
		return property2;
	}

	public FlattenerErrorType getErrorType() {
		return errorType;
	}

	public Entity getEntity() {
		return entity;
	}

	public ArrayList<String> getAlreadyRefinedPatternNames() {
		return alreadyRefinedPatternNames;
	}

	public ModelNodeBlock getNodeBlock() {
		return nodeBlock;
	}

	public SuperType getSuperEntity() {
		return superEntity;
	}
	
	public ModelRelationStatement getRelation() {
		return relation;
	}

}
