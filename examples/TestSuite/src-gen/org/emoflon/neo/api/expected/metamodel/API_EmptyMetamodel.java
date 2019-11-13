/** 
 * EMSL-API generated by eMoflon::Neo - Do not edit as this file will be overwritten
 */
package org.emoflon.neo.api.expected.metamodel;

import org.emoflon.neo.neo4j.adapter.models.NeoCoreBuilder;
import org.emoflon.neo.neo4j.adapter.patterns.NeoMatch;
import org.emoflon.neo.neo4j.adapter.rules.NeoCoMatch;
import org.emoflon.neo.emsl.eMSL.EMSL_Spec;
import org.emoflon.neo.emsl.eMSL.Model;
import org.emoflon.neo.emsl.eMSL.Metamodel;
import org.emoflon.neo.emsl.util.EMSLUtil;
import org.emoflon.neo.emsl.util.FlattenerException;
import org.emoflon.neo.engine.api.patterns.IPattern;
import org.emoflon.neo.engine.api.rules.IRule;
import org.emoflon.neo.neo4j.adapter.rules.NeoRule;
import org.emoflon.neo.neo4j.adapter.rules.NeoRuleAccess;
import org.emoflon.neo.neo4j.adapter.patterns.NeoPattern;
import org.emoflon.neo.neo4j.adapter.patterns.NeoPatternFactory;
import org.emoflon.neo.emsl.eMSL.Pattern;
import org.emoflon.neo.emsl.eMSL.Rule;
import org.emoflon.neo.emsl.eMSL.TripleRule;
import org.emoflon.neo.neo4j.adapter.rules.NeoRuleFactory;
import org.emoflon.neo.neo4j.adapter.constraints.NeoConstraint;
import org.emoflon.neo.neo4j.adapter.constraints.NeoConstraintFactory;
import org.emoflon.neo.engine.api.constraints.IConstraint;
import org.emoflon.neo.emsl.eMSL.Constraint;
import org.neo4j.driver.v1.Value;
import org.emoflon.neo.neo4j.adapter.patterns.NeoPatternAccess;
import org.emoflon.neo.neo4j.adapter.patterns.NeoMask;
import org.emoflon.neo.neo4j.adapter.patterns.NeoData;
import org.emoflon.neo.api.API_Common;
import java.util.Collection;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.time.LocalDate;

@SuppressWarnings("unused")
public class API_EmptyMetamodel {
	private EMSL_Spec spec;
	private NeoCoreBuilder builder;

	/** Use this constructor for default values */
	public API_EmptyMetamodel(NeoCoreBuilder builder) {
		this(builder, API_Common.PLATFORM_RESOURCE_URI, API_Common.PLATFORM_PLUGIN_URI);
	}

	/** Use this constructor to configure values for loading EMSL files */
	public API_EmptyMetamodel(NeoCoreBuilder builder, String platformResourceURIRoot, String platformPluginURIRoot){
		spec = (EMSL_Spec) EMSLUtil.loadSpecification("platform:/resource/TestSuite/resources/expected/metamodel/EmptyMetamodel.msl", platformResourceURIRoot, platformPluginURIRoot);
		this.builder = builder;
	}

	//:~> platform:/resource/TestSuite/resources/expected/metamodel/EmptyMetamodel.msl#//@entities.0
	public Metamodel getMetamodel_EmptyMetamodel(){
		return (Metamodel) spec.getEntities().get(0);
	}
}
