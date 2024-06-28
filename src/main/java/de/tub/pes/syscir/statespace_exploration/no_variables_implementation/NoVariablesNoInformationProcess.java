package de.tub.pes.syscir.statespace_exploration.no_variables_implementation;

import de.tub.pes.syscir.statespace_exploration.EventBlocker.Event;
import de.tub.pes.syscir.statespace_exploration.ProcessTransitionResult;
import de.tub.pes.syscir.statespace_exploration.standard_implementations.BaseProcess;
import de.tub.pes.syscir.statespace_exploration.standard_implementations.BinaryAbstractedValue;
import de.tub.pes.syscir.statespace_exploration.transition_informations.NoInformation;
import de.tub.pes.syscir.sc_model.SCProcess;
import de.tub.pes.syscir.sc_model.SCSystem;
import de.tub.pes.syscir.sc_model.expressions.Expression;
import de.tub.pes.syscir.sc_model.variables.SCClassInstance;
import java.util.Set;


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
