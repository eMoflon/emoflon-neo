package org.emoflon.neo.neo4j.adapter;

import java.util.ArrayList;
import java.util.Collection;

import org.emoflon.neo.emsl.eMSL.AndBody;
import org.emoflon.neo.emsl.eMSL.OrBody;

public class NeoOrBody {

	private OrBody body;

	private NeoCoreBuilder builder;

	public NeoOrBody(OrBody body, NeoCoreBuilder builder) {

		this.body = body;
		this.builder = builder;

	}
	

	public boolean isSatisfied() {

		for (AndBody b : body.getChildren()) {
			var andbody = new NeoAndBody(b, builder);

			if (andbody.isSatisfied()) {
				return true;
			}

		}

		return false;

	}

	public Collection<NeoNode> getNodes() {
		
		Collection<NeoNode> nodes = new ArrayList<>();
		
		for (AndBody b : body.getChildren()) {
			var andbody = new NeoAndBody(b, builder);
			for(NeoNode node : andbody.getNodes()) {
				nodes.add(node);
			}
			
		}
		return nodes;
		
	}

	public String getOptionalQuery() {
		
		var query = "";
		
		for (AndBody b : body.getChildren()) {
			
			var andbody = new NeoAndBody(b, builder);
			query += andbody.getOptionalMatch();

		}
		return query;
	}


	public String getQueryString_Where() {
		
		var query = "(";
		var first = true;
		
		for (AndBody b : body.getChildren()) {
			
			var andbody = new NeoAndBody(b, builder);
			if(first) {
				first = false;
			} else {
				query += " OR ";
			}
			query += andbody.getQueryString_Where();

		}
		return query + ")";
	}
}
