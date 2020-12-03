package org.emoflon.neo.engine.modules.ilp;

import org.moeaframework.Executor;

//FIXME[Nils]
public class MOEAWrapper {
	public static void main(String[] args) {
		Executor e = new Executor();
		e.checkpointEveryIteration();
	}
}
