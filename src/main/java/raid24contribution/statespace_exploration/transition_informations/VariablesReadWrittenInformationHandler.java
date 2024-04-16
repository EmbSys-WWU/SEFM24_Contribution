package raid24contribution.statespace_exploration.transition_informations;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import raid24contribution.sc_model.expressions.BinaryExpression;
import raid24contribution.sc_model.expressions.Expression;
import raid24contribution.sc_model.expressions.SCPortSCSocketExpression;
import raid24contribution.sc_model.expressions.SCVariableExpression;
import raid24contribution.statespace_exploration.AbstractedValue;
import raid24contribution.statespace_exploration.AnalyzedProcess;
import raid24contribution.statespace_exploration.EventBlocker;
import raid24contribution.statespace_exploration.EventBlocker.Event;
import raid24contribution.statespace_exploration.LocalState;
import raid24contribution.statespace_exploration.ProcessState;
import raid24contribution.statespace_exploration.TransitionResult;
import raid24contribution.statespace_exploration.some_variables_implementation.SomeVariablesExpressionHandler;
import raid24contribution.statespace_exploration.some_variables_implementation.SomeVariablesExpressionHandler.AccessedVariablesInformation;
import raid24contribution.statespace_exploration.standard_implementations.ExpressionCrawler;
import raid24contribution.statespace_exploration.standard_implementations.ExpressionCrawler.ExecutionConditions;
import raid24contribution.statespace_exploration.standard_implementations.InformationHandler;
import raid24contribution.statespace_exploration.standard_implementations.Variable;


public class VariablesReadWrittenInformationHandler<ValueT extends AbstractedValue<ValueT, BoolT, ?>, BoolT extends AbstractedValue<BoolT, BoolT, Boolean>>
        implements InformationHandler<VariablesReadWrittenInformation<BoolT>, ValueT> {
    
    private final Predicate<Variable<?, ?>> variableConsiderationPredicate;
    private final Function<Boolean, BoolT> determinedBoolGetter;
    
    public VariablesReadWrittenInformationHandler(Predicate<Variable<?, ?>> variableConsiderationPredicate,
            Function<Boolean, BoolT> determinedBoolGetter) {
        this.variableConsiderationPredicate = Objects.requireNonNull(variableConsiderationPredicate);
        this.determinedBoolGetter = Objects.requireNonNull(determinedBoolGetter);
    }
    
    @Override
    public VariablesReadWrittenInformation<BoolT> getNeutralInformation() {
        return new VariablesReadWrittenInformation<>();
    }
    
    @Override
    public <LocalStateT extends LocalState<LocalStateT, ValueT>> VariablesReadWrittenInformation<BoolT> handleStartOfCode(
            TransitionResult<?, ?, ?, VariablesReadWrittenInformation<BoolT>, ?> currentState, LocalStateT localState) {
        return currentState.transitionInformation();
    }
    
    @Override
    public <LocalStateT extends LocalState<LocalStateT, ValueT>> VariablesReadWrittenInformation<BoolT> handleExpressionEvaluation(
            Expression evaluated, int comingFrom,
            TransitionResult<?, ?, ?, VariablesReadWrittenInformation<BoolT>, ?> resultingState,
            LocalStateT localState) {
        if (evaluated instanceof SCVariableExpression || evaluated instanceof SCPortSCSocketExpression) {
            AccessedVariablesInformation readVariables =
                    localState.getStateInformation(SomeVariablesExpressionHandler.VARIABLES_READ_KEY);
            if (readVariables == null) {
                return resultingState.transitionInformation();
            }
            Map<Variable<?, ?>, BoolT> informationMap =
                    createInformationMap(readVariables, getCurrentCondition(localState));
            return resultingState.transitionInformation()
                    .concat(new VariablesReadWrittenInformation<>(informationMap, Map.of()));
        } else if (evaluated instanceof BinaryExpression be) {
            if (be.getOp().equals("=")) {
                AccessedVariablesInformation writtenVariables =
                        localState.getStateInformation(SomeVariablesExpressionHandler.VARIABLES_WRITTEN_KEY);
                if (writtenVariables == null) {
                    return resultingState.transitionInformation();
                }
                Map<Variable<?, ?>, BoolT> informationMap =
                        createInformationMap(writtenVariables, getCurrentCondition(localState));
                return resultingState.transitionInformation()
                        .concat(new VariablesReadWrittenInformation<>(Map.of(), informationMap));
            }
        }
        
        return resultingState.transitionInformation();
    }
    
    protected <LocalStateT extends LocalState<LocalStateT, ValueT>> BoolT getCurrentCondition(LocalStateT localState) {
        ExecutionConditions<BoolT> currentExecutionConditions =
                localState.getStateInformation(ExpressionCrawler.executionConditionsKey());
        if (currentExecutionConditions == null) {
            return this.determinedBoolGetter.apply(true);
        }
        
        return currentExecutionConditions.getConditions().stream().reduce(this.determinedBoolGetter.apply(true),
                (b1, b2) -> b1.getAbstractedLogic().and(b1, b2));
    }
    
    protected Map<Variable<?, ?>, BoolT> createInformationMap(AccessedVariablesInformation accessedVariables,
            BoolT condition) {
        Map<Variable<?, ?>, BoolT> result = new LinkedHashMap<>();
        for (Variable<?, ?> var : accessedVariables) {
            if (this.variableConsiderationPredicate.test(var)) {
                result.put(var, condition);
            }
        }
        return result;
    }
    
    @Override
    public VariablesReadWrittenInformation<BoolT> handleProcessWaitedForDelta(AnalyzedProcess<?, ?, ?, ?> process,
            ProcessState<?, ValueT> resultingState, VariablesReadWrittenInformation<BoolT> currentInformation) {
        return currentInformation;
    }
    
    @Override
    public VariablesReadWrittenInformation<BoolT> handleProcessWaitedForTime(AnalyzedProcess<?, ?, ?, ?> process,
            ProcessState<?, ValueT> resultingState, VariablesReadWrittenInformation<BoolT> currentInformation) {
        return currentInformation;
    }
    
    @Override
    public VariablesReadWrittenInformation<BoolT> handleProcessWaitedForEvents(AnalyzedProcess<?, ?, ?, ?> process,
            ProcessState<?, ValueT> resultingState, Set<Event> events, EventBlocker blockerBefore,
            VariablesReadWrittenInformation<BoolT> currentInformation) {
        return currentInformation;
    }
    
}
