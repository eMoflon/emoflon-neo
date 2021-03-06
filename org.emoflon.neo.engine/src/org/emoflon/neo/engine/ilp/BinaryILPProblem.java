package org.emoflon.neo.engine.ilp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class is an extension of the generic {@link ILPProblem} for the more
 * specific binary ILP problems in which variables are either 1 or 0. <br>
 *
 * The class contains basic constructs that are often used in boolean
 * expression. Therefore when defining the ILP it is not necessary to think
 * about how to transform the boolean expressions into constraints, as these are
 * automatically translated into suitable ILP constraints. When fixing variables
 * these boolean constructs help determining other variables that can already be
 * fixed as well, which simplifies the ILP problem and reduces the time needed
 * for computing a valid solution.
 *
 */
public class BinaryILPProblem extends ILPProblem {
	/**
	 * This map contains all defined implications
	 */
	private final LinkedList<Implication> implications = new LinkedList<>();
	/**
	 * A list containing all negative implications
	 */
	private final LinkedList<NegativeImplication> negativeImplications = new LinkedList<>();
	/**
	 * A list containing all exclusions
	 */
	private final LinkedList<Exclusion> exclusions = new LinkedList<>();

	/**
	 * A set of all constraints that were generated to express the boolean
	 * constructs.
	 */
	private final Set<ILPConstraint> generatedConstraints = new HashSet<>();

	/**
	 * All fixed variables that have been set to 1
	 */
	private final Set<Integer> positiveChoices = new HashSet<>();
	/**
	 * All fixed variables that have been set to 0
	 */
	private final Set<Integer> negativeChoices = new HashSet<>();
	/**
	 * Fixed but not yet applied variables set to 1
	 */
	private final Set<Integer> lazyPositiveChoices = new HashSet<>();
	/**
	 * Fixed but not yet applied variables set to 0
	 */
	private final Set<Integer> lazyNegativeChoices = new HashSet<>();

	/**
	 * Contains for each variable the list of constraints the variable is contained
	 * in. This makes fixing variables very efficient, but costs memory
	 */
	private final Map<Integer, LinkedList<BinaryConstraint>> variableIdsToContainingConstraints = new HashMap<>();

	/**
	 * Creates a new binary ILP problem
	 */
	public BinaryILPProblem() {
	}

	/**
	 * Superclass for all binary expressions that can be transformed into ILP
	 * constraints
	 */
	private abstract class BinaryConstraint {
		/**
		 * If the constraint is still needed or is superfluous
		 */
		private boolean isRelevant = true;
		/**
		 * The name of the constraint
		 */
		private final String name;

		/**
		 * Creates a new binary constraint
		 *
		 * @param name The name of the constraint
		 */
		private BinaryConstraint(final String name) {
			this.name = name;
		}

		/**
		 * @return whether the expression is still relevant, or superfluous
		 */
		final boolean isRelevant() {
			return this.isRelevant;
		}

		/**
		 * @param Sets the relevance status
		 */
		final void setRelevant(final boolean isRelevant) {
			this.isRelevant = isRelevant;
		}

		/**
		 * @return the name
		 */
		final String getName() {
			return this.name;
		}

		/**
		 * Fixes the given variable. If the value of other variables is obvious due to
		 * the fixes, they are fixed as well.
		 *
		 * @param variableId ID of the variable
		 * @param choice     value of the variable
		 * @return whether the constraint is still relevant
		 */
		abstract boolean fixVariable(int variableId, boolean choice);

		/**
		 * Fixes all given variables. If the value of other variables is obvious due to
		 * the fixes, they are fixed as well.
		 *
		 * @param positiveChoices Variables that have been set to 1
		 * @param negativeChoices Variables that have been set to 0
		 * @return whether the constraint is still relevant
		 */
		abstract boolean fixVariables(Collection<Integer> positiveChoices, Collection<Integer> negativeChoices);

		/**
		 * Generates an ILP constraint for the binary constraint.
		 */
		abstract void generateILPConstraint();

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + this.getOuterType().hashCode();
			result = prime * result + (this.isRelevant ? 1231 : 1237);
			return result;
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (this.getClass() != obj.getClass())
				return false;
			BinaryConstraint other = (BinaryConstraint) obj;
			if (!this.getOuterType().equals(other.getOuterType()))
				return false;
			if (this.isRelevant != other.isRelevant)
				return false;
			return true;
		}

		private BinaryILPProblem getOuterType() {
			return BinaryILPProblem.this;
		}
	}

	/**
	 * A constraint of the form x -> a v b <br>
	 * If the variable on the left side is chosen, one of the variables on the right
	 * side has to be chosen as well.
	 */
	public final class Implication extends BinaryConstraint {
		/**
		 * Variables on the left side of the implication
		 */
		private final Set<Integer> leftVariables;
		/**
		 * OR-connected variables on the right side of the implication
		 */
		private final Set<Integer> rightVariables;

		/**
		 * Creates a new implication from the given variables
		 *
		 * @param leftVariables  variables on the left side of the implication
		 * @param rightVariables variables on the right side of the implication
		 * @param name           the name of the implication
		 */
		private Implication(final Set<Integer> leftVariables, final Set<Integer> rightVariables, final String name) {
			super(name);
			this.leftVariables = leftVariables;
			this.rightVariables = rightVariables;

			this.fixVariables(BinaryILPProblem.this.positiveChoices, BinaryILPProblem.this.negativeChoices);

			if (this.isRelevant()) {
				BinaryILPProblem.this.implications.add(this);
				for (int id : this.leftVariables) {
					BinaryILPProblem.this.variableIdsToContainingConstraints.get(id).add(this);
				}
				for (int id : this.rightVariables) {
					BinaryILPProblem.this.variableIdsToContainingConstraints.get(id).add(this);
				}
			}
		}

		@Override
		boolean fixVariable(final int id, final boolean choice) {
			if (this.leftVariables.contains(id)) {
				if (!choice) { // left side is false
					this.setRelevant(false);
					return false;
				} else {
					// left side of the implication is true -> one of the right variables has to be
					// true
					new Exclusion(this.rightVariables, Integer.MAX_VALUE, 1, this.getName());
					this.setRelevant(false);
					return false;
				}
			}
			if (this.rightVariables.remove(id)) {
				// remove yielded true, so it was contained
				if (choice) {
					// implication is fulfilled -> remove
					this.setRelevant(false);
					return false;
				} else {
					if (this.rightVariables.isEmpty()) {
						// Implication cannot be fulfilled -> one of the left variables must be false
						new Exclusion(this.leftVariables, this.leftVariables.size() - 1, 0, this.getName());
						this.setRelevant(false);
						return false;
					}
				}
			}
			return true;
		}

		@Override
		boolean fixVariables(final Collection<Integer> positiveChoices, final Collection<Integer> negativeChoices) {
			for (int id : this.leftVariables) {
				if (negativeChoices.contains(id)) {
					this.setRelevant(false);
					return false;
				}
			}

			for (int id : positiveChoices) {
				if (this.rightVariables.contains(id)) {
					this.setRelevant(false);
					return false;
				}
			}

			if (this.rightVariables.removeAll(negativeChoices)) {
				if (this.rightVariables.isEmpty()) {
					// Implication cannot be fulfilled -> one of the left variables must be false
					new Exclusion(this.leftVariables, this.leftVariables.size() - 1, 0, this.getName());
					this.setRelevant(false);
					return false;
				}
			}

			if (positiveChoices.containsAll(this.leftVariables)) {
				// left side of the implication is true -> one of the right variables has to be
				// true
				new Exclusion(this.rightVariables, Integer.MAX_VALUE, 1, this.getName());
				this.setRelevant(false);
				return false;
			}
			return true;
		}

		@Override
		public String toString() {
			List<String> rightTermStrings = new LinkedList<>();
			this.rightVariables.stream().forEach((variableId) -> {
				rightTermStrings.add(BinaryILPProblem.this.getVariable(variableId));
			});

			List<String> leftTermStrings = new LinkedList<>();
			this.leftVariables.stream().forEach((variableId) -> {
				leftTermStrings.add(BinaryILPProblem.this.getVariable(variableId));
			});

			return "Implication(" + this.getName() + ")" + String.join(" ^ ", leftTermStrings) + " -> "
					+ String.join(" V ", rightTermStrings);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + this.getOuterType().hashCode();
			result = prime * result + ((this.leftVariables == null) ? 0 : this.leftVariables.hashCode());
			result = prime * result + ((this.rightVariables == null) ? 0 : this.rightVariables.hashCode());
			return result;
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (this.getClass() != obj.getClass())
				return false;
			Implication other = (Implication) obj;
			if (!this.getOuterType().equals(other.getOuterType()))
				return false;
			if (this.leftVariables == null) {
				if (other.leftVariables != null)
					return false;
			} else if (!this.leftVariables.equals(other.leftVariables))
				return false;
			if (this.rightVariables == null) {
				if (other.rightVariables != null)
					return false;
			} else if (!this.rightVariables.equals(other.rightVariables))
				return false;
			return true;
		}

		private BinaryILPProblem getOuterType() {
			return BinaryILPProblem.this;
		}

		@Override
		void generateILPConstraint() {
			ILPLinearExpression expr = BinaryILPProblem.this.createLinearExpression();
			this.leftVariables.stream().forEach(v -> {
				expr.addTerm(v, 1);
			});
			this.rightVariables.stream().forEach(v -> {
				expr.addTerm(v, -1);
			});
			ILPConstraint constr = new ILPConstraint(expr, Comparator.le, leftVariables.size() - 1, this.getName());
			BinaryILPProblem.this.addConstraint(constr);
			BinaryILPProblem.this.generatedConstraints.add(constr);

		}
	}

	/**
	 * A constraint of the form not(x V y) -> not(a) ^ not(b) <br>
	 * If none of the variables on the left side is chosen, none of the variables on
	 * the right side can be chosen
	 */
	public final class NegativeImplication extends BinaryConstraint {
		/**
		 * Variables on the left side of the implication
		 */
		private final Set<Integer> leftVariables;
		/**
		 * Variables on the right side of the implication
		 */
		private final Set<Integer> rightVariables;

		/**
		 * Adds a negative implication
		 *
		 * @param leftVariables  Variables on the left side of the implication
		 * @param rightVariables Variables on the right side of the implication
		 * @param name           Name of the implication
		 */
		private NegativeImplication(final Set<Integer> leftVariables, final Set<Integer> rightVariables,
				final String name) {
			super(name);

			this.leftVariables = leftVariables;
			this.rightVariables = rightVariables;

			this.fixVariables(BinaryILPProblem.this.positiveChoices, BinaryILPProblem.this.negativeChoices);

			if (this.isRelevant()) {
				BinaryILPProblem.this.negativeImplications.add(this);
				for (int id : this.leftVariables) {
					BinaryILPProblem.this.variableIdsToContainingConstraints.get(id).add(this);
				}
				for (int id : this.rightVariables) {
					BinaryILPProblem.this.variableIdsToContainingConstraints.get(id).add(this);
				}
			}
		}

		@Override
		void generateILPConstraint() {
			ILPLinearExpression expr = BinaryILPProblem.this.createLinearExpression();
			this.leftVariables.stream().forEach(v -> {
				expr.addTerm(v, -this.rightVariables.size());
			});
			this.rightVariables.stream().forEach(v -> {
				expr.addTerm(v, 1);
			});
			ILPConstraint constr = new ILPConstraint(expr, Comparator.le, 0.0, this.getName());
			BinaryILPProblem.this.addConstraint(constr);
			BinaryILPProblem.this.generatedConstraints.add(constr);
		}

		@Override
		boolean fixVariable(final int id, final boolean choice) {
			if (this.leftVariables.remove(id)) {
				// remove yielded true, so it was contained
				if (choice) {
					// irrelevant impl
					this.setRelevant(false);
					return false;
				} else {
					if (this.leftVariables.isEmpty()) {
						for (int id2 : this.rightVariables) {
							BinaryILPProblem.this.fixVariable(id2, false);
						}
						this.setRelevant(false);
						return false;
					}
				}
			}
			if (this.rightVariables.remove(id)) {
				// was contained
				if (choice) {
					// other vars are irrelevant -> remove
					this.rightVariables.clear();
					this.rightVariables.add(id);
				} else {
					if (this.rightVariables.isEmpty()) {
						// Implication is fulfilled -> remove
						this.setRelevant(false);
						return false;
					}
				}
			}
			return true;
		}

		@Override
		boolean fixVariables(final Collection<Integer> positiveChoices, final Collection<Integer> negativeChoices) {
			if (this.leftVariables.removeAll(negativeChoices)) {
				if (this.leftVariables.isEmpty()) {
					for (int id2 : this.rightVariables) {
						BinaryILPProblem.this.fixVariable(id2, false);
					}
					this.setRelevant(false);
					return false;
				}
			}
			if (this.rightVariables.removeAll(negativeChoices)) {
				if (this.rightVariables.isEmpty()) {
					// Implication is fulfilled -> remove
					this.setRelevant(false);
					return false;
				}
			}
			for (int id : positiveChoices) {
				if (this.leftVariables.contains(id)) {
					this.setRelevant(false);
					return false;
				}
				if (this.rightVariables.contains(id)) {
					this.rightVariables.clear();
					this.rightVariables.add(id);
					break;
				}
			}
			return true;
		}

		@Override
		public String toString() {
			List<String> termStringsLeft = new LinkedList<>();
			this.leftVariables.stream().forEach((variableId) -> {
				termStringsLeft.add(BinaryILPProblem.this.getVariable(variableId));
			});
			List<String> termStringsRight = new LinkedList<>();
			this.rightVariables.stream().forEach((variableId) -> {
				termStringsRight.add("-" + BinaryILPProblem.this.getVariable(variableId));
			});
			return "NegativeImplication(" + this.getName() + ")" + "-(" + String.join(" V ", termStringsLeft) + ") -> "
					+ String.join(" ^ ", termStringsRight);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + this.getOuterType().hashCode();
			result = prime * result + ((this.leftVariables == null) ? 0 : this.leftVariables.hashCode());
			result = prime * result + ((this.rightVariables == null) ? 0 : this.rightVariables.hashCode());
			return result;
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (this.getClass() != obj.getClass())
				return false;
			NegativeImplication other = (NegativeImplication) obj;
			if (!this.getOuterType().equals(other.getOuterType()))
				return false;
			if (this.leftVariables == null) {
				if (other.leftVariables != null)
					return false;
			} else if (!this.leftVariables.equals(other.leftVariables))
				return false;
			if (this.rightVariables == null) {
				if (other.rightVariables != null)
					return false;
			} else if (!this.rightVariables.equals(other.rightVariables))
				return false;
			return true;
		}

		private BinaryILPProblem getOuterType() {
			return BinaryILPProblem.this;
		}
	}

	/**
	 * A set of variables of which only a certain number can be chosen.
	 */
	public final class Exclusion extends BinaryConstraint {
		/**
		 * Set of variables
		 */
		private final Set<Integer> variableGroup;
		/**
		 * Number of variables that can be chosen
		 */
		private int allowed;

		/**
		 * Number of variables that have to be chosen
		 */
		private int required = 0;

		/**
		 * Creates a new Exclusion from the given variables. Applies already fixed
		 * variables and adds the exclusion to the collections
		 *
		 * @param variableGroup Variables in this exclusion
		 * @param allowed       Maximum number of variables to choose
		 * @param required      Required number of variables to choose
		 * @param name          Name of the exclusion
		 */
		private Exclusion(final Set<Integer> variableGroup, final int allowed, final int required, final String name) {
			super(name);
			if (required > allowed || required > variableGroup.size())
				throw new RuntimeException("Cannot fulfill number of required choices for this exclusion");
			this.variableGroup = variableGroup;
			this.allowed = allowed;
			this.required = required;
			this.fixVariables(BinaryILPProblem.this.positiveChoices, BinaryILPProblem.this.negativeChoices);
			if (this.isRelevant()) {
				BinaryILPProblem.this.exclusions.add(this);
				for (int id : this.variableGroup) {
					BinaryILPProblem.this.variableIdsToContainingConstraints.get(id).add(this);
				}
			}
		}

		@Override
		void generateILPConstraint() {
			if (this.variableGroup.size() > this.allowed) {
				// generate constraint for choosing at most allowed vars
				ILPLinearExpression expr = BinaryILPProblem.this.createLinearExpression();
				this.variableGroup.stream().forEach(v -> {
					expr.addTerm(v, 1);
				});
				ILPConstraint constr = new ILPConstraint(expr, Comparator.le, this.allowed, this.getName());
				BinaryILPProblem.this.addConstraint(constr);
				BinaryILPProblem.this.generatedConstraints.add(constr);
			}

			if (this.required > 0) {
				// generate constraint for choosing at least required vars
				ILPLinearExpression expr2 = BinaryILPProblem.this.createLinearExpression();
				this.variableGroup.stream().forEach(v -> {
					expr2.addTerm(v, -1);
				});
				ILPConstraint constr2 = new ILPConstraint(expr2, Comparator.le, -this.required, this.getName());
				BinaryILPProblem.this.addConstraint(constr2);
				BinaryILPProblem.this.generatedConstraints.add(constr2);
			}
		}

		@Override
		boolean fixVariable(final int id, final boolean choice) {
			if (this.variableGroup.remove(id)) {
				// remove yielded true, so it was contained
				if (choice) {
					--this.required;
					if (--this.allowed == 0) {
						for (int id2 : this.variableGroup) {
							// all other vars cannot be chosen
							BinaryILPProblem.this.fixVariable(id2, false);
						}
						this.setRelevant(false);
						return false;
					}
				} else if (this.variableGroup.size() < this.required)
					throw new RuntimeException("Cannot fulfill number of required choices for this exclusion");
			}

			if (this.variableGroup.isEmpty() || (this.required <= 0 && this.variableGroup.size() <= this.allowed)) {
				// no further variable choices required and all other vars can be chosen
				this.setRelevant(false);
				return false;
			}

			return true;
		}

		@Override
		boolean fixVariables(final Collection<Integer> positiveChoices, final Collection<Integer> negativeChoices) {
			if (this.variableGroup.removeAll(negativeChoices)) {
				if (this.variableGroup.size() < this.required)
					throw new RuntimeException("Cannot fulfill number of required choices for this exclusion");
			}

			for (int id : positiveChoices) {
				if (this.variableGroup.remove(id)) {
					this.required--;
					if (--this.allowed == 0) {
						for (int id2 : this.variableGroup) {
							// all other vars cannot be chosen
							BinaryILPProblem.this.fixVariable(id2, false);
						}
						this.setRelevant(false);
						return false;
					}
				}
			}

			if (this.variableGroup.isEmpty() || (this.required <= 0 && this.variableGroup.size() <= this.allowed)) {
				// no further variable choices required and all other vars can be chosen
				this.setRelevant(false);
				return false;
			}
			return true;
		}

		@Override
		public String toString() {
			List<String> termStrings = new LinkedList<>();
			this.variableGroup.stream().forEach((variableId) -> {
				termStrings.add(BinaryILPProblem.this.getVariable(variableId));
			});
			return "Exclusion(" + this.getName() + ", [" + this.required + ", " + this.allowed + "]" + ") {"
					+ String.join(", ", termStrings) + "}";
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + this.getOuterType().hashCode();
			result = prime * result + this.allowed;
			result = prime * result + ((this.variableGroup == null) ? 0 : this.variableGroup.hashCode());
			return result;
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (this.getClass() != obj.getClass())
				return false;
			Exclusion other = (Exclusion) obj;
			if (!this.getOuterType().equals(other.getOuterType()))
				return false;
			if (this.allowed != other.allowed)
				return false;
			if (this.variableGroup == null) {
				if (other.variableGroup != null)
					return false;
			} else if (!this.variableGroup.equals(other.variableGroup))
				return false;
			return true;
		}

		private BinaryILPProblem getOuterType() {
			return BinaryILPProblem.this;
		}

	}

	/**
	 * Generates ILP constraints for the defined boolean expressions
	 */
	private void generateContraints() {
		ILPSolver.logger.debug("Generating ILP constraints");
		this.removeConstraints(this.generatedConstraints);
		this.generatedConstraints.clear();

		for (Implication impl : this.implications) {
			if (impl.isRelevant()) {
				impl.generateILPConstraint();
			}
		}
		for (NegativeImplication negImpl : this.negativeImplications) {
			if (negImpl.isRelevant()) {
				negImpl.generateILPConstraint();
			}
		}
		for (Exclusion excl : this.exclusions) {
			if (excl.isRelevant()) {
				excl.generateILPConstraint();
			}
		}
	}

	@Override
	protected void applyLazyFixedVariables() {
		if (this.getLazyFixedVariables().isEmpty())
			return;
		this.removeConstraints(this.generatedConstraints);
		this.generatedConstraints.clear();
		while (!(this.lazyPositiveChoices.isEmpty() && this.lazyNegativeChoices.isEmpty())) {
			var positiveChoices = new ArrayList<Integer>(this.lazyPositiveChoices);
			var negativeChoices = new ArrayList<Integer>(this.lazyNegativeChoices);
			this.lazyPositiveChoices.clear();
			this.lazyNegativeChoices.clear();
			for (int id : positiveChoices) {
				for (BinaryConstraint constraint : this.variableIdsToContainingConstraints.remove(id)) {
					if (constraint.isRelevant) {
						constraint.isRelevant = constraint.fixVariable(id, true);
					}
				}
			}
			for (int id : negativeChoices) {
				for (BinaryConstraint constraint : this.variableIdsToContainingConstraints.remove(id)) {
					if (constraint.isRelevant) {
						constraint.isRelevant = constraint.fixVariable(id, false);
					}
				}
			}
		}
		super.applyLazyFixedVariables();
	}

	@Override
	int createNewVariable(final String variableName) {
		int variableId = super.createNewVariable(variableName);
		this.variableIdsToContainingConstraints.put(variableId, new LinkedList<>());
		return variableId;
	}

	/**
	 * Sets the variable to the given value
	 *
	 * @param variableName Name of the variable to fix
	 * @param choice       Value of the variable
	 */
	public void fixVariable(final String variableName, final boolean choice) {
		this.fixVariable(this.getVariableId(variableName), choice);
	}

	/**
	 * Sets the variable to the given value
	 *
	 * @param variableId ID of the variable to fix
	 * @param choice     Value of the variable
	 */
	protected void fixVariable(final int variableId, final boolean choice) {
		super.fixVariable(variableId, choice ? 1 : 0);
		if (choice) {
			if (!this.positiveChoices.contains(variableId)) {
				this.positiveChoices.add(variableId);
				this.lazyPositiveChoices.add(variableId);
			}
		} else {
			if (!this.negativeChoices.contains(variableId)) {
				this.negativeChoices.add(variableId);
				this.lazyNegativeChoices.add(variableId);
			}
		}
	}

	@Override
	public void fixVariable(final String variableName, final int value) {
		switch (value) {
		case 1:
			this.fixVariable(variableName, true);
			break;
		case 0:
			this.fixVariable(variableName, false);
			break;
		default:
			throw new IllegalArgumentException("Only 0 or 1 are supported in binary ILP problems");
		}
	}

	public Boolean getFixedBooleanVariable(final String variableName) {
		return this.getFixedBooleanVariable(this.getVariableId(variableName));
	}

	protected Boolean getFixedBooleanVariable(final int variable) {
		Integer value = this.getFixedVariable(variable);
		if (value == null)
			return null;
		switch (value) {
		case 1:
			return true;
		case 0:
			return false;
		default:
			throw new IllegalArgumentException("Only 0 or 1 are supported in binary ILP problems");
		}
	}

	/**
	 * Adds a constraint of the form x ^ y -> a v b <br>
	 * If all of the variables on the left side is chosen, one of the variables on
	 * the right side has to be chosen as well.
	 *
	 * @param leftSide  The names of the variables on the left side of the
	 *                  implication
	 * @param rightSide The names of the variables on the right side of the
	 *                  implication
	 * @param name      The name of the implication
	 * @return The implication that has been created
	 */
	public Implication addImplication(final Stream<String> leftSide, final Stream<String> rightSide,
			final String name) {
		return new Implication(//
				leftSide.map(s -> this.getVariableId(s)).collect(Collectors.toSet()), //
				rightSide.map(s -> this.getVariableId(s)).collect(Collectors.toSet()), //
				name);
	}

	/**
	 * Adds a constraint for a set of variables of which only a certain number can
	 * be chosen.
	 *
	 * @param variables The variables contained in the exclusion
	 * @param name      The name of the exclusion
	 * @param allowed   The number of variables that can be chosen
	 * @return The created exclusion
	 */
	public Exclusion addExclusion(final Stream<String> variables, final String name, final int allowed) {
		return this.addExclusion(variables, name, allowed, 0);
	}

	/**
	 * Adds a constraint for a set of variables of which only a certain number can
	 * be chosen.
	 *
	 * @param variables The variables contained in the exclusion
	 * @param name      The name of the exclusion
	 * @param allowed   The number of variables that can be chosen
	 * @param required  The minimum number of variables that have to be chosen
	 * @return The created exclusion
	 */
	public Exclusion addExclusion(final Stream<String> variables, final String name, final int allowed,
			final int required) {
		return new Exclusion(variables.map(this::getVariableId).collect(Collectors.toSet()), //
				allowed, required, name);
	}

	/**
	 * Adds a constraint for a set of variables of which only one can be chosen
	 *
	 * @param variables The variables contained in the exclusion
	 * @param name      The name of the exclusion
	 * @return The created exclusion
	 */
	public Exclusion addExclusion(final Stream<String> variables, final String name) {
		return this.addExclusion(variables, name, 1);
	}

	/**
	 * Adds a negative implication: A constraint of the form not(x V y) -> not(a) ^
	 * not(b) <br>
	 * If none of the variables on the left side is chosen, none of the variables on
	 * the right side can be chosen
	 *
	 * @param leftSide  Variables on the left side of the implication
	 * @param rightSide Variables on the right side of the implication
	 * @param name      The name of the implication
	 * @return the created implication
	 */
	public NegativeImplication addNegativeImplication(final Stream<String> leftSide, final Stream<String> rightSide,
			final String name) {
		return new NegativeImplication(//
				leftSide.map(this::getVariableId).collect(Collectors.toSet()),
				rightSide.map(this::getVariableId).collect(Collectors.toSet()), //
				name);
	}

	@Override
	public Collection<ILPConstraint> getConstraints() {
		this.applyLazyFixedVariables();
		if (this.generatedConstraints.isEmpty()) {
			this.generateContraints();
		}
		return super.getConstraints();
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		for (Exclusion excl : this.exclusions) {
			if (excl.isRelevant()) {
				b.append("\n" + excl);
			}
		}
		for (Implication impl : this.implications) {
			if (impl.isRelevant()) {
				b.append("\n" + impl);
			}
		}
		for (NegativeImplication impl : this.negativeImplications) {
			if (impl.isRelevant()) {
				b.append("\n" + impl);
			}
		}

		return super.toString() + b.toString();
	}
}
