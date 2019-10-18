package org.emoflon.neo.victory.adapter;

import java.util.Collection;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.emoflon.neo.engine.api.patterns.IMatch;
import org.emoflon.victory.ui.api.Rule;

@SuppressWarnings("all")
public class MatchQuery {
  public static String create(final IMatch match, final Rule rule, final int neighbourhoodSize) {
    StringConcatenation _builder = new StringConcatenation();
    String _matchPath = MatchQuery.matchPath(match, neighbourhoodSize);
    _builder.append(_matchPath);
    _builder.newLineIfNotEmpty();
    String _checkIds = MatchQuery.checkIds(match, neighbourhoodSize);
    _builder.append(_checkIds);
    _builder.newLineIfNotEmpty();
    String _returnPath = MatchQuery.returnPath(match, neighbourhoodSize);
    _builder.append(_returnPath);
    _builder.newLineIfNotEmpty();
    return _builder.toString();
  }
  
  public static String getMatchEdges(final long edgeId) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("MATCH ()-[r]->() WHERE id(r)= ");
    _builder.append(edgeId);
    _builder.append(" RETURN r");
    _builder.newLineIfNotEmpty();
    return _builder.toString();
  }
  
  public static String matchPath(final IMatch match, final int neighbourhoodSize) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("MATCH ");
    {
      Collection<Long> _values = match.getNodeIDs().values();
      boolean _hasElements = false;
      for(final Long n : _values) {
        if (!_hasElements) {
          _hasElements = true;
        } else {
          _builder.appendImmediate(", ", "");
        }
        _builder.newLineIfNotEmpty();
        _builder.append("p");
        _builder.append(n);
        _builder.append("=(n");
        _builder.append(n);
        _builder.append(")- [*0..");
        _builder.append(neighbourhoodSize);
        _builder.append("]-(m");
        _builder.append(n);
        _builder.append(")");
        _builder.newLineIfNotEmpty();
      }
    }
    return _builder.toString();
  }
  
  public static String checkIds(final IMatch match, final int neighbourhoodSize) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("WHERE ");
    {
      Collection<Long> _values = match.getNodeIDs().values();
      boolean _hasElements = false;
      for(final Long n : _values) {
        if (!_hasElements) {
          _hasElements = true;
        } else {
          _builder.appendImmediate(" AND ", "");
        }
        _builder.newLineIfNotEmpty();
        _builder.append("id(n");
        _builder.append(n);
        _builder.append(") = ");
        _builder.append(n);
      }
    }
    _builder.append(" ");
    _builder.newLineIfNotEmpty();
    return _builder.toString();
  }
  
  public static String returnPath(final IMatch match, final int neighbourhoodSize) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("RETURN ");
    {
      Collection<Long> _values = match.getNodeIDs().values();
      boolean _hasElements = false;
      for(final Long n : _values) {
        if (!_hasElements) {
          _hasElements = true;
        } else {
          _builder.appendImmediate(",\n ", "");
        }
        _builder.newLineIfNotEmpty();
        _builder.append("p");
        _builder.append(n);
        _builder.newLineIfNotEmpty();
      }
    }
    return _builder.toString();
  }
}
