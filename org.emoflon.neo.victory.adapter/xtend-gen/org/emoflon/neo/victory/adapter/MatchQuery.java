package org.emoflon.neo.victory.adapter;

import org.eclipse.xtend2.lib.StringConcatenation;
import org.emoflon.neo.engine.api.patterns.IMatch;
import org.emoflon.victory.ui.api.Rule;

@SuppressWarnings("all")
public class MatchQuery {
  public static String create(final IMatch match, final Rule rule, final int neighbourhoodSize) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("match p1=(n1)-[*0..");
    _builder.append(neighbourhoodSize);
    _builder.append("]-(m1)  where id(n1) = 66160 return p1");
    _builder.newLineIfNotEmpty();
    return _builder.toString();
  }
}
