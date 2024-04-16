package de.tub.pes.syscir.analysis.statespace_exploration.some_variables_implementation;

import de.tub.pes.syscir.analysis.statespace_exploration.AbstractedValue;
import de.tub.pes.syscir.analysis.statespace_exploration.EventBlocker.Event;
import de.tub.pes.syscir.analysis.statespace_exploration.ProcessTransitionResult;
import de.tub.pes.syscir.analysis.statespace_exploration.Scheduler;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.BaseProcess;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.BinaryAbstractedValue;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.ComposableTransitionInformation;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.GlobalVariable;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.InformationHandler;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.LocalVariable;
import de.tub.pes.syscir.analysis.statespace_exploration.transition_informations.NoInformation;
import de.tub.pes.syscir.analysis.statespace_exploration.transition_informations.VariablesReadWrittenInformation;
import de.tub.pes.syscir.analysis.statespace_exploration.transition_informations.VariablesReadWrittenInformationHandler;
import de.tub.pes.syscir.analysis.statespace_exploration.transition_informations.pdg.AdvancedPdgInformationHandler;
import de.tub.pes.syscir.analysis.statespace_exploration.transition_informations.pdg.PdgInformation;
import de.tub.pes.syscir.sc_model.SCProcess;
import de.tub.pes.syscir.sc_model.SCSystem;
import de.tub.pes.syscir.sc_model.expressions.Expression;
import de.tub.pes.syscir.sc_model.expressions.FunctionCallExpression;
import de.tub.pes.syscir.sc_model.variables.SCClassInstance;
import de.tub.pes.syscir.sc_model.variables.SCEvent;
import de.tub.pes.syscir.sc_model.variables.SCPortEvent;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Process implementation storing the values of some variables and providing no additional
 * transition information.
 * 
 * @author Jonas Becker-Kupczok
 *
 * @param <ValueT> the type of abstracted value
 */
public abstract class SomeVariablesProcess<ValueT extends AbstractedValue<ValueT, ?, ?>, InfoT extends ComposableTransitionInformation<InfoT>>
extends
BaseProcess<SomeVariablesProcess<ValueT, InfoT>, SomeVariablesGlobalState<ValueT>, SomeVariablesProcessState<ValueT>, ValueT, InfoT> {

    /**
     * Returns an implementation of the {@link SomeVariablesProcess} using a binary abstraction
     * ({@link BinaryAbstractedValue}) for values.
     * 
     * @param scSystem the SysCIR representation of the entire SystemC design
     * @param scProcess the SysCIR process
     * @param scClassInstance owning instance of this process
     * @param scheduler the scheduler used in this analysis
     * @param globalVariableStorageCondition the condition under which the values of global variables
     *        are stored
     * @param localVariableStorageCondition the condition under which the values of local variables are
     *        stored
     * @return an instance of {@link SomeVariablesProcess}
     */
    public static SomeVariablesProcess<BinaryAbstractedValue<?>, NoInformation> withBinaryAbstractionAndNoInformation(
            SCSystem scSystem, SCProcess scProcess, SCClassInstance scClassInstance,
            Scheduler<SomeVariablesGlobalState<BinaryAbstractedValue<?>>, SomeVariablesProcessState<BinaryAbstractedValue<?>>, NoInformation, SomeVariablesProcess<BinaryAbstractedValue<?>, NoInformation>> scheduler,
            Predicate<? super GlobalVariable<?, ?>> globalVariableStorageCondition,
                    Predicate<? super LocalVariable<?>> localVariableStorageCondition) {
        return new SomeVariablesProcess<>(scSystem, scProcess, scClassInstance, scheduler, NoInformation.getHandler(),
                globalVariableStorageCondition, localVariableStorageCondition) {

            @Override
            public BinaryAbstractedValue<?> getNonDeterminedValue() {
                return BinaryAbstractedValue.empty();
            }

            @Override
            public BinaryAbstractedValue<?> getDeterminedValue(Object value) {
                return BinaryAbstractedValue.of(value);
            }

        };
    }

    public static SomeVariablesProcess<BinaryAbstractedValue<?>, VariablesReadWrittenInformation<BinaryAbstractedValue<Boolean>>> withBinaryAbstractionAndVariablesReadWrittenInformation(
            SCSystem scSystem, SCProcess scProcess, SCClassInstance scClassInstance,
            Scheduler<SomeVariablesGlobalState<BinaryAbstractedValue<?>>, SomeVariablesProcessState<BinaryAbstractedValue<?>>, VariablesReadWrittenInformation<BinaryAbstractedValue<Boolean>>, SomeVariablesProcess<BinaryAbstractedValue<?>, VariablesReadWrittenInformation<BinaryAbstractedValue<Boolean>>>> scheduler,
            Predicate<? super GlobalVariable<?, ?>> globalVariableStorageCondition,
                    Predicate<? super LocalVariable<?>> localVariableStorageCondition) {
        return new SomeVariablesProcess<>(scSystem, scProcess, scClassInstance, scheduler,
                new VariablesReadWrittenInformationHandler<>(x -> true, BinaryAbstractedValue::of),
                globalVariableStorageCondition, localVariableStorageCondition) {

            @Override
            public BinaryAbstractedValue<?> getNonDeterminedValue() {
                return BinaryAbstractedValue.empty();
            }

            @Override
            public BinaryAbstractedValue<?> getDeterminedValue(Object value) {
                return BinaryAbstractedValue.of(value);
            }

        };
    }

    public static SomeVariablesProcess<BinaryAbstractedValue<?>, PdgInformation> withBinaryAbstractionAndPdgInformation(
            SCSystem scSystem, SCProcess scProcess, SCClassInstance scClassInstance,
            Scheduler<SomeVariablesGlobalState<BinaryAbstractedValue<?>>, SomeVariablesProcessState<BinaryAbstractedValue<?>>, PdgInformation, SomeVariablesProcess<BinaryAbstractedValue<?>, PdgInformation>> scheduler,
            Predicate<? super GlobalVariable<?, ?>> globalVariableStorageCondition,
                    Predicate<? super LocalVariable<?>> localVariableStorageCondition) {
        return new SomeVariablesProcess<>(scSystem, scProcess, scClassInstance, scheduler,
                new AdvancedPdgInformationHandler<>(), globalVariableStorageCondition, localVariableStorageCondition) {

            @Override
            public BinaryAbstractedValue<?> getNonDeterminedValue() {
                return BinaryAbstractedValue.empty();
            }

            @Override
            public BinaryAbstractedValue<?> getDeterminedValue(Object value) {
                return BinaryAbstractedValue.of(value);
            }

        };
    }

    private SomeVariablesExpressionHandler<SomeVariablesProcessState<ValueT>, ValueT, ProcessTransitionResult<SomeVariablesGlobalState<ValueT>, SomeVariablesProcessState<ValueT>, InfoT, SomeVariablesProcess<ValueT, InfoT>>, InfoT> expressionHandler;

    /**
     * Constructs a new SomeVariableNoInformationProcess representing the given SysCIR process,
     * belonging to the given SysCIR class instance (i.e. the module instance) and using the given
     * scheduler. The given conditions are used to determine whether or not the value of a variable
     * shall be stored.
     * 
     * @param scSystem the SysCIR representation of the entire SystemC design
     * @param scProcess the SysCIR process
     * @param scClassInstance owning instance of this process
     * @param scheduler the scheduler used in this analysis
     * @param globalVariableStorageCondition the condition under which the values of global variables
     *        are stored
     * @param localVariableStorageCondition the condition under which the values of local variables are
     *        stored
     */
    public SomeVariablesProcess(SCSystem scSystem, SCProcess scProcess, SCClassInstance scClassInstance,
            Scheduler<SomeVariablesGlobalState<ValueT>, SomeVariablesProcessState<ValueT>, InfoT, SomeVariablesProcess<ValueT, InfoT>> scheduler,
            InformationHandler<InfoT, ValueT> informationHandler,
            Predicate<? super GlobalVariable<?, ?>> globalVariableStorageCondition,
                    Predicate<? super LocalVariable<?>> localVariableStorageCondition) {
        super(scSystem, scProcess, scClassInstance, scheduler, informationHandler);

        this.expressionHandler = new SomeVariablesExpressionHandler<>(this, globalVariableStorageCondition,
                localVariableStorageCondition);
    }

    @Override
    public Set<Event> getSensitivities(
            ProcessTransitionResult<SomeVariablesGlobalState<ValueT>, SomeVariablesProcessState<ValueT>, InfoT, SomeVariablesProcess<ValueT, InfoT>> currentState,
            SomeVariablesProcessState<ValueT> localState) {
        return getSensitivities(currentState.globalState());
    }

    /**
     * See {@link #getSensitivities(ProcessTransitionResult, SomeVariablesProcessState)}.
     * 
     * @param globalState the global state
     * @return the set of events the process is statically sensitive on
     */
    public Set<Event> getSensitivities(SomeVariablesGlobalState<ValueT> globalState) {
        Set<Event> result = new LinkedHashSet<>();

        for (SCEvent scEvent : getSCProcess().getSensitivity()) {
            ValueT eventValue = getEventValue(globalState, scEvent);
            if (!eventValue.isDetermined()) {
                throw new InsufficientPrecisionException();
            }
            result.add((Event) eventValue.get());
        }

        return result;
    }

    /**
     * See {@link SomeVariablesExpressionHandler#getEventValue(SomeVariablesGlobalState, SCPortEvent)}.
     * This method is meant only to find the events on which the process is statically sensitive (see
     * {@link #getSensitivities(SomeVariablesGlobalState)}), it may yield false results in other
     * contexts.
     *
     * @param globalState the global state
     * @param scEvent the SysCIR variable for the event on which the process is statically sensitive
     * @return the (abstracted) event value
     */
    public ValueT getEventValue(SomeVariablesGlobalState<ValueT> globalState, SCEvent scEvent) {
        if (!(scEvent instanceof SCPortEvent portEvent)) {
            ValueT eventValue = globalState.getValue(new GlobalVariable<>(getSCClassInstance(), scEvent),
                    this::getNonDeterminedValue);
            if (!eventValue.isDetermined()) {
                throw new InsufficientPrecisionException();
            }
            return eventValue;
        }

        return this.expressionHandler.getEventValue(globalState, this.expressionHandler.getEventVariable(portEvent));
    }

    @Override
    public SmallStepResult<SomeVariablesGlobalState<ValueT>, SomeVariablesProcessState<ValueT>, InfoT, ProcessTransitionResult<SomeVariablesGlobalState<ValueT>, SomeVariablesProcessState<ValueT>, InfoT, SomeVariablesProcess<ValueT, InfoT>>> handleSpecialExpression(
            ProcessTransitionResult<SomeVariablesGlobalState<ValueT>, SomeVariablesProcessState<ValueT>, InfoT, SomeVariablesProcess<ValueT, InfoT>> currentState,
            SomeVariablesProcessState<ValueT> localState, Expression expression, int comingFrom) {
        return this.expressionHandler.handleSpecialExpression(currentState, localState, expression, comingFrom);
    }

    @Override
    public ValueT aggregateExpressionValue(
            ProcessTransitionResult<SomeVariablesGlobalState<ValueT>, SomeVariablesProcessState<ValueT>, InfoT, SomeVariablesProcess<ValueT, InfoT>> currentState,
            SomeVariablesProcessState<ValueT> localState, Expression expression) {
        return this.expressionHandler.aggregateExpressionValue(currentState, localState, expression);
    }

    @Override
    protected void functionCalled(FunctionCallExpression expression,
            ProcessTransitionResult<SomeVariablesGlobalState<ValueT>, SomeVariablesProcessState<ValueT>, InfoT, SomeVariablesProcess<ValueT, InfoT>> currentState,
            SomeVariablesProcessState<ValueT> localState) {
        this.expressionHandler.functionCalled(expression, currentState, localState);
    }

    @Override
    protected void functionReturned(FunctionCallExpression expression,
            ProcessTransitionResult<SomeVariablesGlobalState<ValueT>, SomeVariablesProcessState<ValueT>, InfoT, SomeVariablesProcess<ValueT, InfoT>> currentState,
            SomeVariablesProcessState<ValueT> localState) {
        this.expressionHandler.functionReturned(expression, currentState, localState);
    }

}
