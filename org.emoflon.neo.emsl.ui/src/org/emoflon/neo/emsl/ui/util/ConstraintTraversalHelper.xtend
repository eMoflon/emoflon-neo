package org.emoflon.neo.emsl.ui.util

import java.util.ArrayList
import java.util.List
import org.emoflon.neo.emsl.eMSL.AndBody
import org.emoflon.neo.emsl.eMSL.AtomicPattern
import org.emoflon.neo.emsl.eMSL.Constraint
import org.emoflon.neo.emsl.eMSL.ConstraintBody
import org.emoflon.neo.emsl.eMSL.ConstraintReference
import org.emoflon.neo.emsl.eMSL.Entity
import org.emoflon.neo.emsl.eMSL.Implication
import org.emoflon.neo.emsl.eMSL.NegativeConstraint
import org.emoflon.neo.emsl.eMSL.OrBody
import org.emoflon.neo.emsl.eMSL.Pattern
import org.emoflon.neo.emsl.eMSL.PositiveConstraint
import org.emoflon.neo.emsl.eMSL.Rule

class ConstraintTraversalHelper {

	ArrayList<AtomicPattern> patternList = newArrayList

	def getConstraintPattern(Entity entity) {
		getConditionString(entity)
		return patternList
	}
	
	def getConditionString(Entity entity) {
		if (entity instanceof Rule) {
			
			if (entity.condition instanceof NegativeConstraint || entity.condition instanceof PositiveConstraint || entity.condition instanceof Implication)
				getSimpleConstraintString(entity.condition as ConstraintBody)
			
			if (entity.condition instanceof ConstraintReference)
				getConstraintReferenceString((entity.condition as ConstraintReference))
		}
		else if (entity instanceof Pattern) {
			
			if (entity.condition instanceof NegativeConstraint || entity.condition instanceof PositiveConstraint || entity.condition instanceof Implication)
				getSimpleConstraintString(entity.condition as ConstraintBody)
			
			if (entity.condition instanceof ConstraintReference)
				getConstraintReferenceString((entity.condition as ConstraintReference))
		}
		else if (entity instanceof Constraint) {
			
			if (entity.body instanceof NegativeConstraint || entity.body instanceof PositiveConstraint || entity.body instanceof Implication)
				getSimpleConstraintString(entity.body as ConstraintBody)
			
			else
				getOrBodyString(entity.body)
		}
	}
	
	def void getConstraintReferenceString(ConstraintReference constraint) {
		if (constraint.reference.body instanceof NegativeConstraint || constraint.reference.body instanceof PositiveConstraint || constraint.reference.body instanceof Implication)
			getSimpleConstraintString(constraint.reference.body)
		// OrBody
		else  {
			if (constraint.reference.body !== null) {
				for (c : constraint.reference.body.children)
					getOrBodyString(c)		
			}
		}
	}
	
	def dispatch static List<? extends ConstraintBody> getChildren(AndBody body){
		return body.children
	}
	
	def dispatch static List<? extends ConstraintBody> getChildren(OrBody body){
		return body.children
	}
	
	def void getOrBodyString(ConstraintBody constraintBody) {
		for (c : constraintBody.children)
			getAndBodyString(c)
	}
	
	def getAndBodyString(ConstraintBody constraintBody) {
		if ((constraintBody instanceof ConstraintReference))
			getConstraintReferenceString(constraintBody)

		if (!(constraintBody instanceof ConstraintReference)) {
			for (c : constraintBody.children)
				getPrimaryString(c)
		}
	}
	
	def getPrimaryString(ConstraintBody constraintBody) {
		if (constraintBody instanceof ConstraintReference)
				getConstraintReferenceString(constraintBody)
		else if (constraintBody.children.size > 0) {
			for (c : constraintBody.children) {
				if (c instanceof ConstraintReference)
					getConstraintReferenceString((c as ConstraintReference))
				else
					getOrBodyString(constraintBody)
			}
		} else
			getOrBodyString(constraintBody)
	}
	
	def getSimpleConstraintString(ConstraintBody constraintBody) {
		if (constraintBody instanceof NegativeConstraint) {
			if (!patternList.contains((constraintBody as NegativeConstraint).pattern as AtomicPattern))
				patternList.add((constraintBody as NegativeConstraint).pattern as AtomicPattern)	
		}
		else if (constraintBody instanceof PositiveConstraint) {
			if (!patternList.contains((constraintBody as PositiveConstraint).pattern as AtomicPattern))
				patternList.add((constraintBody as PositiveConstraint).pattern as AtomicPattern)
		}
		else if (constraintBody instanceof Implication) {
			if (!patternList.contains((constraintBody as Implication).premise as AtomicPattern))
				patternList.add((constraintBody as Implication).premise as AtomicPattern)
			if (!patternList.contains((constraintBody as Implication).conclusion as AtomicPattern))
				patternList.add((constraintBody as Implication).conclusion as AtomicPattern)	
		}
	}

}
