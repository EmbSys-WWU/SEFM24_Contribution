package de.tub.pes.syscir.statespace_exploration.some_variables_implementation;

import de.tub.pes.syscir.statespace_exploration.AbstractedValue;
import de.tub.pes.syscir.statespace_exploration.EvaluationContext;
import de.tub.pes.syscir.statespace_exploration.ProcessBlocker;
import de.tub.pes.syscir.statespace_exploration.ProcessState;
import de.tub.pes.syscir.statespace_exploration.standard_implementations.LocalVariable;
import de.tub.pes.syscir.statespace_exploration.standard_implementations.VariableHolder;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * An abstraction of the process-local state of a SystemC design keeping the values of some local
 * variables.
 *
 * @author Jonas Becker-Kupczok
 *
 * @param <ValueT> the type of abstracted value used in the analysis
 */
public class SomeVariablesProcessState<ValueT extends AbstractedValue<ValueT, ?, ?>>
extends ProcessState<SomeVariablesProcessState<ValueT>, ValueT> implements VariableHolder<LocalVariable<?>, ValueT> {

    private Map<LocalVariable<?>, ValueT> variableValues;

    /**
     * Constructs a new, mutable SomeVariablesProcessState waiting for the given {@link ProcessBlocker}
     * at the given execution stack and with the given variable values.
     * <p>
     * The parameters are stored in the newly created object as is, without being copied. The lists must
     * be modifiable. Care must be taken not to modify them externally, especially after this state has
     * been locked.
     * 
     * @param waitingFor what this process is waiting for (null means it's ready to be scheduled)
     * @param executionStack where in the program this process is currently waiting (the last element in
     *        the list is at the top of the execution stack)
     * @param variableValues the values of all stored global variables
     */
    public SomeVariablesProcessState(ProcessBlocker waitingFor, List<EvaluationContext<ValueT>> executionStack,
            Map<LocalVariable<?>, ValueT> variableValues) {
        super(waitingFor, executionStack);

        this.variableValues = variableValues;
    }

    /**
     * Constructs a new, mutable SomeVariablesProcessState waiting for the given {@link ProcessBlocker}
     * at the given execution stack. No variables are initially stored.
     * <p>
     * The parameters are stored in the newly created object as is, without being copied. The lists must
     * be modifiable. Care must be taken not to modify them externally, especially after this state has
     * been locked.
     * 
     * @param waitingFor what this process is waiting for (null means it's ready to be scheduled)
     * @param executionStack where in the program this process is currently waiting (the last element in
     *        the list is at the top of the execution stack)
     */
    public SomeVariablesProcessState(ProcessBlocker waitingFor, List<EvaluationContext<ValueT>> executionStack) {
        this(waitingFor, executionStack, new LinkedHashMap<>());
    }

    /**
     * Constructs a new, mutable copy of the given SomeVariablesProcessState.
     * 
     * @param copyOf the state to copy
     */
    public SomeVariablesProcessState(SomeVariablesProcessState<ValueT> copyOf) {
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
    public SomeVariablesProcessState<ValueT> unlockedClone() {
        return new SomeVariablesProcessState<>(this);
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

        return this.variableValues.equals(((SomeVariablesProcessState<?>) other).variableValues);
    }

    @Override
    public String toString() {
        return super.toString() + " vars " + this.variableValues.toString();
    }

}
