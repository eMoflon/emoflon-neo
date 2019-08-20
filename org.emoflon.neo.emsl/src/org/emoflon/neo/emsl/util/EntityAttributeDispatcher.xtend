package org.emoflon.neo.emsl.util

import java.util.ArrayList
import java.util.List
import org.emoflon.neo.emsl.eMSL.AtomicPattern
import org.emoflon.neo.emsl.eMSL.Constraint
import org.emoflon.neo.emsl.eMSL.GraphGrammar
import org.emoflon.neo.emsl.eMSL.Metamodel
import org.emoflon.neo.emsl.eMSL.Model
import org.emoflon.neo.emsl.eMSL.Pattern
import org.emoflon.neo.emsl.eMSL.RefinementCommand
import org.emoflon.neo.emsl.eMSL.Rule
import org.emoflon.neo.emsl.eMSL.SuperType
import org.emoflon.neo.emsl.eMSL.TripleGrammar
import org.emoflon.neo.emsl.eMSL.TripleRule

class EntityAttributeDispatcher {
	
	/*-----------------------------------------*/
	/*------ Get SuperRefinementTypes ---------*/
	/*-----------------------------------------*/

	def List<RefinementCommand> getSuperRefinementTypes(SuperType st){
		st.superRefinementTypes
	}
	
	def List<RefinementCommand> getSuperRefinementTypes(Pattern p){
		p.body.superRefinementTypes
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

	def dispatch getNodeBlocks(TripleRule entity) {
		var nodeBlocks = new ArrayList
		nodeBlocks.addAll(entity.srcNodeBlocks)
		nodeBlocks.addAll(entity.trgNodeBlocks)
		return nodeBlocks
	}

	/**
	 * Returns all NodeBlocks of a Metamodel.
	 */
	def getMetamodelNodeBlocks(Metamodel entity) {
		entity.nodeBlocks
	}

	/**
	 * Returns all NodeBlocks of a Pattern.
	 */
	def getPatternNodeBlocks(Pattern entity) {
		entity.body.nodeBlocks
	}
	
	/**
	 * Returns all NodeBlocks of an AtomicPattern.
	 */
	def dispatch getNodeBlocks(AtomicPattern entity) {
		entity.nodeBlocks
	}

	/**
	 * Returns all NodeBlocks of a Rule.
	 */
	def dispatch getNodeBlocks(Rule entity) {
		entity.nodeBlocks
	}
	
	/*-------------------------------------------*/
	/*-------------- Get Abstract ---------------*/
	/*-------------------------------------------*/
	
	def dispatch getAbstract(Model entity) {
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