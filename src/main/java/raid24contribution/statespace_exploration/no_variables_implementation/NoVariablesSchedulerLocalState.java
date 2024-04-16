package raid24contribution.statespace_exploration.no_variables_implementation;

import java.util.List;
import raid24contribution.statespace_exploration.EvaluationContext;
import raid24contribution.statespace_exploration.LocalState;
import raid24contribution.statespace_exploration.standard_implementations.BinaryAbstractedValue;


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
