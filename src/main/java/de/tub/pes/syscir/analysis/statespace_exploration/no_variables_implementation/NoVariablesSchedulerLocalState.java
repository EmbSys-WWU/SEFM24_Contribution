package de.tub.pes.syscir.analysis.statespace_exploration.no_variables_implementation;

import de.tub.pes.syscir.analysis.statespace_exploration.EvaluationContext;
import de.tub.pes.syscir.analysis.statespace_exploration.LocalState;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.BinaryAbstractedValue;
import java.util.List;


public class NoVariablesSchedulerLocalState
extends LocalState<NoVariablesSchedulerLocalState, BinaryAbstractedValue<?>> {

    public NoVariablesSchedulerLocalState(List<EvaluationContext<BinaryAbstractedValue<?>>> executionStack) {
        super(executionStack);
    }

    public NoVariablesSchedulerLocalState(NoVariablesSchedulerLocalState copyOf) {
        super(copyOf);
    }

    @Override
    public NoVariablesSchedulerLocalState unlockedClone() {
        return new NoVariablesSchedulerLocalState(this);
    }

}
