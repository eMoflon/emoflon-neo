package org.emoflon.neo.neo4j.adapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.emf.ecore.EObject;
import org.emoflon.neo.emsl.eMSL.AtomicPattern;
import org.emoflon.neo.emsl.eMSL.Constraint;
import org.emoflon.neo.emsl.eMSL.ConstraintBody;
import org.emoflon.neo.emsl.eMSL.ConstraintReference;
import org.emoflon.neo.emsl.eMSL.Implication;
import org.emoflon.neo.emsl.eMSL.ModelNodeBlock;
import org.emoflon.neo.emsl.eMSL.ModelPropertyStatement;
import org.emoflon.neo.emsl.eMSL.ModelRelationStatement;
import org.emoflon.neo.emsl.eMSL.NegativeConstraint;
import org.emoflon.neo.emsl.eMSL.Pattern;
import org.emoflon.neo.emsl.eMSL.PositiveConstraint;
import org.emoflon.neo.engine.api.constraints.IConstraint;
import org.emoflon.neo.engine.api.rules.IMatch;
import org.neo4j.driver.v1.StatementResult;

/**
 * TODO[Jannik] Implement constraints
 * 
 */
public class NeoConstraint implements IConstraint {

	private static final Logger logger = Logger.getLogger(NeoCoreBuilder.class);

	private NeoCoreBuilder builder;

	private Constraint c;
	private Collection<AtomicPattern> pattern;
	List<EObject> ref;
	
	public NeoConstraint(Constraint c, NeoCoreBuilder builder) {
		this.pattern = new ArrayList<>();
		this.builder = builder;
		this.c = c;
		
		for(int i=0; i<c.getBody().eCrossReferences().size(); i++) {
			
			if(c.getBody().eCrossReferences().get(i) instanceof AtomicPattern) {
				logger.info("AtomicPattern");
				AtomicPattern ap = (AtomicPattern) c.getBody().eCrossReferences().get(i);
				logger.info(ap.getNodeBlocks().toString());
				
				pattern.add(ap);
				
			} else {
				logger.info(c.getBody().eCrossReferences().get(i).getClass().getName().toString());
				logger.info(c.getBody().eCrossReferences().get(i).toString());
			}
			
		}
		
		logger.info(c.getBody().toString());

	}
	
	private List<NeoNode> extractPatternInformations(AtomicPattern ap) {
		
		List<NeoNode> nodes = new ArrayList<>();
		
		for(ModelNodeBlock n : ap.getNodeBlocks()) {
			var node = new NeoNode(n.getType().getName(), n.getName());
			
			for(ModelPropertyStatement p : n.getProperties()) {
				node.addProperty(p.getType().getName(), NeoUtil.handleValue(p.getValue()));
			}
			for(ModelRelationStatement r: n.getRelations()) {
				node.addRelation(new NeoRelation(
						node, //
						n.getRelations().indexOf(r), //
						r.getType().getName(), //
						r.getProperties(), //
						r.getTarget().getType().getName(), //
						r.getTarget().getName()));
			}
			
			nodes.add(node);
		}
		return nodes;
		
	}

	@Override
	public boolean isSatisfied() {
		
		for(AtomicPattern ap : pattern) {
			
			List<NeoNode> nodes = extractPatternInformations(ap);
			logger.info("Searching matches for Pattern: " + ap.getName());
			var cypherQuery = CypherPatternBuilder.readQuery(nodes, true);
			logger.debug(cypherQuery);

			var result = builder.executeQuery(cypherQuery);

			var matches = 0;
			while (result.hasNext()) {
				result.next();
				matches += 1;
			}
			logger.info("Found " + matches + " matche(s)");

			if (c.getBody() instanceof PositiveConstraint) {
				if(matches > 0)
					return true;
				else
					return false;
			} else if (c.getBody() instanceof NegativeConstraint) {
				if(matches == 0)
					return true;
				else
					return false;
			}
			
		}
		return false;
	}

}
