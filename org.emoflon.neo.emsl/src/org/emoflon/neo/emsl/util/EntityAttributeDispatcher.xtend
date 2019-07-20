package org.emoflon.neo.emsl.util

import org.emoflon.neo.emsl.eMSL.Model
import org.emoflon.neo.emsl.eMSL.Metamodel
import org.emoflon.neo.emsl.eMSL.Pattern
import org.emoflon.neo.emsl.eMSL.Rule
import org.emoflon.neo.emsl.eMSL.TripleRule
import org.emoflon.neo.emsl.eMSL.Constraint
import org.emoflon.neo.emsl.eMSL.TripleGrammar
import org.emoflon.neo.emsl.eMSL.GraphGrammar
import org.emoflon.neo.emsl.eMSL.AtomicPattern
import org.emoflon.neo.emsl.eMSL.SuperType
import java.util.ArrayList
import org.emoflon.neo.emsl.eMSL.AttributeCondition

class EntityAttributeDispatcher {
	
	/*-----------------------------------------*/
	/*------ Get SuperRefinementTypes ---------*/
	/*-----------------------------------------*/

	def dispatch getSuperRefinementTypes(Model entity) {
		entity.superRefinementTypes
	}
	
	def dispatch getSuperRefinementTypes(Metamodel entity) {
		entity.superRefinementTypes
	}
	
	def dispatch getSuperRefinementTypes(Pattern entity) {
		entity.body.superRefinementTypes
	}
	
	def dispatch getSuperRefinementTypes(Rule entity) {
		entity.superRefinementTypes
	}
	
	def dispatch getSuperRefinementTypes(TripleRule entity) {
		entity.superRefinementTypes
	}
	
	def dispatch getSuperRefinementTypes(Constraint entity) {
		return newArrayList
	}
	
	def dispatch getSuperRefinementTypes(TripleGrammar entity) {
		return newArrayList
	}
	
	def dispatch getSuperRefinementTypes(GraphGrammar entity) {
		return newArrayList
	}
	
	
	/*------------------------------*/
	/*---------- Get Names ---------*/
	/*------------------------------*/
	
	def dispatch getName(Model entity) {
		entity.name
	}
	
	def dispatch getName(Metamodel entity) {
		entity.name
	}
	
	def dispatch getName(Pattern entity) {
		entity.body.name
	}
	
	def dispatch getName(Rule entity) {
		entity.name
	}
	
	def dispatch getName(TripleRule entity) {
		entity.name
	}
	
	def dispatch getName(AtomicPattern entity) {
		entity.name
	}
	
	def dispatch getName(Constraint entity) {
		entity.name
	}
	
	def dispatch getName(TripleGrammar entity) {
		entity.name
	}
	
	def dispatch getName(GraphGrammar entity) {
		entity.name
	}
	
	def getSuperTypeName(SuperType entity) {
		if (entity instanceof Pattern) {
			return entity.body.name
		} else {
			return entity.name
		}
	}
	
	
	/*----------------------------------*/
	/*--------- Get NodeBlocks ---------*/
	/*----------------------------------*/
	/**
	 * Returns all NodeBlocks of a Model.
	 */
	def dispatch getNodeBlocks(Model entity) {
		entity.nodeBlocks
	}

	/**
	 * Returns all NodeBlocks of a Pattern.
	 */
	def dispatch getNodeBlocks(Pattern entity) {
		entity.body.nodeBlocks
	}

	/**
	 * Returns all NodeBlocks of a Rule.
	 */
	def dispatch getNodeBlocks(Rule entity) {
		entity.nodeBlocks
	}
	
	/*-------------------------------------------*/
	/*--------- Get AttributeConditions ---------*/
	/*-------------------------------------------*/
	
	def dispatch getAttributeConditions(Pattern entity) {
		entity.body.attributeConditions
	}
	
	def dispatch getAttributeConditions(Rule entity) {
		entity.attributeConditions
	}
	
	def dispatch getAttributeConditions(TripleRule entity) {
		entity.attributeConditions
	}
	
	def dispatch getAttributeConditions(Model entity) {
		new ArrayList<AttributeCondition>()
	}
	
	/*-------------------------------------------*/
	/*-------------- Get Abstract ---------------*/
	/*-------------------------------------------*/
	
	def dispatch getAbstract(Model entity) {
		entity.abstract
	}
	
	def dispatch getAbstract(Metamodel entity) {
		entity.abstract
	}
	
	def dispatch getAbstract(Pattern entity) {
		entity.body.abstract
	}
	
	def dispatch getAbstract(Rule entity) {
		entity.abstract
	}
	
	def dispatch getAbstract(TripleRule entity) {
		entity.abstract
	}
	
	def dispatch getAbstract(TripleGrammar entity) {
		entity.abstract
	}
	
	def dispatch getAbstract(GraphGrammar entity) {
		entity.abstract
	}
	
	def dispatch getAbstract(Constraint entity) {
		entity.abstract
	}
}