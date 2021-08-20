/** 
 * EMSL-API generated by eMoflon::Neo - Do not edit as this file will be overwritten
 */
package org.emoflon.neo.api.classinhhier2db.bin;

import org.emoflon.neo.cypher.common.*;
import org.emoflon.neo.cypher.constraints.*;
import org.emoflon.neo.cypher.factories.*;
import org.emoflon.neo.cypher.models.*;
import org.emoflon.neo.cypher.patterns.*;
import org.emoflon.neo.cypher.rules.*;
import org.emoflon.neo.engine.api.patterns.*;
import org.emoflon.neo.engine.api.constraints.*;
import org.emoflon.neo.engine.api.rules.*;
import org.emoflon.neo.emsl.eMSL.*;
import org.emoflon.neo.emsl.util.*;
import org.neo4j.driver.Value;
import org.neo4j.driver.Record;
import org.eclipse.emf.common.util.URI;
import org.emoflon.neo.api.classinhhier2db.API_Common;
import java.util.Collection;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import java.util.Optional;
import java.time.LocalDate;

@SuppressWarnings("unused")
public class API_ChessLanguage {
	private EMSL_Spec spec;
	private NeoCoreBuilder builder;

	/** Use this constructor for default values */
	public API_ChessLanguage(NeoCoreBuilder builder) {
		this(builder, API_Common.PLATFORM_RESOURCE_URI, API_Common.PLATFORM_PLUGIN_URI, API_Common.NEOCORE_URI_INSTALLED);
	}

	/** Use this constructor to configure values for loading EMSL files */
	public API_ChessLanguage(NeoCoreBuilder builder, String platformResourceURIRoot, String platformPluginURIRoot, String neocoreURI){
		this((EMSL_Spec) EMSLUtil.loadSpecification("platform:/resource/ClassInhHier2DB/Chess/bin/ChessLanguage.msl", platformResourceURIRoot, platformPluginURIRoot, neocoreURI), builder);
	}

	public API_ChessLanguage(EMSL_Spec spec, NeoCoreBuilder builder) {
		this.spec = spec;
		this.builder = builder;
	}

	//:~> platform:/resource/ClassInhHier2DB/Chess/bin/ChessLanguage.msl#//@entities.0
	public Metamodel getMetamodel_Chess(){
		return (Metamodel) spec.getEntities().get(0);
	}
	
	public static final String Chess__Board = "Chess__Board";
	public static final String Chess__Field = "Chess__Field";
	public static final String Chess__Figure = "Chess__Figure";
	public static final String Chess__King = "Chess__King";
	public static final String Chess__Queen = "Chess__Queen";
	public static final String Chess__Rook = "Chess__Rook";
	public static final String Chess__Bishop = "Chess__Bishop";
	public static final String Chess__Knight = "Chess__Knight";
	public static final String Chess__Pawn = "Chess__Pawn";
}
