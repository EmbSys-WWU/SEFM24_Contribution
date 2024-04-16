package raid24contribution.statespace_exploration.no_variables_implementation;

import java.util.Set;
import raid24contribution.sc_model.SCProcess;
import raid24contribution.sc_model.SCSystem;
import raid24contribution.sc_model.expressions.Expression;
import raid24contribution.sc_model.variables.SCClassInstance;
import raid24contribution.statespace_exploration.ProcessTransitionResult;
import raid24contribution.statespace_exploration.EventBlocker.Event;
import raid24contribution.statespace_exploration.standard_implementations.BaseProcess;
import raid24contribution.statespace_exploration.standard_implementations.BinaryAbstractedValue;
import raid24contribution.statespace_exploration.transition_informations.NoInformation;


public class NoVariablesNoInformationProcess
extends
BaseProcess<NoVariablesNoInformationProcess, NoVariablesGlobalState, NoVariablesProcessState, BinaryAbstractedValue<?>, NoInformation> {

    public NoVariablesNoInformationProcess(SCSystem scSystem, SCProcess scProcess, SCClassInstance scClassInstance,
            NoVariablesNoInformationScheduler scheduler) {
        super(scSystem, scProcess, scClassInstance, scheduler, NoInformation.getHandler());
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
            ProcessTransitionResult<NoVariablesGlobalState, NoVariablesProcessState, NoInformation, NoVariablesNoInformationProcess> currentState,
            NoVariablesProcessState localState, Expression expression) {
        return getNonDeterminedValue();
    }

    @Override
    public Set<Event> getSensitivities(
            ProcessTransitionResult<NoVariablesGlobalState, NoVariablesProcessState, NoInformation, NoVariablesNoInformationProcess> currentState,
            NoVariablesProcessState localState) {
        if (getSCProcess().getSensitivity().isEmpty()) {
            return Set.of();
        }
        throw new InsufficientPrecisionException();
    }
}
