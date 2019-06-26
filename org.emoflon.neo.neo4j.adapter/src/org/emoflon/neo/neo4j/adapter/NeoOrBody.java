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
}
