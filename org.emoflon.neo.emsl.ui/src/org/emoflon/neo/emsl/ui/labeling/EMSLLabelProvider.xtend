/*
 * generated by Xtext 2.16.0
 */
package org.emoflon.neo.emsl.ui.labeling

import com.google.inject.Inject
import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider
import org.eclipse.jface.resource.JFaceResources
import org.eclipse.jface.viewers.StyledString
import org.eclipse.jface.viewers.StyledString.Styler
import org.eclipse.swt.graphics.TextStyle
import org.eclipse.xtext.ui.label.DefaultEObjectLabelProvider
import org.emoflon.neo.emsl.eMSL.AtomicPattern
import org.emoflon.neo.emsl.eMSL.AttributeExpression
import org.emoflon.neo.emsl.eMSL.Constraint
import org.emoflon.neo.emsl.eMSL.Correspondence
import org.emoflon.neo.emsl.eMSL.CorrespondenceType
import org.emoflon.neo.emsl.eMSL.EnumValue
import org.emoflon.neo.emsl.eMSL.LinkAttributeExpTarget
import org.emoflon.neo.emsl.eMSL.Metamodel
import org.emoflon.neo.emsl.eMSL.MetamodelNodeBlock
import org.emoflon.neo.emsl.eMSL.MetamodelPropertyStatement
import org.emoflon.neo.emsl.eMSL.MetamodelRelationStatement
import org.emoflon.neo.emsl.eMSL.Model
import org.emoflon.neo.emsl.eMSL.ModelNodeBlock
import org.emoflon.neo.emsl.eMSL.ModelPropertyStatement
import org.emoflon.neo.emsl.eMSL.ModelRelationStatement
import org.emoflon.neo.emsl.eMSL.NodeAttributeExpTarget
import org.emoflon.neo.emsl.eMSL.PrimitiveBoolean
import org.emoflon.neo.emsl.eMSL.PrimitiveInt
import org.emoflon.neo.emsl.eMSL.PrimitiveString
import org.emoflon.neo.emsl.eMSL.Rule
import org.emoflon.neo.emsl.eMSL.TripleGrammar
import org.emoflon.neo.emsl.eMSL.TripleRule
import org.emoflon.neo.emsl.util.EMSLUtil
import org.emoflon.neo.emsl.eMSL.PrimitiveDouble
import org.emoflon.neo.emsl.eMSL.BinaryExpression
import org.emoflon.neo.emsl.eMSL.Parameter

/**
 * Provides labels for EObjects.
 * 
 * See https://www.eclipse.org/Xtext/documentation/304_ide_concepts.html#label-provider
 */
class EMSLLabelProvider extends DefaultEObjectLabelProvider {

	@Inject
	new(AdapterFactoryLabelProvider delegate) {
		super(delegate);
	}

	def image(Metamodel m) {
		'metamodel.gif'
	}
	
	protected def setItalicStyle(String label) {
		val ss = new StyledString(label)
		ss.setStyle(0, ss.length, new Styler(){
			override applyStyles(TextStyle textStyle) {
				textStyle.font = JFaceResources.fontRegistry.getItalic(JFaceResources.DEFAULT_FONT)
			}
		})
		
		return ss
	}
	
	def image(AtomicPattern p){
		'gt-pattern.gif'
	}
	
	def text(AtomicPattern p){
		if(p.isAbstract)
			setItalicStyle(p.name)
	}
	
	def image(Constraint c){
		'gt-condition.gif'
	}
	
	def image(Rule r){
		'gt-rule.gif'
	}
	
	def text(Rule r){
		if(r.isAbstract)
			setItalicStyle(r.name)
	}
	
	def image(Model m){
		'model.gif'
	}
	
	def text(Model m){
		if(m.isAbstract)
			setItalicStyle(m.name)
	}
	
	def image(ModelNodeBlock n){
		'node.gif'
	}
	
	def image(MetamodelNodeBlock n){
		'node.gif'
	}
	
	def image(ModelRelationStatement r){
		'edge.gif'
	}
	
	def text(ModelRelationStatement r){
		r.target.name
	}
	
	def image(MetamodelRelationStatement r){
		'edge.gif'
	}
	
	def image(MetamodelPropertyStatement p) {
		'prop.gif'
	}
	
	def image(ModelPropertyStatement p) {
		'prop.gif'
	}
	
	def text(ModelPropertyStatement p){
		EMSLUtil.getNameOfType(p) + " " + p?.op + " " + p?.value?.print
	}
	
	dispatch def String print(Parameter value){
		'''<«value.name»>'''
	}
	
	dispatch def String print(PrimitiveDouble value){
		if(value === null)
			return "?"
		
		value.literal.toString
	}
	
	dispatch def String print(BinaryExpression value){
		if(value === null)
			return "?"
			
		print(value?.left) + " " + print(value?.op) + " " + print(value?.right)
	}
		
	dispatch def String print(PrimitiveInt value){
		if(value === null)
			return "?"
		
		value.literal.toString
	}
	
	dispatch def String print(PrimitiveString value){
		value.literal.toString
	}
	
	dispatch def String print(PrimitiveBoolean value){
		value.^true.toString
	}
	
	dispatch def String print(EnumValue value){
		value.literal.name
	}
	
	dispatch def String print(AttributeExpression exp){
		exp.node.name + "." + print(exp.target)
	}
	
	dispatch def String print(NodeAttributeExpTarget trg){
		trg.attribute.name
	}
	
	dispatch def String print(LinkAttributeExpTarget trg){
		trg.attribute.name
	}
	
	def image(TripleRule r){
		'tgg-rule.gif'
	}
	
	def image(Correspondence c){
		'corr.gif'
	}
	
	def text(Correspondence c){
		c.source.name + "<->" + c.target.name
	}
	
	def image(CorrespondenceType t){
		'corr-type.gif'
	}
	
	def image(TripleGrammar tgg){
		'tgg.gif'
	}
}
