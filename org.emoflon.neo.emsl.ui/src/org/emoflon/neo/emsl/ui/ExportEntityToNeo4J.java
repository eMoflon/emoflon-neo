package org.emoflon.neo.emsl.ui;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

public class ExportEntityToNeo4J extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		return null;
	}

	@Override
	public void setEnabled(Object evaluationContext) {
		System.out.println("Yihaa!");
	}
}
