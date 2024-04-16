package raid24contribution.statespace_exploration.some_variables_implementation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import raid24contribution.sc_model.SCSystem;
import raid24contribution.sc_model.expressions.Expression;
import raid24contribution.sc_model.expressions.FunctionCallExpression;
import raid24contribution.statespace_exploration.AbstractedValue;
import raid24contribution.statespace_exploration.EvaluationContext;
import raid24contribution.statespace_exploration.SchedulerTransitionResult;
import raid24contribution.statespace_exploration.EventBlocker.Event;
import raid24contribution.statespace_exploration.standard_implementations.BaseScheduler;
import raid24contribution.statespace_exploration.standard_implementations.BinaryAbstractedValue;
import raid24contribution.statespace_exploration.standard_implementations.ComposableTransitionInformation;
import raid24contribution.statespace_exploration.standard_implementations.GlobalVariable;
import raid24contribution.statespace_exploration.standard_implementations.InformationHandler;
import raid24contribution.statespace_exploration.standard_implementations.LocalVariable;
import raid24contribution.statespace_exploration.transition_informations.NoInformation;
import raid24contribution.statespace_exploration.transition_informations.VariablesReadWrittenInformation;
import raid24contribution.statespace_exploration.transition_informations.VariablesReadWrittenInformationHandler;
import raid24contribution.statespace_exploration.transition_informations.pdg.AdvancedPdgInformationHandler;
import raid24contribution.statespace_exploration.transition_informations.pdg.PdgInformation;
import raid24contribution.util.WrappedSCClassInstance;
import raid24contribution.util.WrappedSCFunction;

/**
 * Scheduler implementation storing the values of some variables and providing no additional
 * transition information.
 * 
 * @author Jonas Becker-Kupczok
 *
 * @param <ValueT> the type of abstracted value
 */
public abstract class SomeVariablesScheduler<ValueT extends AbstractedValue<ValueT, ?, ?>, InfoT extends ComposableTransitionInformation<InfoT>>
extends
BaseScheduler<SomeVariablesGlobalState<ValueT>, SomeVariablesProcessState<ValueT>, SomeVariablesSchedulerState<ValueT>, ValueT, InfoT, SomeVariablesProcess<ValueT, InfoT>> {

    /**
     * Returns an implementation of the {@link SomeVariablesScheduler} using a binary abstraction
     * ({@link BinaryAbstractedValue}) for values.
     * 
     * @param scSystem the SysCIR representation of the entire SystemC design
     * @param stopMode the stop mode for this scheduler
     * @param globalVariableStorageCondition the condition under which the values of global variables
     *        are stored
     * @param localVariableStorageCondition the condition under which the values of local variables are
     *        stored
     * @return an instance of {@link SomeVariablesScheduler}
     */
    public static SomeVariablesScheduler<BinaryAbstractedValue<?>, NoInformation> withBinaryAbstractionAndNoInformation(
            SCSystem scSystem, SimulationStopMode stopMode, Predicate<? super Event> eventConsiderationCondition,
            Predicate<? super GlobalVariable<?, ?>> globalVariableStorageCondition,
                    Predicate<? super LocalVariable<?>> localVariableStorageCondition) {
        return new SomeVariablesScheduler<BinaryAbstractedValue<?>, NoInformation>(scSystem, NoInformation.getHandler(),
                stopMode, eventConsiderationCondition, globalVariableStorageCondition, localVariableStorageCondition) {

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

    public static SomeVariablesScheduler<BinaryAbstractedValue<?>, VariablesReadWrittenInformation<BinaryAbstractedValue<Boolean>>> withBinaryAbstractionAndVariablesReadWrittenInformation(
            SCSystem scSystem, SimulationStopMode stopMode, Predicate<? super Event> eventConsiderationCondition,
            Predicate<? super GlobalVariable<?, ?>> globalVariableStorageCondition,
                    Predicate<? super LocalVariable<?>> localVariableStorageCondition) {
        return new SomeVariablesScheduler<BinaryAbstractedValue<?>, VariablesReadWrittenInformation<BinaryAbstractedValue<Boolean>>>(
                scSystem, new VariablesReadWrittenInformationHandler<>(x -> true, BinaryAbstractedValue::of), stopMode,
                eventConsiderationCondition, globalVariableStorageCondition, localVariableStorageCondition) {

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

    public static SomeVariablesScheduler<BinaryAbstractedValue<?>, PdgInformation> withBinaryAbstractionAndPdgInformation(
            SCSystem scSystem, SimulationStopMode stopMode, Predicate<? super Event> eventConsiderationCondition,
            Predicate<? super GlobalVariable<?, ?>> globalVariableStorageCondition,
                    Predicate<? super LocalVariable<?>> localVariableStorageCondition) {
        return new SomeVariablesScheduler<BinaryAbstractedValue<?>, PdgInformation>(scSystem,
                new AdvancedPdgInformationHandler<>(), stopMode, eventConsiderationCondition,
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

    private SomeVariablesExpressionHandler<SomeVariablesSchedulerState<ValueT>, ValueT, SchedulerTransitionResult<SomeVariablesGlobalState<ValueT>, SomeVariablesProcessState<ValueT>, SomeVariablesSchedulerState<ValueT>, InfoT, SomeVariablesProcess<ValueT, InfoT>>, InfoT> expressionHandler;

    /**
     * Creates a new StandardScheduler with the given stop mode.
     *
     * @param scSystem the SysCIR representation of the entire SystemC design
     * @param stopMode the stop mode for this scheduler
     * @param globalVariableStorageCondition the condition under which the values of global variables
     *        are stored
     * @param localVariableStorageCondition the condition under which the values of local variables are
     *        stored
     */
    public SomeVariablesScheduler(SCSystem scSystem, InformationHandler<InfoT, ValueT> informationHandler,
            SimulationStopMode stopMode, Predicate<? super Event> eventConsiderationCondition,
            Predicate<? super GlobalVariable<?, ?>> globalVariableStorageCondition,
                    Predicate<? super LocalVariable<?>> localVariableStorageCondition) {
        super(scSystem, informationHandler, stopMode, eventConsiderationCondition);

        this.expressionHandler = new SomeVariablesExpressionHandler<>(this, globalVariableStorageCondition,
                localVariableStorageCondition);
    }

    @Override
    public SomeVariablesSchedulerState<ValueT> constructLocalSchedulerState(WrappedSCClassInstance port,
            WrappedSCFunction entryPoint) {
        List<EvaluationContext<ValueT>> executionStack = new ArrayList<>();
        List<List<ValueT>> expressionValues = new ArrayList<>();
        expressionValues.add(new ArrayList<>());
        executionStack.add(new EvaluationContext<ValueT>(entryPoint, new ArrayList<>(), -1, expressionValues,
                getDeterminedValue(port)));
        return new SomeVariablesSchedulerState<>(executionStack);
    }

    @Override
    public SmallStepResult<SomeVariablesGlobalState<ValueT>, SomeVariablesProcessState<ValueT>, InfoT, SchedulerTransitionResult<SomeVariablesGlobalState<ValueT>, SomeVariablesProcessState<ValueT>, SomeVariablesSchedulerState<ValueT>, InfoT, SomeVariablesProcess<ValueT, InfoT>>> handleSpecialExpression(
            SchedulerTransitionResult<SomeVariablesGlobalState<ValueT>, SomeVariablesProcessState<ValueT>, SomeVariablesSchedulerState<ValueT>, InfoT, SomeVariablesProcess<ValueT, InfoT>> currentState,
            SomeVariablesSchedulerState<ValueT> localState, Expression expression, int comingFrom) {
        return this.expressionHandler.handleSpecialExpression(currentState, localState, expression, comingFrom);
    }

    @Override
    public ValueT aggregateExpressionValue(
            SchedulerTransitionResult<SomeVariablesGlobalState<ValueT>, SomeVariablesProcessState<ValueT>, SomeVariablesSchedulerState<ValueT>, InfoT, SomeVariablesProcess<ValueT, InfoT>> currentState,
            SomeVariablesSchedulerState<ValueT> localState, Expression expression) {
        return this.expressionHandler.aggregateExpressionValue(currentState, localState, expression);
    }

    @Override
    protected void functionCalled(FunctionCallExpression expression,
            SchedulerTransitionResult<SomeVariablesGlobalState<ValueT>, SomeVariablesProcessState<ValueT>, SomeVariablesSchedulerState<ValueT>, InfoT, SomeVariablesProcess<ValueT, InfoT>> currentState,
            SomeVariablesSchedulerState<ValueT> localState) {
        this.expressionHandler.functionCalled(expression, currentState, localState);
    }

    @Override
    protected void functionReturned(FunctionCallExpression expression,
            SchedulerTransitionResult<SomeVariablesGlobalState<ValueT>, SomeVariablesProcessState<ValueT>, SomeVariablesSchedulerState<ValueT>, InfoT, SomeVariablesProcess<ValueT, InfoT>> currentState,
            SomeVariablesSchedulerState<ValueT> localState) {
        this.expressionHandler.functionReturned(expression, currentState, localState);
    }

}
