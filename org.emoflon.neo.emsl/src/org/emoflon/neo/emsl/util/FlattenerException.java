package org.emoflon.neo.emsl.util;

import java.util.Collection;
import java.util.List;

import org.emoflon.neo.emsl.eMSL.Correspondence;
import org.emoflon.neo.emsl.eMSL.ModelNodeBlock;
import org.emoflon.neo.emsl.eMSL.ModelPropertyStatement;
import org.emoflon.neo.emsl.eMSL.ModelRelationStatement;
import org.emoflon.neo.emsl.eMSL.ModelRelationStatementType;
import org.emoflon.neo.emsl.eMSL.SuperType;

public class FlattenerException extends Exception {
	private static final long serialVersionUID = -9007303619385845276L;
	private SuperType entity;
	private SuperType superEntity;

	private FlattenerErrorType errorType;

	private Collection<String> alreadyRefinedPatternNames;

	private ModelPropertyStatement property1;
	private ModelPropertyStatement property2;

	private ModelNodeBlock nodeBlock;

	private ModelRelationStatement relation;
	private ModelRelationStatementType statementType;

	private List<?> elements;
	
	private String proxyName;
	private Correspondence corr;

	/**
	 * Constructor for the case of a detected infinite loop.
	 * 
	 * @param entity                     that was to be flattened and caused the
	 *                                   exception.
	 * @param type                       of error that occurred.
	 * @param alreadyRefinedPatternNames names of patterns that were already
	 *                                   defined.
	 */
	public FlattenerException(SuperType entity, FlattenerErrorType type,
			Collection<String> alreadyRefinedPatternNames) {
		this.entity = entity;
		this.errorType = type;
		this.alreadyRefinedPatternNames = alreadyRefinedPatternNames;
	}

	// for not mergeable complex edges
	public FlattenerException(SuperType entity, FlattenerErrorType type) {
		this.entity = entity;
		this.errorType = type;
	}

	// for not mergeable property statements
	public FlattenerException(SuperType entity, FlattenerErrorType type, ModelPropertyStatement property1,
			ModelPropertyStatement property2, SuperType superEntity) {
		this.entity = entity;
		this.errorType = type;
		this.property1 = property1;
		this.property2 = property2;
		this.superEntity = superEntity;
	}

	// for not mergeable nodeblocks
	public FlattenerException(SuperType entity, FlattenerErrorType type, ModelNodeBlock nodeBlock) {
		this.entity = entity;
		this.errorType = type;
		this.nodeBlock = nodeBlock;
	}

	// for superEntities with when condition
	public FlattenerException(SuperType entity, FlattenerErrorType type, SuperType superEntity) {
		this.entity = entity;
		this.errorType = type;
		this.superEntity = superEntity;
	}

	// for non-resolvable proxies of relation statements/relation path limits
	public FlattenerException(SuperType entity, FlattenerErrorType type, ModelRelationStatement relation) {
		this.entity = entity;
		this.errorType = type;
		this.relation = relation;
	}

	public FlattenerException(SuperType entity, FlattenerErrorType type, List<?> elements) {
		this.entity = entity;
		this.errorType = type;
		this.elements = elements;
	}

	public FlattenerException(SuperType entity, FlattenerErrorType nonResolvableCorrProxy, String proxySource, Correspondence corr) {
		this.entity = entity;
		this.errorType = nonResolvableCorrProxy;
		this.proxyName = proxySource;
		this.corr = corr;
	}

	public String getProxyName() {
		return proxyName;
	}

	public Correspondence getCorr() {
		return corr;
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

	public SuperType getEntity() {
		return entity;
	}

	public Collection<String> getAlreadyRefinedPatternNames() {
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

	public ModelRelationStatementType getStatementType() {
		return statementType;
	}

	public List<?> getElements() {
		return elements;
	}

}
