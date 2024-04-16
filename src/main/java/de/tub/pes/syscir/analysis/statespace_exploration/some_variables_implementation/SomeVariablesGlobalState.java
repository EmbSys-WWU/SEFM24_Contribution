package de.tub.pes.syscir.analysis.statespace_exploration.some_variables_implementation;

import de.tub.pes.syscir.analysis.statespace_exploration.AbstractedValue;
import de.tub.pes.syscir.analysis.statespace_exploration.EventBlocker.Event;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.GlobalVariable;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.VariableHolder;
import de.tub.pes.syscir.analysis.statespace_exploration.GlobalState;
import de.tub.pes.syscir.analysis.statespace_exploration.TimedBlocker;
import de.tub.pes.syscir.analysis.util.WrappedSCClassInstance;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * An abstraction of the global state of a SystemC design keeping the values of some global
 * variables.
 *
 * @author Jonas Becker-Kupczok
 *
 * @param <ValueT> the type of abstracted value used in the analysis
 */
public class SomeVariablesGlobalState<ValueT extends AbstractedValue<ValueT, ?, ?>>
        extends GlobalState<SomeVariablesGlobalState<ValueT>>
implements VariableHolder<GlobalVariable<?, ?>, ValueT> {

    private Map<GlobalVariable<?, ?>, ValueT> variableValues;


    /**
     * Constructs a new, mutable SomeVariablesGlobalState with the given event scheduler states,
     * requested updates, simulation stoppage and variable values.
     * <p>
     * The map and set parameters are stored in the newly created object as is, without being copied.
     * They must be modifiable. Care must be taken not to modify them externally, especially after this
     * state has been locked.
     * 
     * @param eventStates map of each event to its scheduler state (no mapping means not pending, null
     *        values are not allowed)
     * @param requestedUpdates set of ports for which updates have been requested
     * @param simulationStopped whether or not sc_stop() has been called
     * @param variableValues the values of all stored global variables
     */
    public SomeVariablesGlobalState(Map<Event, TimedBlocker> eventStates, Set<WrappedSCClassInstance> requestedUpdates,
            boolean simulationStopped, Map<GlobalVariable<?, ?>, ValueT> variableValues) {
        super(eventStates, requestedUpdates, simulationStopped);

        this.variableValues = variableValues;
    }

    /**
     * Constructs a new, mutable copy of the given SomeVariablesGlobalState.
     * 
     * @param copyOf the state to copy
     */
    public SomeVariablesGlobalState(SomeVariablesGlobalState<ValueT> copyOf) {
        super(copyOf);

        this.variableValues = new LinkedHashMap<>(copyOf.variableValues);
    }

    @Override
    public Map<GlobalVariable<?, ?>, ValueT> getVariableValues() {
        return Collections.unmodifiableMap(this.variableValues);
    }

    @Override
    public void setVariableValue(GlobalVariable<?, ?> variable, ValueT value) {
        requireNotLocked();
        resetHashCode();
        this.variableValues.put(variable, value);
    }

    @Override
    public void deleteVariableValue(GlobalVariable<?, ?> variable) {
        requireNotLocked();
        resetHashCode();
        this.variableValues.remove(variable);
    }

    @Override
    public SomeVariablesGlobalState<ValueT> unlockedClone() {
        return new SomeVariablesGlobalState<>(this);
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

        return this.variableValues.equals(((SomeVariablesGlobalState<?>) other).variableValues);
    }

    @Override
    public String toString() {
        return super.toString() + " vars " + this.variableValues.toString();
    }

}
