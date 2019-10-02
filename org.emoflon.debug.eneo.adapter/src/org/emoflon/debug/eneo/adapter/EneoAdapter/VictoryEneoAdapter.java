package org.emoflon.debug.eneo.adapter.EneoAdapter;

import java.io.IOException;
import java.util.Collection;

import org.emoflon.ibex.tgg.ui.debug.api.DataProvider;
import org.emoflon.ibex.tgg.ui.debug.api.Rule;
import org.emoflon.ibex.tgg.ui.debug.api.Victory;
import org.emoflon.neo.engine.api.rules.IRule;
import org.emoflon.neo.neo4j.adapter.patterns.NeoMatch;
import org.emoflon.neo.neo4j.adapter.rules.NeoCoMatch;

public class VictoryEneoAdapter implements DataProvider {

    public static VictoryEneoAdapter create(Collection<IRule<NeoMatch, NeoCoMatch>> pRules) {

	pRules.forEach(rule -> EneoRuleAdapter.adapt(rule));

	VictoryEneoAdapter adapter = new VictoryEneoAdapter();
	Victory.create(adapter);
	return adapter;
    }

    private VictoryEneoAdapter() {
    }
    public boolean runUI() {
   	return Victory.run();
       }

    @Override
    public Collection<Rule> getAllRules() {
	return EneoRuleAdapter.getAllRules();
    }

    @Override
    public void saveModels() throws IOException {
	// TODO Auto-generated method stub

    }

}
