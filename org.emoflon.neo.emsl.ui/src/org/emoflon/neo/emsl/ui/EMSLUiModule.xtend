/*
 * generated by Xtext 2.16.0
 */
package org.emoflon.neo.emsl.ui

import org.apache.log4j.Level
import org.apache.log4j.Logger
import org.eclipse.ui.plugin.AbstractUIPlugin
import org.eclipse.xtext.ui.editor.syntaxcoloring.AbstractAntlrTokenToAttributeIdMapper
import org.emoflon.neo.emsl.ui.util.ENeoConsole
import org.eclipse.xtext.ui.editor.syntaxcoloring.DefaultAntlrTokenToAttributeIdMapper
import org.eclipse.xtext.ui.editor.syntaxcoloring.DefaultHighlightingConfiguration
import org.emoflon.neo.emsl.parser.antlr.internal.InternalEMSLLexer

/**
 * Use this class to register components to be used within the Eclipse IDE.
 */
class EMSLUiModule extends AbstractEMSLUiModule {
	new(AbstractUIPlugin plugin) {
		super(plugin);

		Logger.rootLogger.level = Level.DEBUG
		Logger.rootLogger.addAppender(new ENeoConsole);
	}

	def Class<? extends AbstractAntlrTokenToAttributeIdMapper> bindAbstractAntlrTokenToAttributeIdMapper() {
		return AntlrTokenToAttributeIdMapper
	}
}

class AntlrTokenToAttributeIdMapper extends DefaultAntlrTokenToAttributeIdMapper {
	
	override protected calculateId(String tokenName, int tokenType) {
		if(tokenType === InternalEMSLLexer.RULE_DESCRIPTION)
			return DefaultHighlightingConfiguration.STRING_ID
		else
			super.calculateId(tokenName, tokenType)
	}
	
}
