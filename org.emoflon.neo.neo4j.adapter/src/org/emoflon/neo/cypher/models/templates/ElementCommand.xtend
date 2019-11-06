package org.emoflon.neo.cypher.models.templates

import java.util.List
import org.eclipse.xtend.lib.annotations.Data

@Data abstract class ElementCommand {
	val List<NeoProp> properties

	new(List<NeoProp> props) {
		if (props === null)
			throw new IllegalArgumentException("Properties should never be null!")

		properties = props
	}
}
