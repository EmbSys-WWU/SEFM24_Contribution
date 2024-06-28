package de.tub.pes.syscir.statespace_exploration.some_variables_implementation;

import de.tub.pes.syscir.statespace_exploration.AbstractedValue;
import de.tub.pes.syscir.statespace_exploration.EvaluationContext;
import de.tub.pes.syscir.statespace_exploration.LocalState;
import de.tub.pes.syscir.statespace_exploration.standard_implementations.LocalVariable;
import de.tub.pes.syscir.statespace_exploration.standard_implementations.VariableHolder;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class SomeVariablesSchedulerState<ValueT extends AbstractedValue<ValueT, ?, ?>>
extends LocalState<SomeVariablesSchedulerState<ValueT>, ValueT> implements VariableHolder<LocalVariable<?>, ValueT> {

    private final Map<LocalVariable<?>, ValueT> variableValues;

    /**
     * Constructs a new, mutable SomeVariablesSchedulerState with the given execution stack and with the
     * given variable values.
     * <p>
     * The parameters are stored in the newly created object as is, without being copied. The lists must
     * be modifiable. Care must be taken not to modify them externally, especially after this state has
     * been locked.
     * 
     * @param executionStack where in the program the scheduler is currently residing (the last element
     *        in the list is at the top of the execution stack)
     * @param variableValues the values of all stored global variables
     */
    public SomeVariablesSchedulerState(List<EvaluationContext<ValueT>> executionStack,
            Map<LocalVariable<?>, ValueT> variableValues) {
        super(executionStack);

        this.variableValues = Objects.requireNonNull(variableValues);
    }

    /**
     * Constructs a new, mutable SomeVariablesSchedulerStatewith the given execution stack. No variables
     * are initially stored.
     * <p>
     * The parameters are stored in the newly created object as is, without being copied. The lists must
     * be modifiable. Care must be taken not to modify them externally, especially after this state has
     * been locked.
     * 
     * @param executionStack where in the program the scheduler is currently residing (the last element
     *        in the list is at the top of the execution stack)
     */
    public SomeVariablesSchedulerState(List<EvaluationContext<ValueT>> executionStack) {
        this(executionStack, new LinkedHashMap<>());
    }

    /**
     * Constructs a new, mutable copy of the given SomeVariablesSchedulerState.
     * 
     * @param copyOf the state to copy
     */
    public SomeVariablesSchedulerState(SomeVariablesSchedulerState<ValueT> copyOf) {
        super(copyOf);

        this.variableValues = new LinkedHashMap<>(copyOf.variableValues);
    }

    @Override
    public Map<LocalVariable<?>, ValueT> getVariableValues() {
        return Collections.unmodifiableMap(this.variableValues);
    }

    @Override
    public void setVariableValue(LocalVariable<?> variable, ValueT value) {
        requireNotLocked();
        resetHashCode();
        this.variableValues.put(variable, value);
    }

    @Override
    public void deleteVariableValue(LocalVariable<?> variable) {
        requireNotLocked();
        resetHashCode();
        this.variableValues.remove(variable);
    }

    @Override
    public SomeVariablesSchedulerState<ValueT> unlockedClone() {
        return new SomeVariablesSchedulerState<>(this);
    }

    @Override
    public int hashCodeInternal() {
        int result = super.hashCodeInternal();
        result = result * 31 + this.variableValues.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!super.equals(other)) {
            return false;
        }

        return this.variableValues.equals(((SomeVariablesSchedulerState<?>) other).variableValues);
    }

    @Override
    public String toString() {
        return super.toString() + " variables " + this.variableValues.toString();
    }
}
