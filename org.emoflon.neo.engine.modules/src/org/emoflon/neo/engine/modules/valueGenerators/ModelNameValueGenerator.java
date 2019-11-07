package org.emoflon.neo.engine.modules.valueGenerators;

import org.emoflon.neo.emsl.eMSL.DataType;
import org.emoflon.neo.engine.generator.modules.IParameterValueGenerator;

public class ModelNameValueGenerator implements IParameterValueGenerator<DataType, String> {
	private String srcModelName;
	private String trgModelName;

	/**
	 * Provides the model names for the generator. If a random name should be
	 * created, simply set the model-name to null.
	 */
	public ModelNameValueGenerator(String srcModelName, String trgModelName) {
		if (srcModelName == null)
			srcModelName = LoremIpsum.getInstance().randomWord();

		if (trgModelName == null)
			trgModelName = LoremIpsum.getInstance().randomWord();

		this.srcModelName = srcModelName;
		this.trgModelName = trgModelName;
	}

	@Override
	public boolean generatesValueFor(String parameterName, DataType dataType) {
		return "__srcModelName".equals(parameterName) || "__trgModelName".equals(parameterName);
	}

	@Override
	public String generateValueFor(String parameterName) {
		if ("__srcModelName".equals(parameterName))
			return srcModelName;
		else if ("__trgModelName".equals(parameterName))
			return trgModelName;
		else
			return null;
	}
}
