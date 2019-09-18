package org.emoflon.neo.engine.modules.updatepolicies;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.emoflon.neo.engine.api.patterns.IMatch;
import org.emoflon.neo.engine.api.rules.IRule;
import org.emoflon.neo.engine.modules.ilp.ILPBasedOperationalStrategy;
import org.emoflon.neo.engine.modules.ilp.ILPFactory;
import org.emoflon.neo.engine.modules.ilp.ILPFactory.SupportedILPSolver;
import org.emoflon.neo.neo4j.adapter.patterns.NeoMatch;
import org.emoflon.neo.neo4j.adapter.rules.NeoCoMatch;

public class CheckOnlyOperationalStrategy extends ILPBasedOperationalStrategy {

	private static final Logger logger = Logger.getLogger(CheckOnlyOperationalStrategy.class);
	
	public CheckOnlyOperationalStrategy(Collection<IRule<NeoMatch, NeoCoMatch>> genRules) {
		super(genRules);
	}

	@Override
	protected Set<Long> getContextElts(IMatch m) {
		var genRule = genRules.get(m.getPattern().getName());
		return genRule.getContextElts()//
				.map(name -> m.getIDs().get(name))//
				.filter(elt -> elt != null)//
				.collect(Collectors.toSet());
	}

	@Override
	protected Set<Long> getCreatedElts(IMatch m) {
		var genRule = genRules.get(m.getPattern().getName());
		return genRule.getCreatedElts()//
				.map(name -> m.getIDs().get(name))//
				.filter(elt -> elt != null)//
				.collect(Collectors.toSet());
	}

	public Set<Long> determineConsistentElements(SupportedILPSolver suppSolver) throws Exception {
		var solver = ILPFactory.createILPSolver(getILPProblem(), suppSolver);
		
		logger.debug(getILPProblem());
		
		var solution = solver.solveILP();

		return matchToId.entrySet().stream()//
				.filter(entry -> solution.getVariable(entry.getValue()) > 0)
				.flatMap(entry -> getCreatedElts(entry.getKey()).stream()).collect(Collectors.toSet());
	}
	
	public ArrayList<Long> determineInconsistentElements(SupportedILPSolver suppSolver) throws Exception {
		var consistentElements = determineConsistentElements(suppSolver);
		var allElements = new ArrayList<>(elementToCreatingMatches.keySet());
		allElements.removeAll(consistentElements);
		return allElements;
	}
}
