package org.emoflon.neo.cypher.patterns;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.emoflon.neo.cypher.common.NeoDatabaseException;
import org.emoflon.neo.cypher.models.IBuilder;
import org.emoflon.neo.emsl.eMSL.AndBody;
import org.emoflon.neo.emsl.eMSL.Condition;
import org.emoflon.neo.emsl.eMSL.ConstraintBody;
import org.emoflon.neo.emsl.eMSL.ConstraintReference;
import org.emoflon.neo.emsl.eMSL.Implication;
import org.emoflon.neo.emsl.eMSL.ModelNodeBlock;
import org.emoflon.neo.emsl.eMSL.NegativeConstraint;
import org.emoflon.neo.emsl.eMSL.OrBody;
import org.emoflon.neo.emsl.eMSL.Pattern;
import org.emoflon.neo.emsl.eMSL.PositiveConstraint;
import org.emoflon.neo.emsl.util.FlattenerException;
import org.emoflon.neo.engine.api.patterns.IMask;
import org.emoflon.neo.engine.api.patterns.IPattern;
import org.emoflon.neo.engine.generator.Schedule;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.StatementResult;

public class NeoPattern extends NeoBasicPattern implements IPattern<NeoMatch> {
	private static Logger logger = Logger.getLogger(NeoPattern.class);
	protected IBuilder builder;
	private List<NeoPredicatePattern> subPredicatePatterns;
	private List<NeoImplicationPattern> subImplicationPatterns;
	private List<NeoSubPattern> allSubPatterns;
	private String logicalExprForWhere = "TRUE";

	public NeoPattern(Pattern pattern, IBuilder builder, boolean isCoPattern) throws FlattenerException {
		this(pattern.getBody().getName(), pattern.getBody().getNodeBlocks(), builder, pattern.getCondition(),
				isCoPattern);
	}

	public NeoPattern(String name, Collection<ModelNodeBlock> nodeBlocks, IBuilder builder, Condition condition,
			boolean isCoPattern) {
		super(name, nodeBlocks, isCoPattern);

		this.builder = builder;
		subPredicatePatterns = new ArrayList<>();
		subImplicationPatterns = new ArrayList<>();
		allSubPatterns = new ArrayList<>();

		// Extract subpatterns
		if (condition != null && !isCoPattern)
			logicalExprForWhere = handleCondition(condition);

	}

	private String handleCondition(Condition condition) {
		if (condition instanceof ConstraintReference)
			return handleConstraintReference((ConstraintReference) condition);
		else if (condition instanceof ConstraintBody)
			return handleConstraintBody((ConstraintBody) condition);
		else
			throw new IllegalArgumentException("Don't know how to handle: " + condition);
	}

	private String handleConstraintBody(ConstraintBody condition) {
		if (condition instanceof NegativeConstraint) {
			return handleNegativeConstraint((NegativeConstraint) condition);
		} else if (condition instanceof PositiveConstraint)
			return handlePositiveConstraint((PositiveConstraint) condition);
		else if (condition instanceof Implication)
			return handleImplication((Implication) condition);
		else if (condition instanceof OrBody)
			return handleOrBody((OrBody) condition);
		else
			throw new IllegalArgumentException("Don't know how to handle: " + condition);
	}

	private String handleImplication(Implication condition) {
		try {
			var subPattern = new NeoImplicationPattern(condition, allSubPatterns.size(), this);
			subImplicationPatterns.add(subPattern);
			allSubPatterns.add(subPattern);
			return subPattern.getLogicVariable();
		} catch (Exception e) {
			e.printStackTrace();
			return e.getMessage();
		}
	}

	private String handlePositiveConstraint(PositiveConstraint condition) {
		return handleSubPattern(condition);
	}

	protected String handleSubPattern(ConstraintBody condition) {
		try {
			var subPattern = new NeoPredicatePattern(condition, allSubPatterns.size(), this);
			subPredicatePatterns.add(subPattern);
			allSubPatterns.add(subPattern);
			return subPattern.getLogicVariable();
		} catch (FlattenerException e) {
			e.printStackTrace();
			return e.getMessage();
		}
	}

	private String handleNegativeConstraint(NegativeConstraint condition) {
		return handleSubPattern(condition);
	}

	private String handleConstraintReference(ConstraintReference ref) {
		var referencedExp = handleConstraintBody(ref.getReference().getBody());

		if (ref.isNegated())
			return "NOT (" + referencedExp + ")";
		else
			return referencedExp;
	}

	private String handleOrBody(OrBody or) {
		return "(" + //
				or.getChildren().stream()//
						.map(this::handleAndBody)//
						.collect(Collectors.joining(" OR "))//
				+ ")";
	}

	private String handleAndBody(AndBody and) {
		var expression = new ArrayList<String>();
		for (var child : and.getChildren()) {
			if (child instanceof ConstraintReference)
				expression.add(handleConstraintReference((ConstraintReference) child));
			else
				expression.add(handleOrBody((OrBody) child));
		}

		return "(" + expression.stream().collect(Collectors.joining(" AND ")) + ")";
	}

	@Override
	public Collection<NeoMatch> determineMatches(Schedule schedule, IMask mask) {
		var cypherQuery = getQuery(schedule, mask);

		var parameters = new HashMap<String, Object>();
		// Parameters for collections of ids used to sample nodes
		parameters.putAll(schedule.getParameters());
		// Parameters for masked values
		parameters.putAll(mask.getParameters());

		var result = builder.executeQuery(cypherQuery, parameters);
		logger.debug(parameters);
		logger.debug("\n" + cypherQuery);
		return extractMatches(result);
	}

	private Collection<NeoMatch> extractMatches(StatementResult result) {
		if (result == null) {
			throw new NeoDatabaseException();
		} else {
			var matches = new ArrayList<NeoMatch>();
			while (result.hasNext()) {
				var record = result.next();
				matches.add(new NeoMatch(this, record));
			}

			logger.debug("Found: " + matches.size() + " matches!");

			return matches;
		}
	}

	public String getQuery(Schedule schedule, IMask mask) {
		return CypherPatternQueryGenerator.query(this, schedule, mask).toString();
	}

	public String getIsStillValidQuery() {
		return CypherPatternQueryGenerator.isStillValidQuery(this).toString();
	}

	@Override
	public Map<String, Boolean> isStillValid(Collection<NeoMatch> matches) {
		var cypherQuery = getIsStillValidQuery();

		var result = builder.executeQuery(cypherQuery, Map.of(NeoMatch.getMatchesParameter(), matches));
		logger.debug(matches);
		logger.debug("\n" + cypherQuery);

		if (result == null) {
			throw new NeoDatabaseException();
		} else {
			var hashCode = result.list().stream()//
					.map(res -> res.asMap().get(NeoMatch.getIdParameter()).toString())//
					.collect(Collectors.toList());

			var returnMap = matches.stream()//
					.collect(Collectors.toMap(m -> m.getHashCode(), m -> hashCode.contains(m.getHashCode())));

			logger.debug(returnMap.toString());
			return returnMap;
		}
	}

	public Collection<Record> getData(Collection<? extends NeoMatch> matches) {
		var cypherQuery = getDataQuery();

		var result = builder.executeQuery(cypherQuery, Map.of(NeoMatch.getMatchesParameter(), matches));
		logger.debug(matches);
		logger.debug("\n" + cypherQuery);

		if (result == null) {
			throw new NeoDatabaseException();
		} else {
			var results = result.list();
			return results;
		}
	}

	private String getDataQuery() {
		return CypherPatternQueryGenerator.dataQuery(this).toString();
	}

	public Collection<NeoPredicatePattern> getSubPredicatePatterns() {
		return subPredicatePatterns;
	}

	public Collection<NeoImplicationPattern> getSubImplicationPatterns() {
		return subImplicationPatterns;
	}

	public List<NeoSubPattern> getAllSubPatterns() {
		return allSubPatterns;
	}

	public String getLogicalExprForWhere() {
		return logicalExprForWhere;
	}
}
