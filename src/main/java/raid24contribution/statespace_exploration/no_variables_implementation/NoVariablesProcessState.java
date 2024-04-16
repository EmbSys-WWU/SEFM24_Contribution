package raid24contribution.statespace_exploration.no_variables_implementation;

import java.util.List;
import raid24contribution.statespace_exploration.EvaluationContext;
import raid24contribution.statespace_exploration.ProcessBlocker;
import raid24contribution.statespace_exploration.ProcessState;
import raid24contribution.statespace_exploration.standard_implementations.BinaryAbstractedValue;


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
