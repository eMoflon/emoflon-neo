package org.emoflon.neo.neo4j.adapter.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.emoflon.neo.emsl.eMSL.AtomicPattern;
import org.emoflon.neo.emsl.eMSL.Pattern;
import org.emoflon.neo.emsl.eMSL.Rule;
import org.emoflon.neo.emsl.eMSL.SuperType;
import org.emoflon.neo.emsl.refinement.EMSLFlattener;
import org.emoflon.neo.emsl.util.FlattenerException;
import org.emoflon.neo.neo4j.adapter.common.NeoNode;
import org.emoflon.neo.neo4j.adapter.common.NeoRelation;

public class NeoUtil {
	private static final Logger logger = Logger.getLogger(NeoUtil.class);
	
	private NeoUtil() {}
    
    public static List<String> extractElementsOnlyInConclusionPattern(Collection<NeoNode> ifPattern, Collection<NeoNode> thenPattern) {
        List<String> temp = new ArrayList<String>();
        List<String> only = new ArrayList<String>();
        
        for(NeoNode n: ifPattern) {
            temp.add(n.getVarName());
            for(NeoRelation r: n.getRelations()) {
                temp.add(r.getVarName());
            }
        }
        for(NeoNode n:thenPattern) {
            if(!temp.contains(n.getVarName()))
                only.add(n.getVarName());
            for(NeoRelation r: n.getRelations()) {
                if(!temp.contains(r.getVarName())) {
                    only.add(r.getVarName());
                }
            }
        }
        return only;
    }

    private static SuperType getFlattenedSuperType(SuperType st) {
    	try {
			return EMSLFlattener.flatten(st);
		} catch (FlattenerException e) {
			logger.error("EMSL Flattener was unable to process the entity.");
			e.printStackTrace();
			return st;
		}
    }
    
	public static AtomicPattern getFlattenedPattern(AtomicPattern ap) {
		return (AtomicPattern) getFlattenedSuperType(ap);
	}

	public static Pattern getFlattenedPattern(Pattern p) {
		try {
			return EMSLFlattener.flattenPattern(p);
		} catch (FlattenerException e) {
			logger.error("EMSL Flattener was unable to process the entity.");
			e.printStackTrace();
			return p;
		}
	}
	
	public static Rule getFlattenedRule(Rule r) {
		return (Rule) getFlattenedSuperType(r);
	}	
}
