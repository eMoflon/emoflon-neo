/** 
 * EMSL-API generated by eMoflon::Neo - Do not edit as this file will be overwritten
 */
package org.emoflon.neo.api;

import org.emoflon.neo.neo4j.adapter.NeoCoreBuilder;
import org.emoflon.neo.emsl.eMSL.EMSL_Spec;
import org.emoflon.neo.emsl.eMSL.Model;
import org.emoflon.neo.emsl.eMSL.Metamodel;
import org.emoflon.neo.emsl.util.EMSLUtil;
import org.emoflon.neo.engine.api.rules.IPattern;
import org.emoflon.neo.neo4j.adapter.NeoPattern;
import org.emoflon.neo.emsl.eMSL.Pattern;
import org.emoflon.neo.neo4j.adapter.NeoConstraint;
import org.emoflon.neo.engine.api.constraints.IConstraint;
import org.emoflon.neo.emsl.eMSL.Constraint;

@SuppressWarnings("unused")
public class API_Src_Organigram {
	private EMSL_Spec spec;
	private NeoCoreBuilder builder;

	public API_Src_Organigram(NeoCoreBuilder builder, String platformURIRoot){
		spec = (EMSL_Spec) EMSLUtil.loadSpecification("platform:/resource/Organigram/src/Organigram.msl", platformURIRoot);
		this.builder = builder;
	}
	
	public API_Src_Organigram(NeoCoreBuilder builder){
		this(builder, "../");
	}

	//:~> platform:/resource/Organigram/src/Organigram.msl#//@entities.0
	public Metamodel getMetamodel_Company(){
		return (Metamodel) spec.getEntities().get(0);
	}
	
	//:~> platform:/resource/Organigram/src/Organigram.msl#//@entities.1
	public Model getModel_SimpleCompany(){
		return (Model) spec.getEntities().get(1);
	}
	
	//:~> platform:/resource/Organigram/src/Organigram.msl#//@entities.2
	public IPattern getPattern_IsEmployed(){
		var p = (Pattern) spec.getEntities().get(2);
		return new NeoPattern(p, builder);
	}
	
	//:~> platform:/resource/Organigram/src/Organigram.msl#//@entities.3
	public IPattern getPattern_ManagersMustBeEmployed(){
		var p = (Pattern) spec.getEntities().get(3);
		return new NeoPattern(p, builder);
	}
	
	//:~> platform:/resource/Organigram/src/Organigram.msl#//@entities.4
	public IPattern getPattern_CeoIsEmployed(){
		var p = (Pattern) spec.getEntities().get(4);
		return new NeoPattern(p, builder);
	}
	
	//:~> platform:/resource/Organigram/src/Organigram.msl#//@entities.5
	public IPattern getPattern_CeoAndManagerEmployed(){
		var p = (Pattern) spec.getEntities().get(5);
		return new NeoPattern(p, builder);
	}
}