package org.emoflon.neo.neo4j.adapter;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.emoflon.neo.emsl.eMSL.AtomicPattern;
import org.emoflon.neo.emsl.eMSL.ModelNodeBlock;
import org.emoflon.neo.emsl.eMSL.ModelRelationStatement;
import org.emoflon.neo.engine.api.constraints.IPositiveConstraint;
import org.emoflon.neo.engine.api.rules.IMatch;

public class NeoImplication implements IPositiveConstraint {

	private static final Logger logger = Logger.getLogger(NeoCoreBuilder.class);
	private NeoCoreBuilder builder;

	private AtomicPattern apIf;
	private AtomicPattern apThen;
	private NeoPattern pIf;
	private NeoPattern pThen;
	private String name;
	
	private Collection<String> nodesMap;

	public NeoImplication(AtomicPattern apIf, AtomicPattern apThen, NeoCoreBuilder builder) {
		this.builder = builder;
		this.apIf = apIf;
		this.apThen = apThen;
		this.name = "IF " + apIf.getName() + " THEN " + apThen.getName();
		this.pIf = new NeoPattern(apIf, builder);
		this.pThen = new NeoPattern(apThen, builder);
		this.nodesMap = new ArrayList<String>();
		createNodesMap();
	}

	private void createNodesMap() {

		
		for(NeoNode n: pIf.getNodes()) {
			nodesMap.add(n.getVarName());
			for(NeoRelation r: n.getRelations()) {
				nodesMap.add(r.getVarName());
			}
		}
		for(NeoNode n: pThen.getNodes()) {
			if(!nodesMap.contains(n.getVarName())) {
				nodesMap.add(n.getVarName());
			}
			for(NeoRelation r: n.getRelations()) {
				if(!nodesMap.contains(r.getVarName())) {
					nodesMap.add(r.getVarName());
				}
			}
		}
		
	}

	public String getName() {
		return name;
	}

	public AtomicPattern getIfPattern() {
		return apIf;
	}

	public AtomicPattern getThenPattern() {
		return apThen;
	}

	@Override
	public boolean isSatisfied() {

		if (getMatch() == null)
			return true;
		else
			return false;

	}

	@Override
	public IMatch getMatch() {

		logger.info("Check constraint: " + name);

		var cypherQuery = CypherPatternBuilder.readQuery(pIf.getNodes(), pThen.getNodes(), nodesMap, true);
		logger.debug(cypherQuery);

		var result = builder.executeQuery(cypherQuery);

		var matches = new ArrayList<IMatch>();
		while (result.hasNext()) {
			var record = result.next();
			matches.add(new NeoMatch(pThen, record));
			var recMap = record.asMap();

			for (var n : pThen.getNodes()) {
				if (recMap.containsKey(n.getVarName())) {
					if (recMap.get(n.getVarName()) == null) {
						logger.info("Invalid match found. Constraint: " + name + " is NOT complied!");
						return matches.get(0);
					}
				}
				for (var r : n.getRelations()) {
					if (recMap.containsKey(r.getVarName())) {
						if (recMap.get(r.getVarName()) == null) {
							logger.info("Invalid match found. Constraint: " + name + " is NOT complied!");
							return matches.get(0);
						}
					}
				}
			}
		}

		logger.info("No invalid matches found. Constraint: " + name + " is complied!");
		return null;

	}

}
