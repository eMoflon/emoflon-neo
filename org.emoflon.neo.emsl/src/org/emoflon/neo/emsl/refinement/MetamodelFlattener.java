package org.emoflon.neo.emsl.refinement;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.util.EcoreUtil;
import org.emoflon.neo.emsl.eMSL.EMSLFactory;
import org.emoflon.neo.emsl.eMSL.Entity;
import org.emoflon.neo.emsl.eMSL.Metamodel;
import org.emoflon.neo.emsl.eMSL.MetamodelNodeBlock;
import org.emoflon.neo.emsl.eMSL.MetamodelRefinementCommand;
import org.emoflon.neo.emsl.eMSL.MetamodelRelationStatement;
import org.emoflon.neo.emsl.util.FlattenerErrorType;
import org.emoflon.neo.emsl.util.FlattenerException;

public class MetamodelFlattener extends AbstractEntityFlattener implements IEntityFlattener {

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Entity> T flatten(T t, Set<String> alreadyRefinedEntityNames) throws FlattenerException {
		// Sanity checks
		forbidNullEntity(t);
		forbidRefinementLoops(t, alreadyRefinedEntityNames);

		// 0. Make a copy
		Metamodel entity = (Metamodel) EcoreUtil.copy(t);

		// 1. Recursively flatten all parents
		var allFlattenedParents = flattenTransitiveParents(entity, alreadyRefinedEntityNames);

		// 2. Build a union of all inherited elements
		var union = union(allFlattenedParents, entity);

		// 3. step: merge nodes and edges
		var merge = merge(union);

		return (T) merge;
	}

	private <T extends Entity> void forbidNullEntity(T t) {
		if (t == null)
			throw new IllegalArgumentException("I can't flatten a null entity!");
	}

	private <T extends Entity> void forbidRefinementLoops(T t, Set<String> alreadyRefinedEntityNames)
			throws FlattenerException {
		if (alreadyRefinedEntityNames.contains(dispatcher.getName(t)))
			throw new FlattenerException(t, FlattenerErrorType.INFINITE_LOOP, alreadyRefinedEntityNames);
	}

	private Map<MetamodelRefinementCommand, Metamodel> flattenTransitiveParents(Metamodel entity,
			Set<String> alreadyRefinedEntityNames) {
		var refComandToParent = new HashMap<MetamodelRefinementCommand, Metamodel>();
		for (var refinementCommand : entity.getSuperRefinementTypes()) {
			try {
				alreadyRefinedEntityNames.add(entity.getName());
				var flatParent = flatten((Metamodel) refinementCommand.getReferencedType(),
						new HashSet<String>(alreadyRefinedEntityNames));
				refComandToParent.put(refinementCommand, flatParent);
			} catch (FlattenerException e) {
				e.printStackTrace();
			}
		}

		return refComandToParent;
	}

	private Metamodel union(Map<MetamodelRefinementCommand, Metamodel> allFlattenedParents, Metamodel entity) {
		for (var flatParent : allFlattenedParents.entrySet()) {
			applyRelabeling(flatParent.getKey(), flatParent.getValue());
			entity.getNodeBlocks().addAll(flatParent.getValue().getNodeBlocks());
			entity.getEnums().addAll(flatParent.getValue().getEnums());
		}

		return entity;
	}

	private void applyRelabeling(MetamodelRefinementCommand command, Metamodel metamodel) {
		for (var nodeBlock : metamodel.getNodeBlocks()) {
			for (var relabelling : command.getRelabeling()) {
				if (relabelling.getOldLabel().equals(nodeBlock.getName())) {
					nodeBlock.setName(relabelling.getNewLabel());
				}
			}
		}
	}

	private Metamodel merge(Metamodel union) throws FlattenerException {
		return mergeReferences(mergeInheritanceEdges(mergeNodes(union)));
	}

	private Metamodel mergeNodes(Metamodel union) {
		var merge = EMSLFactory.eINSTANCE.createMetamodel();
		merge.setName(union.getName());
		merge.setAbstract(union.isAbstract());
		merge.getEnums().addAll(union.getEnums());

		var nameToBlocks = new HashMap<String, List<MetamodelNodeBlock>>();
		union.getNodeBlocks().forEach(nb -> {
			nameToBlocks.putIfAbsent(nb.getName(), new ArrayList<>());
			nameToBlocks.get(nb.getName()).add(nb);
		});

		for (var entry : nameToBlocks.entrySet()) {
			var newBlock = EMSLFactory.eINSTANCE.createMetamodelNodeBlock();
			merge.getNodeBlocks().add(newBlock);
			newBlock.setName(entry.getKey());
			newBlock.setAbstract(entry.getValue().stream()//
					.anyMatch(nb -> nb.isAbstract() != true));
			for (var nb : entry.getValue()) {
				newBlock.getProperties().addAll(nb.getProperties());
				newBlock.getRelations().addAll(nb.getRelations());
				newBlock.getSuperTypes().addAll(nb.getSuperTypes());
			}
		}

		return merge;
	}

	private Metamodel mergeInheritanceEdges(Metamodel union) {
		for (var nb : union.getNodeBlocks()) {
			var superTypes = nb.getSuperTypes().stream()//
					.map(st -> st.getName())//
					.collect(Collectors.toSet());
			nb.getSuperTypes().clear();
			for (var ref : superTypes) {
				for (var snb : union.getNodeBlocks()) {
					if (snb.getName().equals(ref))
						nb.getSuperTypes().add(snb);
				}
			}
		}

		return union;
	}

	private Metamodel mergeReferences(Metamodel union) throws FlattenerException {
		for (var nb : union.getNodeBlocks()) {
			var nameToRelations = new HashMap<String, List<MetamodelRelationStatement>>();
			for (var rel : nb.getRelations()) {
				nameToRelations.putIfAbsent(rel.getName(), new ArrayList<MetamodelRelationStatement>());
				nameToRelations.get(rel.getName()).add(rel);
			}
			nb.getRelations().clear();
			for (var relations : nameToRelations.entrySet()) {
				var mergedRel = EMSLFactory.eINSTANCE.createMetamodelRelationStatement();
				mergedRel.setName(relations.getKey());

				var lower = relations.getValue().stream()//
						.map(r -> r.getLower())//
						.max(Comparator.comparingInt(v -> Integer.valueOf(v)));

				mergedRel.setLower(lower.orElseThrow());

				var upper = relations.getValue().stream()//
						.map(r -> r.getUpper())//
						.min(Comparator.comparingInt(v -> Integer.valueOf(v)));

				mergedRel.setUpper(upper.orElseThrow());

				var targets = relations.getValue().stream()//
						.map(r -> r.getTarget().getName())//
						.collect(Collectors.toSet());

				if (targets.size() != 1) {
					throw new FlattenerException(union, FlattenerErrorType.CONFLICTING_REFERENCES);
				} else {
					for (var target : union.getNodeBlocks()) {
						if (targets.contains(target.getName()))
							mergedRel.setTarget(target);
					}
				}

				var kind = relations.getValue().stream()//
						.map(r -> r.getKind())//
						.max(Comparator.comparingInt(k -> k.getValue()));

				mergedRel.setKind(kind.orElseThrow());

				for (var r : relations.getValue()) {
					mergedRel.getProperties().addAll(r.getProperties());
				}

				nb.getRelations().add(mergedRel);
			}
		}

		return union;
	}
}
