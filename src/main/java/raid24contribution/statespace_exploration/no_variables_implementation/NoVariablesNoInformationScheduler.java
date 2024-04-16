package raid24contribution.statespace_exploration.no_variables_implementation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import raid24contribution.sc_model.SCSystem;
import raid24contribution.sc_model.expressions.Expression;
import raid24contribution.statespace_exploration.EvaluationContext;
import raid24contribution.statespace_exploration.SchedulerTransitionResult;
import raid24contribution.statespace_exploration.EventBlocker.Event;
import raid24contribution.statespace_exploration.standard_implementations.BaseScheduler;
import raid24contribution.statespace_exploration.standard_implementations.BinaryAbstractedValue;
import raid24contribution.statespace_exploration.transition_informations.NoInformation;
import raid24contribution.util.WrappedSCClassInstance;
import raid24contribution.util.WrappedSCFunction;


public class NoVariablesNoInformationScheduler extends
BaseScheduler<NoVariablesGlobalState, NoVariablesProcessState, NoVariablesSchedulerLocalState, BinaryAbstractedValue<?>, NoInformation, NoVariablesNoInformationProcess> {

    public NoVariablesNoInformationScheduler(SCSystem scSystem, SimulationStopMode stopMode,
            Predicate<? super Event> eventConsiderationCondition) {
        super(scSystem, NoInformation.getHandler(), stopMode, eventConsiderationCondition);
    }

    @Override
    public NoInformation getNeutralInformation() {
        return NoInformation.INSTANCE;
    }

    @Override
    public BinaryAbstractedValue<?> getNonDeterminedValue() {
        return BinaryAbstractedValue.empty();
    }

    @Override
    public BinaryAbstractedValue<?> getDeterminedValue(Object value) {
        return BinaryAbstractedValue.of(value);
    }

    @Override
    public BinaryAbstractedValue<?> aggregateExpressionValue(
            SchedulerTransitionResult<NoVariablesGlobalState, NoVariablesProcessState, NoVariablesSchedulerLocalState, NoInformation, NoVariablesNoInformationProcess> currentState,
            NoVariablesSchedulerLocalState localState, Expression expression) {
        return BinaryAbstractedValue.empty();
    }

    @Override
    public NoVariablesSchedulerLocalState constructLocalSchedulerState(WrappedSCClassInstance port,
            WrappedSCFunction entryPoint) {
        List<EvaluationContext<BinaryAbstractedValue<?>>> executionStack = new ArrayList<>();
        List<List<BinaryAbstractedValue<?>>> expressionValues = new ArrayList<>();
        expressionValues.add(new ArrayList<>());
        executionStack.add(new EvaluationContext<BinaryAbstractedValue<?>>(entryPoint, new ArrayList<>(), -1,
                expressionValues, BinaryAbstractedValue.of(port)));
        return new NoVariablesSchedulerLocalState(executionStack);
    }

}
