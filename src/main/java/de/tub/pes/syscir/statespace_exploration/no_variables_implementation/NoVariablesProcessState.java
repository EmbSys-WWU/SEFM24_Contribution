package de.tub.pes.syscir.statespace_exploration.no_variables_implementation;

import de.tub.pes.syscir.statespace_exploration.EvaluationContext;
import de.tub.pes.syscir.statespace_exploration.ProcessBlocker;
import de.tub.pes.syscir.statespace_exploration.ProcessState;
import de.tub.pes.syscir.statespace_exploration.standard_implementations.BinaryAbstractedValue;
import java.util.List;


public class NoVariablesProcessState extends ProcessState<NoVariablesProcessState, BinaryAbstractedValue<?>> {

    public NoVariablesProcessState(ProcessBlocker waitingFor,
            List<EvaluationContext<BinaryAbstractedValue<?>>> executionStack) {
        super(waitingFor, executionStack);
    }

    protected NoVariablesProcessState(NoVariablesProcessState copyOf) {
        super(copyOf);
    }

    @Override
    public NoVariablesProcessState unlockedClone() {
        return new NoVariablesProcessState(this);
    }

}
