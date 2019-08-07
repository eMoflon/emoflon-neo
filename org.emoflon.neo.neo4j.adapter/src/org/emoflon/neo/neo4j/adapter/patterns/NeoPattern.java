package org.emoflon.neo.neo4j.adapter.patterns;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.emoflon.neo.emsl.eMSL.Constraint;
import org.emoflon.neo.emsl.eMSL.ConstraintReference;
import org.emoflon.neo.emsl.eMSL.Pattern;
import org.emoflon.neo.emsl.util.EMSLUtil;
import org.emoflon.neo.engine.api.constraints.IConstraint;
import org.emoflon.neo.engine.api.rules.IPattern;
import org.emoflon.neo.neo4j.adapter.CypherPatternBuilder;
import org.emoflon.neo.neo4j.adapter.NeoCondition;
import org.emoflon.neo.neo4j.adapter.NeoConstraint;
import org.emoflon.neo.neo4j.adapter.NeoCoreBuilder;
import org.emoflon.neo.neo4j.adapter.NeoHelper;
import org.emoflon.neo.neo4j.adapter.NeoMask;
import org.emoflon.neo.neo4j.adapter.NeoMatch;
import org.emoflon.neo.neo4j.adapter.NeoNegativeConstraint;
import org.emoflon.neo.neo4j.adapter.NeoNode;
import org.emoflon.neo.neo4j.adapter.NeoPositiveConstraint;
import org.neo4j.driver.v1.Record;

/**
 * Class for representing an in EMSL defined pattern for creating pattern
 * matching or condtion queries
 * 
 * @author Jannik Hinz
 *
 */
public abstract class NeoPattern implements IPattern<NeoMatch> {
	protected static final Logger logger = Logger.getLogger(NeoCoreBuilder.class);

	protected Optional<NeoMask> mask = Optional.empty();

	protected NeoHelper helper;

	protected Pattern p;
	protected Constraint c;
	protected IConstraint cond;
	protected boolean injective;

	protected List<NeoNode> nodes;

	protected NeoPattern(Pattern p) {
		nodes = new ArrayList<>();
		injective = true;
		this.helper = new NeoHelper();

		// execute the Pattern flatterer. Needed if the pattern use refinements or other
		// functions. Returns the complete flattened Pattern.
		this.p = helper.getFlattenedPattern(p);

		// get all nodes, relations and properties from the pattern
		extractNodesAndRelations();
	}

	/**
	 * Creates and extracts all necessary information data from the flattend
	 * Pattern. Create new NeoNode for any AtomicPattern node and corresponding add
	 * Relations and Properties and save them to the node in an node list.
	 */
	private void extractNodesAndRelations() {

		for (var n : p.getBody().getNodeBlocks()) {

			var node = new NeoNode(n.getType().getName(), helper.newPatternNode(n.getName()));

			n.getProperties().forEach(p -> node.addProperty(//
					p.getType().getName(), //
					EMSLUtil.handleValue(p.getValue())));

			// TODO[Jannik] Think of how to handle optional edges with multiple types
			n.getRelations()
					.forEach(r -> node.addRelation(
							helper.newPatternRelation(node.getVarName(), n.getRelations().indexOf(r),
									EMSLUtil.getOnlyType(r).getName(), r.getTarget().getName()),
							EMSLUtil.getOnlyType(r).getName(), //
							r.getProperties(), //
							r.getTarget().getType().getName(), //
							r.getTarget().getName()));

			nodes.add(node);
		}
	}

	public abstract Record getData(NeoMatch m);

	public String getQuery() {
		if (p.getCondition() == null) {
			return CypherPatternBuilder.readQuery_copyPaste(nodes, injective);
		} else {

			if (p.getCondition() instanceof ConstraintReference) {
				var cond = new NeoCondition(new NeoConstraint(c, Optional.empty(), helper), this, c.getName(),
						Optional.empty(), helper);
				return cond.getQuery();

			} else if (cond instanceof NeoPositiveConstraint) {

				var constraint = ((NeoPositiveConstraint) cond);
				return CypherPatternBuilder.constraintQuery_copyPaste(nodes, helper.getNodes(),
						constraint.getQueryString_MatchCondition(), constraint.getQueryString_WhereConditon(),
						injective, 0);

			} else if (cond instanceof NeoNegativeConstraint) {

				var constraint = ((NeoNegativeConstraint) cond);
				return CypherPatternBuilder.constraintQuery_copyPaste(nodes, helper.getNodes(),
						constraint.getQueryString_MatchCondition(), constraint.getQueryString_WhereConditon(),
						injective, 0);
			} else {
				// Note: If/Then conditions are currently not supported
				throw new UnsupportedOperationException();
			}
		}
	}

	/**
	 * Set is the pattern should be injective or not
	 * 
	 * @param injective is the pattern should be injective matched
	 */
	@Override
	public void setMatchInjectively(Boolean injective) {
		this.injective = injective;
	}

	/**
	 * Return the name of the given Pattern
	 * 
	 * @return name of the pattern
	 */
	@Override
	public String getName() {
		return p.getBody().getName();
	}

	/**
	 * Return a NeoNode list of all nodes in the pattern
	 * 
	 * @return NeoNode list of nodes in the pattern
	 */
	public List<NeoNode> getNodes() {
		return nodes;
	}

	/**
	 * Get the injectivity information of a pattern
	 * 
	 * @return boolean true if the given pattern requires injective pattern matching
	 */
	public boolean isInjective() {
		return injective;
	}

	public Pattern getPattern() {
		return p;
	}

	/**
	 * Runs the pattern matching and counts size of matches
	 * 
	 * @return Number of matches
	 */
	@Override
	public Number countMatches() {
		var matches = determineMatches();
		if (matches != null)
			return matches.size();
		else
			return 0;
	}

	public abstract boolean isStillValid(NeoMatch neoMatch);

	/**
	 * Return is the given pattern an base on the constraint reference is negated
	 * 
	 * @return boolean if or not the given result of constraint reference must be
	 *         negated
	 */
	public boolean isNegated() {
		if (p.getCondition() != null)
			return ((ConstraintReference) (p.getCondition())).isNegated();
		else
			return false;
	}
}
