package de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations;

import static de.tub.pes.syscir.analysis.util.WrapperUtil.wrap;

import de.tub.pes.syscir.analysis.statespace_exploration.AbstractedValue;
import de.tub.pes.syscir.analysis.statespace_exploration.AnalyzedProcess;
import de.tub.pes.syscir.analysis.statespace_exploration.DeltaTimeBlocker;
import de.tub.pes.syscir.analysis.statespace_exploration.EvaluationContext;
import de.tub.pes.syscir.analysis.statespace_exploration.EventBlocker;
import de.tub.pes.syscir.analysis.statespace_exploration.EventBlocker.Event;
import de.tub.pes.syscir.analysis.statespace_exploration.GlobalState;
import de.tub.pes.syscir.analysis.statespace_exploration.LocalState;
import de.tub.pes.syscir.analysis.statespace_exploration.LocalState.StateInformation;
import de.tub.pes.syscir.analysis.statespace_exploration.LocalState.StateInformationKey;
import de.tub.pes.syscir.analysis.statespace_exploration.ProcessState;
import de.tub.pes.syscir.analysis.statespace_exploration.ProcessTransitionResult;
import de.tub.pes.syscir.analysis.statespace_exploration.RealTimedBlocker;
import de.tub.pes.syscir.analysis.statespace_exploration.Scheduler;
import de.tub.pes.syscir.analysis.statespace_exploration.TimedBlocker;
import de.tub.pes.syscir.analysis.statespace_exploration.TransitionInformation;
import de.tub.pes.syscir.analysis.statespace_exploration.TransitionResult;
import de.tub.pes.syscir.analysis.util.CollectionUtil;
import de.tub.pes.syscir.analysis.util.WrappedExpression;
import de.tub.pes.syscir.analysis.util.WrappedSCClassInstance;
import de.tub.pes.syscir.analysis.util.WrappedSCPortInstance;
import de.tub.pes.syscir.sc_model.SCConnectionInterface;
import de.tub.pes.syscir.sc_model.SCPort;
import de.tub.pes.syscir.sc_model.SCPortInstance;
import de.tub.pes.syscir.sc_model.SCSystem;
import de.tub.pes.syscir.sc_model.expressions.AccessExpression;
import de.tub.pes.syscir.sc_model.expressions.ArrayAccessExpression;
import de.tub.pes.syscir.sc_model.expressions.ArrayInitializerExpression;
import de.tub.pes.syscir.sc_model.expressions.AssertionExpression;
import de.tub.pes.syscir.sc_model.expressions.BinaryExpression;
import de.tub.pes.syscir.sc_model.expressions.BracketExpression;
import de.tub.pes.syscir.sc_model.expressions.BreakExpression;
import de.tub.pes.syscir.sc_model.expressions.CaseExpression;
import de.tub.pes.syscir.sc_model.expressions.ConstantExpression;
import de.tub.pes.syscir.sc_model.expressions.ContinueExpression;
import de.tub.pes.syscir.sc_model.expressions.DeleteExpression;
import de.tub.pes.syscir.sc_model.expressions.DoWhileLoopExpression;
import de.tub.pes.syscir.sc_model.expressions.EmptyExpression;
import de.tub.pes.syscir.sc_model.expressions.EndlineExpression;
import de.tub.pes.syscir.sc_model.expressions.EnumElementExpression;
import de.tub.pes.syscir.sc_model.expressions.EventNotificationExpression;
import de.tub.pes.syscir.sc_model.expressions.Expression;
import de.tub.pes.syscir.sc_model.expressions.ExpressionBlock;
import de.tub.pes.syscir.sc_model.expressions.ForLoopExpression;
import de.tub.pes.syscir.sc_model.expressions.FunctionCallExpression;
import de.tub.pes.syscir.sc_model.expressions.IfElseExpression;
import de.tub.pes.syscir.sc_model.expressions.LoopExpression;
import de.tub.pes.syscir.sc_model.expressions.NameExpression;
import de.tub.pes.syscir.sc_model.expressions.NewExpression;
import de.tub.pes.syscir.sc_model.expressions.OutputExpression;
import de.tub.pes.syscir.sc_model.expressions.QuestionmarkExpression;
import de.tub.pes.syscir.sc_model.expressions.RefDerefExpression;
import de.tub.pes.syscir.sc_model.expressions.ReturnExpression;
import de.tub.pes.syscir.sc_model.expressions.SCClassInstanceExpression;
import de.tub.pes.syscir.sc_model.expressions.SCDeltaCountExpression;
import de.tub.pes.syscir.sc_model.expressions.SCPortSCSocketExpression;
import de.tub.pes.syscir.sc_model.expressions.SCStopExpression;
import de.tub.pes.syscir.sc_model.expressions.SCTimeStampExpression;
import de.tub.pes.syscir.sc_model.expressions.SCVariableDeclarationExpression;
import de.tub.pes.syscir.sc_model.expressions.SCVariableExpression;
import de.tub.pes.syscir.sc_model.expressions.SCVariableNonDetSet;
import de.tub.pes.syscir.sc_model.expressions.SwitchExpression;
import de.tub.pes.syscir.sc_model.expressions.TimeUnitExpression;
import de.tub.pes.syscir.sc_model.expressions.UnaryExpression;
import de.tub.pes.syscir.sc_model.expressions.WhileLoopExpression;
import de.tub.pes.syscir.sc_model.variables.SCTIMEUNIT;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.SequencedMap;
import java.util.Set;
import java.util.function.Function;

/**
 * Abstract class that captures the functionality of stepping through a program one expression at a
 * time.
 * 
 * This class serves as a basis for {@link BaseProcess} and {@link BaseScheduler} which both have to
 * evaluate code.
 * 
 * @author Jonas Becker-Kupczok
 *
 * @param <GlobalStateT> the type of global state abstraction which this implementation can handle
 * @param <ProcessStateT> the type of process state abstraction which this implementation can handle
 * @param <LocalStateT> the type of local scheduler state abstraction which this implementation can handle
 *        (which may or may not be P)
 * @param <ValueT> the type of abstracted value which this implementation can handle
 * @param <TransitionResultT> the type of transition result which this implementation produces and uses internally
 * @param <InfoT> the type of additional transition information which this implementation can provide.
 */
public abstract class ExpressionCrawler<GlobalStateT extends GlobalState<GlobalStateT>, ProcessStateT extends ProcessState<ProcessStateT, ValueT>, LocalStateT extends LocalState<LocalStateT, ValueT>, TransitionResultT extends TransitionResult<TransitionResultT, GlobalStateT, ProcessStateT, InfoT, ProcessT>, ValueT extends AbstractedValue<ValueT, ?, ?>, InfoT extends ComposableTransitionInformation<InfoT>, ProcessT extends AnalyzedProcess<ProcessT, GlobalStateT, ProcessStateT, InfoT>> {

    /**
     * Exception indicating that the current state information ins insufficiently precise for the
     * analysis to continue.
     *
     * @author Jonas Becker-Kupczok
     *
     */
    public static class InsufficientPrecisionException extends RuntimeException {

        private static final long serialVersionUID = -1470717330359116133L;

        public InsufficientPrecisionException() {
            super();
        }

        public InsufficientPrecisionException(String message, Throwable cause) {
            super(message, cause);
        }

        public InsufficientPrecisionException(String message) {
            super(message);
        }

        public InsufficientPrecisionException(Throwable cause) {
            super(cause);
        }

    }

    /**
     * Record capturing the intermediate result of one expression step.
     * 
     * The list of transition results captures the possible transitions that can be made, endOfStep
     * signals whether the analysis reaches the end of an atomic block, and possiblyRepeatingStep
     * indicates whether or not this intermediate result should be stored to make sure that it's not
     * dealt with again if it appears multiple times.
     * 
     * @author Jonas Becker-Kupczok
     *
     * @param <G> the type of {@link GlobalState} used in the exploration
     * @param <P> the type of {@link ProcessState} used in the exploration
     * @param <T> the type of {@link TransitionInformation} supplied by the {@link AnalyzedProcess}
     */
    public static record SmallStepResult<G extends GlobalState<G>, P extends ProcessState<P, ?>, T extends ComposableTransitionInformation<T>, R extends TransitionResult<R, G, P, T, ?>>(
            List<R> transitions, boolean endOfStep, boolean possiblyRepeatingStep) {}

    // TODO: this is not very elegant and not totally correct either (because the value of a variable
    // used in a condition can change later on). it's also not necessary for my (Jonas) research. is it
    // good for anything else? or should it go?
    public static class ExecutionConditions<B extends AbstractedValue<B, B, Boolean>>
    implements StateInformation<ExecutionConditions<B>> {

        // one map per entry on call stack
        private List<SequencedMap<WrappedExpression, B>> conditions;

        public ExecutionConditions() {
            this.conditions = new ArrayList<>();
            addCall();
        }

        public ExecutionConditions(ExecutionConditions<B> copyOf) {
            this.conditions = new ArrayList<>(copyOf.conditions.size());
            for (int i = 0; i < copyOf.conditions.size(); i++) {
                this.conditions.add(new LinkedHashMap<>(copyOf.conditions.get(i)));
            }
        }

        @Override
        public ExecutionConditions<B> copy() {
            return new ExecutionConditions<>(this);
        }

        public void addCall() {
            this.conditions.add(new LinkedHashMap<>());
        }

        public void add(Expression expression, B condition) {
            this.conditions.getLast().merge(wrap(expression), condition,
                    (b1, b2) -> b1.getAbstractedLogic().and(b1, b2));
        }

        // inclusive
        public void removeUntil(Expression expression) {
            WrappedExpression we = wrap(expression);
            WrappedExpression removed;
            do {
                // because all additional state information is cleared at the end of AnalyzedProcess#makeStep, the
                // conditions may contain less layers then expected by the expression crawler
                if (this.conditions.isEmpty() || this.conditions.getLast().isEmpty()) {
                    break;
                }
                removed = this.conditions.getLast().pollLastEntry().getKey();
            } while (!we.equals(removed));
        }

        public void removeCall() {
            if (!this.conditions.isEmpty()) {
                this.conditions.removeLast();
            }
            if (this.conditions.isEmpty()) {
                addCall();
            }
        }

        public List<B> getConditions() {
            return this.conditions.stream().flatMap(map -> map.values().stream()).toList();
        }

        @Override
        public String toString() {
            return this.conditions.toString();
        }

    }

    private static final StateInformationKey<ExecutionConditions<?>> EXECUTION_CONDITION_KEY =
            new StateInformationKey<>();

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <B extends AbstractedValue<B, B, Boolean>> StateInformationKey<ExecutionConditions<B>> executionConditionsKey() {
        return (StateInformationKey) EXECUTION_CONDITION_KEY;
    }

    protected final SCSystem scSystem;
    protected final Scheduler<GlobalStateT, ProcessStateT, InfoT, ProcessT> scheduler;
    protected final InformationHandler<InfoT, ValueT> informationHandler;

    /**
     * Creates a new ExpressionCralwer analyzing the given SystemC design and using the given scheduler.
     * <p>
     * If this implementation is itself a scheduler, the according parameter may be null.
     *
     * @param scSystem a SysCIR SystemC design
     * @param scheduler a scheduler, or null if this object is a scheduler
     * @param informationHandler an information handler providing {@link TransitionInformation}
     */
    @SuppressWarnings("unchecked")
    public ExpressionCrawler(SCSystem scSystem, Scheduler<GlobalStateT, ProcessStateT, InfoT, ProcessT> scheduler,
            InformationHandler<InfoT, ValueT> informationHandler) {
        if (scheduler == null) {
            try {
                scheduler = (Scheduler<GlobalStateT, ProcessStateT, InfoT, ProcessT>) this;
            } catch (ClassCastException e) {
                throw new IllegalArgumentException(
                        "scheduler must be non-null if this is not a scheduler implementation");
            }
        }

        this.scSystem = scSystem;
        this.scheduler = scheduler;
        this.informationHandler = informationHandler;
    }

    /**
     * Returns the SysCIR SystemC design analyzed by this crawler.
     *
     * @return SysCIR SystemC design.
     */
    public SCSystem getSCSystem() {
        return this.scSystem;
    }

    /**
     * Returns the scheduler that is used in the state space exploration.
     *
     * @return the scheduler
     */
    public Scheduler<GlobalStateT, ProcessStateT, InfoT, ProcessT> getScheduler() {
        return this.scheduler;
    }

    /**
     * Returns the information handler that provides {@link TransitionInformation} for this crawler.
     *
     * @return the information handler
     */
    public InformationHandler<InfoT, ValueT> getInformationHandler() {
        return this.informationHandler;
    }

    /**
     * Returns a transition information representing no information, i.e. one that is neutral with
     * respect to {@link ComposableTransitionInformation#compose(ComposableTransitionInformation)}.
     * 
     * Delegates the call to the result of {@link #getInformationHandler()}.
     *
     * @return neutral transition information
     */
    public InfoT getNeutralInformation() {
        return getInformationHandler().getNeutralInformation();
    }

    /**
     * Returns the information describing the small step that just occured.
     * 
     * Delegates the call to
     * {@link InformationHandler#handleExpressionEvaluation(Expression, int, TransitionResult, LocalState)}.
     * 
     * @param evaluated the expression that was just evaluated
     * @param comingFrom from where the expression was entered
     * @param resultingState the result of the small step
     * @param localState the local part of the result
     * @return the information describing the step
     */
    InfoT getInformation(Expression evaluated, int comingFrom, TransitionResultT resultingState, LocalStateT localState) {
        return getInformationHandler().handleExpressionEvaluation(evaluated, comingFrom, resultingState, localState);
    }

    /**
     * Creates a SmallStepResult from a list of TransitionResults by getting the new
     * TransitionInformation from the InformationHandler and composing it onto the result.
     *
     * @param evaluated the expression that was just evaluated
     * @param comingFrom from where the expression was entered
     * @param resultingStates the results of the small step
     * @param endOfStep whether the analysis reached the end of an atomic block
     * @param possiblyRepeatingStep whether or not this intermediate result should be stored to make
     *        sure that it's not dealt with again if it appears multiple times
     * @return the SmallStepResult, with appropriate TransitionInformation
     */
    public SmallStepResult<GlobalStateT, ProcessStateT, InfoT, TransitionResultT> createSmallStepResult(
            Expression evaluated, int comingFrom,
            List<TransitionResultT> resultingStates, boolean endOfStep, boolean possiblyRepeatingStep) {
        resultingStates = new ArrayList<>(resultingStates);
        for (int i = 0; i < resultingStates.size(); i++) {
            TransitionResultT resultingState = resultingStates.get(i);
            LocalStateT localState = getLocalState(resultingState);
            resultingStates.set(i, resultingState
                    .replaceTransitionInformation(getInformation(evaluated, comingFrom, resultingState, localState)));
        }
        return new SmallStepResult<>(resultingStates, endOfStep, possiblyRepeatingStep);
    }

    /**
     * Creates a SmallStepResult from a list of TransitionResults by getting the new
     * TransitionInformation from the InformationHandler and composing it onto the result.
     *
     * @param evaluated the expression that was just evaluated
     * @param comingFrom from where the expression was entered
     * @param resultingStates the result of the small step
     * @param localState the local part of the result
     * @param endOfStep whether the analysis reached the end of an atomic block
     * @param possiblyRepeatingStep whether or not this intermediate result should be stored to make
     *        sure that it's not dealt with again if it appears multiple times
     * @return the SmallStepResult, with appropriate TransitionInformation
     */
    public SmallStepResult<GlobalStateT, ProcessStateT, InfoT, TransitionResultT> createSmallStepResult(
            Expression evaluated, int comingFrom, TransitionResultT resultingState,
            LocalStateT localState, boolean endOfStep, boolean possiblyRepeatingStep) {
        localState = localState == null ? getLocalState(resultingState) : localState;
        return new SmallStepResult<>(
                List.of(resultingState.replaceTransitionInformation(
                        getInformation(evaluated, comingFrom, resultingState, localState))),
                endOfStep, possiblyRepeatingStep);
    }

    public ProcessTransitionResult<GlobalStateT, ProcessStateT, InfoT, ProcessT> finalizeTransitionResult(
            ProcessTransitionResult<GlobalStateT, ProcessStateT, InfoT, ProcessT> result) {
        result = new ProcessTransitionResult<>(result.resultingState(),
                getInformationHandler().finalizeInformation(result.transitionInformation()));
        result.resultingState().getProcessStates().values().forEach(ProcessState::clearStateInformation);
        result.resultingState().lock();
        return result;
    }

    /**
     * Returns the local state covered by this crawler contained in the given state.
     *
     * @param currentState
     * @return local state
     */
    public abstract LocalStateT getLocalState(TransitionResultT currentState);

    /**
     * Returns an object representing a non-determined value.
     * 
     * @return non-determined value
     */
    public abstract ValueT getNonDeterminedValue();

    /**
     * Returns an object representing a determined value.
     * 
     * @return determined value
     */
    public abstract ValueT getDeterminedValue(Object value);

    /**
     * Makes a small step in the evaluation, i.e. one step in the abstract syntax tree where every
     * expression is an individual node.
     * 
     * This implementation first finds the current expression to be evaluated. It then calls
     * {@link #handleSpecialExpression(ProcessTransitionResult, Expression, int)} to allow subclasses to
     * interject, otherwise calling a function to handle the specific type of expression found.
     * <p>
     * A special case is if the evaluation currently resides directly in a function body. In this case,
     * {@link #handleFunctionBody(ProcessTransitionResult, int)} is called (without a call to
     * {@link #handleSpecialExpression(ProcessTransitionResult, Expression, int)}.
     * <p>
     * If evaluation of the expression only allows for one follow up state, the current state is usually
     * modified instead of cloned for efficiency. Otherwise, clones are created and then modified for
     * every additional possible transition.
     * <p>
     * If the evaluation reaches the end of an atomic block (usually by encountering a wait statement or
     * the end of the top level function), {@link SmallStepResult#endOfStep()} will be true, otherwise
     * it will be false.
     *
     * @param currentState the current state from which to make the step
     * @return the possible transitions and whether or not the end of an atomic block has been reached
     */
    public SmallStepResult<GlobalStateT, ProcessStateT, InfoT, TransitionResultT> makeSmallStep(
            TransitionResultT currentState) {
        LocalStateT localState = getLocalState(currentState);
        EvaluationContext<ValueT> currentLocation =
                localState.getExecutionStack().get(localState.getExecutionStack().size() - 1);
        int comingFrom = currentLocation.getComingFrom();

        if (currentLocation.getExpressionIndices().isEmpty()) {
            return handleFunctionBody(currentState, localState, comingFrom);
        }

        Expression nextExpression = currentLocation.getNextExpression();

        getInformationHandler().announceEvaluation(nextExpression, currentState, localState);

        SmallStepResult<GlobalStateT, ProcessStateT, InfoT, TransitionResultT> result =
                handleSpecialExpression(currentState, localState, nextExpression, comingFrom);
        if (result != null) {
            return result;
        }

        // End of function body
        if (nextExpression == null) {
            return returnFromFunction(currentState, localState, null, comingFrom);
        }

        // Constants
        if (nextExpression instanceof ConstantExpression ex) {
            return handleConstantExpression(currentState, localState, ex, comingFrom, ConstantExpression::getValue);
        } else if (nextExpression instanceof EndlineExpression ex) {
            return handleConstantExpression(currentState, localState, ex, comingFrom, x -> "\n");
        } else if (nextExpression instanceof EnumElementExpression ex) {
            return handleConstantExpression(currentState, localState, ex, comingFrom,
                    EnumElementExpression::getEnumElement);
        } else if (nextExpression instanceof SCClassInstanceExpression ex) {
            return handleConstantExpression(currentState, localState, ex, comingFrom,
                    e -> wrap(e.getInstance()));
        } else if (nextExpression instanceof SCDeltaCountExpression ex) {
            return handleConstantExpression(currentState, localState, ex, comingFrom, x -> DeltaTimeBlocker.INSTANCE);
        } else if (nextExpression instanceof SCPortSCSocketExpression ex) {
            return handleConstantExpression(currentState, localState, ex, comingFrom,
                    SCPortSCSocketExpression::getSCPortSCSocket);
        } else if (nextExpression instanceof SCVariableExpression ex) {
            return handleConstantExpression(currentState, localState, ex, comingFrom, SCVariableExpression::getVar);
        } else if (nextExpression instanceof TimeUnitExpression ex) {
            return handleConstantExpression(currentState, localState, ex, comingFrom, TimeUnitExpression::getTimeUnit);
        }

        // Ignored expressions
        if (nextExpression instanceof AssertionExpression ex) {
            return handleIgnoredExpression(currentState, localState, ex, comingFrom);
        } else if (nextExpression instanceof EmptyExpression ex) {
            return handleIgnoredExpression(currentState, localState, ex, comingFrom);
        } else if (nextExpression instanceof NameExpression ex) {
            return handleIgnoredExpression(currentState, localState, ex, comingFrom);
        } else if (nextExpression instanceof SCTimeStampExpression ex) {
            return handleIgnoredExpression(currentState, localState, ex, comingFrom);
        }

        // Bottom-up evaluated expressions
        if (nextExpression instanceof AccessExpression ex) {
            return handleBottomUpExpression(currentState, localState, ex, comingFrom);
        } else if (nextExpression instanceof ArrayAccessExpression ex) {
            return handleBottomUpExpression(currentState, localState, ex, comingFrom);
        } else if (nextExpression instanceof ArrayInitializerExpression ex) {
            return handleBottomUpExpression(currentState, localState, ex, comingFrom);
        } else if (nextExpression instanceof BinaryExpression ex) {
            return handleBottomUpExpression(currentState, localState, ex, comingFrom);
        } else if (nextExpression instanceof BracketExpression ex) {
            return handleBracketExpression(currentState, localState, ex, comingFrom);
        } else if (nextExpression instanceof DeleteExpression ex) {
            return handleBottomUpExpression(currentState, localState, ex, comingFrom);
        } else if (nextExpression instanceof ExpressionBlock ex) {
            return handleBottomUpExpression(currentState, localState, ex, comingFrom);
        } else if (nextExpression instanceof NewExpression ex) {
            return handleBottomUpExpression(currentState, localState, ex, comingFrom);
        } else if (nextExpression instanceof OutputExpression ex) {
            return handleBottomUpExpression(currentState, localState, ex, comingFrom);
        } else if (nextExpression instanceof RefDerefExpression ex) {
            return handleBottomUpExpression(currentState, localState, ex, comingFrom);
        } else if (nextExpression instanceof SCVariableDeclarationExpression ex) {
            return handleBottomUpExpression(currentState, localState, ex, comingFrom);
        } else if (nextExpression instanceof SCVariableNonDetSet ex) {
            return handleBottomUpExpression(currentState, localState, ex, comingFrom);
        } else if (nextExpression instanceof UnaryExpression ex) {
            return handleBottomUpExpression(currentState, localState, ex, comingFrom);
        }

        // Control structures
        if (nextExpression instanceof IfElseExpression ex) {
            return handleIfElseExpression(currentState, localState, ex, comingFrom);
        } else if (nextExpression instanceof WhileLoopExpression ex) {
            return handleWhileLoopExpression(currentState, localState, ex, comingFrom);
        } else if (nextExpression instanceof DoWhileLoopExpression ex) {
            return handleWhileLoopExpression(currentState, localState, ex, comingFrom);
        } else if (nextExpression instanceof ForLoopExpression ex) {
            return handleForLoopExpression(currentState, localState, ex, comingFrom);
        } else if (nextExpression instanceof SwitchExpression ex) {
            return handleSwitchExpression(currentState, localState, ex, comingFrom);
        } else if (nextExpression instanceof CaseExpression ex) {
            return handleCaseExpression(currentState, localState, ex, comingFrom);
        } else if (nextExpression instanceof BreakExpression ex) {
            return handleBreakExpression(currentState, localState, ex, comingFrom);
        } else if (nextExpression instanceof ContinueExpression ex) {
            return handleContinueExpression(currentState, localState, ex, comingFrom);
        }
        // TODO GoalAnnotation, continue, goto

        // Function calls
        if (nextExpression instanceof FunctionCallExpression ex) {
            return handleFunctionCallExpression(currentState, localState, ex, comingFrom);
        } else if (nextExpression instanceof ReturnExpression ex) {
            return handleReturnExpression(currentState, localState, ex, comingFrom);
        } else if (nextExpression instanceof EventNotificationExpression ex) {
            return handleEventNotificationExpression(currentState, localState, ex, comingFrom);
        } else if (nextExpression instanceof SCStopExpression ex) {
            return handleSCStopExpression(currentState, localState, ex, comingFrom);
        }

        // Other types of expressions
        return handleOtherExpression(currentState, localState, nextExpression, comingFrom);

        // TODO how to deal with SocketFunctionCallExpression? simply "not supported" for now?
    }

    /**
     * Returns the next expression to be executed given the execution stack.
     *
     * @param executionStack an execution stack on this process
     * @return the next expression of this process to be executed
     */
    public Expression getNextExpression(List<EvaluationContext<ValueT>> executionStack) {
        return executionStack.getLast().getNextExpression();
    }

    /**
     * Returns the abstracted evaluation result of the child of the currently evaluated expression at
     * the given index.
     * 
     * @param currentState the current state of the evaluation
     * @param indexOfChild the index of the child whose value is requested
     * @return the evaluation result of that child
     */
    public ValueT getValueOfChild(TransitionResultT currentState, LocalStateT localState, int indexOfChild) {
        return getValueOfExpression(currentState, localState, 0, indexOfChild);
    }

    /**
     * Returns the abstracted evaluation result of a child of some expression that has not yet been
     * fully evaluated.
     * 
     * @param currentState the current state of the evaluation
     * @param levelsAbove how many levels the considered parent expression is above the currently
     *        evaluated expression in the evaluation tree (where 0 yields the currently evaluated
     *        expression)
     * @param indexOfChild the index of the child whose value is requested
     * @return the evaluation result of that child
     */
    public ValueT getValueOfExpression(TransitionResultT currentState, LocalStateT localState, int levelsAbove,
            int indexOfChild) {
        EvaluationContext<ValueT> ec = localState.getExecutionStack().getLast();
        return ec.getExpressionValue(levelsAbove, indexOfChild);
    }

    /**
     * Returns whetehr or not the given expression cares about the (abstracted) evaluation result of its
     * child with the given index.
     * 
     * This information is used to discard evaluation results that have no effect on future evaluations
     * and would therefore unnecessarily enlarge the state space.
     *
     * @param expression an expression
     * @param indexOfChild the index of one of its children
     * @return whether or not the expression cares about the evaluation result of that child
     */
    public boolean caresAboutValue(Expression expression, int indexOfChild) {
        if (expression == null) {
            return false;
        }
        if (expression instanceof WhileLoopExpression ex) {
            return indexOfChild == 0;
        }
        if (expression instanceof DoWhileLoopExpression ex) {
            return indexOfChild == 0;
        }
        if (expression instanceof ForLoopExpression ex) {
            return indexOfChild == 1;
        }
        if (expression instanceof IfElseExpression ex) {
            return indexOfChild == 0 || ex instanceof QuestionmarkExpression;
        }
        if (expression instanceof CaseExpression ex) {
            return indexOfChild == 0 && !ex.isDefaultCase();
        }

        return true;
    }

    /**
     * Attempts to convert the given abstracted value to one of type Boolean.
     * 
     * @param value an abstracted value
     * @return the given value as one of type Boolean, of possible
     * @throws ClassCastException if the conversion is not possible
     */
    @SuppressWarnings("unchecked")
    public <B extends AbstractedValue<B, B, Boolean>> B getAsBoolean(ValueT value) throws ClassCastException {
        if (!value.isDetermined()) {
            return (B) value;
        }

        Object val = value.get();
        if (val == null) {
            return (B) value;
        } else if (val instanceof Boolean) {
            return (B) value;
        } else if (val instanceof String s) {
            if (s.equals("true")) {
                return (B) getDeterminedValue(true);
            } else if (s.equals("false")) {
                return (B) getDeterminedValue(false);
            } else {
                throw new ClassCastException("String \"" + s + "\" cannot be converted to Boolean");
            }
        }

        throw new ClassCastException(val.getClass().getName() + " cannot be converted to Boolean");
    }

    /**
     * Attempts to convert the given value to one of type Boolean.
     * 
     * @param value a value
     * @return the given value as one of type Boolean, if possible
     * @throws ClassCastException if the conversion is not possible
     */
    public Boolean getAsBoolean(Object value) throws ClassCastException {
        return getAsBoolean(getDeterminedValue(value)).get();
    }

    /**
     * Attempts to convert the given abstracted value to one of type Integer.
     * 
     * @param value an abstracted value
     * @return the given value as one of type Integer, if possible
     * @throws ClassCastException if the conversion is not possible
     */
    @SuppressWarnings("unchecked")
    public AbstractedValue<?, ?, Integer> getAsInteger(ValueT value) throws ClassCastException {
        if (!value.isDetermined()) {
            return (AbstractedValue<?, ?, Integer>) value;
        }

        Object val = value.get();
        if (val == null) {
            return (AbstractedValue<?, ?, Integer>) value;
        } else if (val instanceof Integer) {
            return (AbstractedValue<?, ?, Integer>) value;
        } else if (val instanceof String s) {
            try {
                return (AbstractedValue<?, ?, Integer>) getDeterminedValue(Integer.parseInt(s));
            } catch (NumberFormatException e) {
                throw new ClassCastException("String \"" + s + "\" cannot be converted to Integer");
            }
        }

        throw new ClassCastException(val.getClass().getName() + " cannot be converted to Integer");
    }

    /**
     * Attempts to convert the given value to one of type Integer.
     * 
     * @param value a value
     * @return the given value as one of type Integer, if possible
     * @throws ClassCastException if the conversion is not possible
     */
    public Integer getAsInteger(Object value) throws ClassCastException {
        return getAsInteger(getDeterminedValue(value)).get();
    }

    /**
     * Attempts to convert the given abstracted value to one of type Double.
     * 
     * @param value an abstracted value
     * @return the given value as one of type Double, of possible
     * @throws ClassCastException if the conversion is not possible
     */
    @SuppressWarnings("unchecked")
    public AbstractedValue<?, ?, Double> getAsDouble(ValueT value) throws ClassCastException {
        if (!value.isDetermined()) {
            return (AbstractedValue<?, ?, Double>) value;
        }

        Object val = value.get();
        if (val == null) {
            return (AbstractedValue<?, ?, Double>) value;
        } else if (val instanceof Double) {
            return (AbstractedValue<?, ?, Double>) value;
        } else if (val instanceof String s) {
            try {
                return (AbstractedValue<?, ?, Double>) getDeterminedValue(Double.parseDouble(s));
            } catch (NumberFormatException e) {
                throw new ClassCastException("String \"" + s + "\" cannot be converted to Double");
            }
        }

        throw new ClassCastException(val.getClass().getName() + " cannot be converted to Double");
    }

    /**
     * Attempts to convert the given value to one of type Double.
     * 
     * @param value a value
     * @return the given value as one of type Double, if possible
     * @throws ClassCastException if the conversion is not possible
     */
    public Double getAsDouble(Object value) throws ClassCastException {
        return getAsDouble(getDeterminedValue(value)).get();
    }

    /**
     * Checks whether or not two abstracted values are guaranteed to be equal, unequal or whether no
     * definitive answer can be given.
     *
     * @param v1 an abstracted value
     * @param v2 another abstracted value
     * @return the equality between these two values as an abstracted value
     */
    @SuppressWarnings("unchecked")
    public <B extends AbstractedValue<B, B, Boolean>> B checkEquality(ValueT v1, ValueT v2) {
        if (!v1.isDetermined() || !v2.isDetermined()) {
            return (B) getNonDeterminedValue();
        }

        if (v1.get() == null || v2.get() == null) {
            return (B) getDeterminedValue(v1.get() == null && v2.get() == null);
        }

        if (v1.get().getClass() == v2.get().getClass()) {
            return (B) getDeterminedValue(v1.equals(v2));
        }

        if (v1.get() instanceof String || v2.get() instanceof String) {
            try {
                int i = Integer.parseInt(String.valueOf(v1.get()));
                int j = Integer.parseInt(String.valueOf(v2.get()));
                return (B) getDeterminedValue(i == j);
            } catch (NumberFormatException e) {
                // ignore
            }
            try {
                double i = Double.parseDouble(String.valueOf(v1.get()));
                double j = Double.parseDouble(String.valueOf(v2.get()));
                return (B) getDeterminedValue(i == j);
            } catch (NumberFormatException e) {
                // ignore
            }
        }

        return (B) getNonDeterminedValue();
    }

    /**
     * Returns the port instance corresponding to a port variable.
     *
     * @param port a port variable
     * @return the port instance corresponding to that variable
     */
    public WrappedSCPortInstance getPortInstance(SCPort port) {
        WrappedSCPortInstance result = null;
        for (SCConnectionInterface con : getSCSystem().getPortSocketInstances()) {
            SCPortInstance instance = (SCPortInstance) con;
            if (port != instance.getPortSocket()) {
                continue;
            }
            if (result != null) {
                throw new InsufficientPrecisionException();
            }
            result = wrap(instance);
        }
        return result;
    }

    /**
     * Returns the channel instance corresponding to a port instance.
     * 
     * @param portInstance a port instance
     * @return the channel instance corresponding to that port instance
     */
    public WrappedSCClassInstance getChannel(WrappedSCPortInstance portInstance) {
        return wrap(portInstance.getChannels().get(0));
    }

    /**
     * Returns the channel instance corresponding to a port variable.
     *
     * @param port a port variable
     * @return the channel instance corresponding to that variable
     */
    public WrappedSCClassInstance getChannel(SCPort port) {
        return getChannel(getPortInstance(port));
    }

    public <B extends AbstractedValue<B, B, Boolean>> ExecutionConditions<B> getExecutionConditions(
            LocalStateT localState) {
        ExecutionConditions<B> result = localState.getStateInformation(executionConditionsKey());
        if (result == null) {
            result = new ExecutionConditions<>();
            localState.setStateInformation(executionConditionsKey(), result);
        }
        return result;
    }

    /**
     * Called by {@link #makeSmallStep(TransitionResult)} to allow subclasses to intervene in the
     * handling of certain expressions.
     * 
     * Subclasses can either return a {@link SmallStepResult} to overwrite the evaluation of
     * {@link #makeSmallStep(TransitionResult)} or null for {@link #makeSmallStep(TransitionResult)} to
     * continue normally.
     * <p>
     * Note that if subclasses intervene, they still have to call
     * {@link #enterChildExpression(LocalState, int, int)} or
     * {@link #returnToParent(LocalState, AbstractedValue)} respectively or handle the evaluation
     * location and expression values themselves.
     * 
     * @param currentState the current state of the evaluation
     * @param expression the next expression to be evaluated
     * @param comingFrom from where the expression is reached (-1 if from its parent, or the index of
     *        the child which has last been evaluated)
     * @return the result overwriting the usual logic of {@link #makeSmallStep(TransitionResult)}, or
     *         null if no special treatment is given
     */
    public SmallStepResult<GlobalStateT, ProcessStateT, InfoT, TransitionResultT> handleSpecialExpression(
            TransitionResultT currentState, LocalStateT localState, Expression expression,
            int comingFrom) {
        return null;
    }

    /**
     * Called by {@link #makeSmallStep(TransitionResult)} to handle any kind of expression with constant
     * value.
     * 
     * This implementations simply stores the value in the expression values and returns to the parent.
     *
     * @param <X> the type of constant expression
     * @param currentState the current state of the evaluation
     * @param expression the expression to be evaluated
     * @param comingFrom from where the expression is reached (should always be -1 for constant
     *        expressions as they don't have children)
     * @param valueGetter a function for retrieving the constant value of the expression
     * @return the possible transitions and whether or not the end of an atomic block has been reached
     */
    public <X extends Expression> SmallStepResult<GlobalStateT, ProcessStateT, InfoT, TransitionResultT> handleConstantExpression(
            TransitionResultT currentState, LocalStateT localState,
            X expression, int comingFrom, Function<X, Object> valueGetter) {
        assert comingFrom == -1;

        returnToParent(expression.getParent(), localState, getDeterminedValue(valueGetter.apply(expression)));
        return createSmallStepResult(expression, comingFrom, currentState, localState, false, false);
    }

    /**
     * Called by {@link #makeSmallStep(TransitionResult)} to handle expressions that are ignored by this
     * implementation.
     * 
     * @param currentState the current state of the evaluation
     * @param expression the expression to be evaluated
     * @param comingFrom from where the expression is reached
     * @return the possible transitions and whether or not the end of an atomic block has been reached
     */
    public SmallStepResult<GlobalStateT, ProcessStateT, InfoT, TransitionResultT> handleIgnoredExpression(
            TransitionResultT currentState, LocalStateT localState, Expression expression,
            int comingFrom) {
        returnToParent(expression.getParent(), localState, getNonDeterminedValue());
        return createSmallStepResult(expression, comingFrom, currentState, localState, false, false);
    }

    /**
     * Called by {@link #makeSmallStep(TransitionResult)} to handle expressions which are evaluated
     * bottom up, i.e. first their children are evaluated and then their resulting values are aggregated
     * in some way by calling {@link #aggregateExpressionValue(TransitionResult, Expression)}.
     * 
     * @param currentState the current state of the evaluation
     * @param expression the expression to be evaluated
     * @param comingFrom from where the expression is reached (-1 if from its parent, or the index of
     *        the child which has last been evaluated)
     * @return the possible transitions and whether or not the end of an atomic block has been reached
     */
    public SmallStepResult<GlobalStateT, ProcessStateT, InfoT, TransitionResultT> handleBottomUpExpression(
            TransitionResultT currentState, LocalStateT localState, Expression expression,
            int comingFrom) {
        int numOfChildren = expression.getNumOfChildren();

        // coming from the last child
        if (comingFrom == numOfChildren - 1) {
            // handle event lists (e1 & e2 or e1 | e2) separately
            if (expression instanceof BinaryExpression be && (be.getOp().equals("&") || be.getOp().equals("|"))) {
                ValueT left = getValueOfChild(currentState, localState, 0);
                ValueT right = getValueOfChild(currentState, localState, 1);
                if (left.isDetermined() && right.isDetermined()) {
                    Object leftValue = left.get();
                    Object rightValue = right.get();
                    if ((leftValue instanceof Event || leftValue instanceof EventBlocker)
                            && rightValue instanceof Event re) {
                        EventBlocker result;
                        if (leftValue instanceof Event le) {
                            result = new EventBlocker(Set.of(le, re), be.getOp().equals("|"), null);
                        } else {
                            EventBlocker current = (EventBlocker) leftValue;
                            Set<Event> newEvents = new LinkedHashSet<>(current.getEvents());
                            newEvents.add(re);
                            result = current.replaceEvents(newEvents);

                            assert current.isChoice() == be.getOp().equals("|");
                        }
                        returnToParent(expression.getParent(), localState, getDeterminedValue(result));
                        return createSmallStepResult(expression, comingFrom, currentState, localState, false, false);
                    }
                }
            }

            ValueT expressionValue = aggregateExpressionValue(currentState, localState, expression);
            returnToParent(expression.getParent(), localState, expressionValue);
            return createSmallStepResult(expression, comingFrom, currentState, localState, false, false);
        }

        // stop execution early (e.g. in case of short circuit evaluation)?
        ValueT expressionValue = stopEvaluationEarly(currentState, localState, expression, comingFrom);
        if (expressionValue != null) {
            returnToParent(expression.getParent(), localState, expressionValue);
            return createSmallStepResult(expression, comingFrom, currentState, localState, false, false);
        }

        // go into the next child
        enterChildExpression(localState, comingFrom + 1);
        return createSmallStepResult(expression, comingFrom, currentState, localState, false, false);
    }

    /**
     * Called after all children of an expression have been evaluated to aggregate their results
     * according to the state abstraction chosen for the analysis.
     * 
     * @param currentState the current state of the evaluation
     * @param localState the local portion of the state
     * @param expression the expression to be evaluated
     * @return an abstraction of the value of the expression (may not be null, but may be non-determined
     *         or an abstraction of null)
     */
    public abstract ValueT aggregateExpressionValue(TransitionResultT currentState, LocalStateT localState,
            Expression expression);

    /**
     * Called by {@link #handleBottomUpExpression(TransitionResult, Expression, int)} every time the
     * evaluation reaches an expression before its last child has been evaluated to determine whether or
     * not the evaluation should be stopped early, and if so, with which result.
     * 
     * A return value of null indicates that the evaluation shall continue.
     * 
     * @param currentState the current state of the evaluation
     * @param expression the expression currently evaluated
     * @param comingFrom the index of the last child that was evaluated
     * @return an abstraction of the value of the expression if the evaluation shall stop early, or null
     *         otherwise
     */
    public ValueT stopEvaluationEarly(TransitionResultT currentState, LocalStateT localState, Expression expression,
            int comingFrom) {
        return null;
    }

    public SmallStepResult<GlobalStateT, ProcessStateT, InfoT, TransitionResultT> handleBracketExpression(
            TransitionResultT currentState, LocalStateT localState, BracketExpression expression, int comingFrom) {
        // entering from parent
        if (comingFrom == -1) {
            // go into condition
            enterChildExpression(localState, 0);
            return createSmallStepResult(expression, comingFrom, currentState, localState, false, false);
        }

        returnToParent(expression, localState, getValueOfChild(currentState, localState, 0));
        return createSmallStepResult(expression, comingFrom, currentState, localState, false, false);
    }

    public <B extends AbstractedValue<B, B, Boolean>> SmallStepResult<GlobalStateT, ProcessStateT, InfoT, TransitionResultT> handleIfElseExpression(
            TransitionResultT currentState, LocalStateT localState,
            IfElseExpression expression, int comingFrom) {
        // entering from parent
        if (comingFrom == -1) {
            // go into condition
            enterChildExpression(localState, 0);
            return createSmallStepResult(expression, comingFrom, currentState, localState, false, false);
        }

        // returning from condition
        if (comingFrom == 0) {
            B conditionResult = getAsBoolean(getValueOfChild(currentState, localState, 0));

            if (!conditionResult.isDetermined()) {
                // go into both then and else block
                TransitionResultT copyForThenCase = currentState.clone();
                LocalStateT localStateForThenCase = getLocalState(copyForThenCase);
                ExecutionConditions<B> executionConditionsForThenCase = getExecutionConditions(localStateForThenCase);
                executionConditionsForThenCase.add(expression, conditionResult);
                enterChildExpression(localStateForThenCase, 1);

                if (expression.getElseBlock().isEmpty()) {
                    returnToParent(expression.getParent(), localState);
                } else {
                    ExecutionConditions<B> executionConditionsForElseCase = getExecutionConditions(localState);
                    executionConditionsForElseCase.add(expression,
                            conditionResult.getAbstractedLogic().not(conditionResult));
                    enterChildExpression(localState, expression.getThenBlock().size() + 1);
                }
                return createSmallStepResult(expression, comingFrom, List.of(copyForThenCase, currentState), false,
                        false);
            } else if (conditionResult.get()) {
                // go into then block

                // add a condition result even though it is "true" because we always remove a condition upon leaving
                // the statement
                ExecutionConditions<B> executionConditions = getExecutionConditions(localState);
                executionConditions.add(expression, conditionResult);
                enterChildExpression(localState, 1);
                return createSmallStepResult(expression, comingFrom, currentState, localState, false, false);
            } else {
                if (expression.getElseBlock().isEmpty()) {
                    returnToParent(expression.getParent(), localState);
                } else {
                    // go into else block

                    // add a condition result even though it is "true" because we always remove a condition upon leaving
                    // the statement
                    ExecutionConditions<B> executionConditions = getExecutionConditions(localState);
                    executionConditions.add(expression, conditionResult.getAbstractedLogic().not(conditionResult));
                    enterChildExpression(localState, expression.getThenBlock().size() + 1);
                }
                return createSmallStepResult(expression, comingFrom, currentState, localState, false, false);
            }
        }

        int lengthOfThenBlock = expression.getThenBlock().size();

        // returning from then block
        if (comingFrom <= lengthOfThenBlock) {
            // returning from end of then block
            if (comingFrom >= lengthOfThenBlock) {
                // leave if-else-block
                ExecutionConditions<B> executionConditions = getExecutionConditions(localState);
                executionConditions.removeUntil(expression);
                if (expression instanceof QuestionmarkExpression) {
                    assert lengthOfThenBlock == 1;
                    returnToParent(expression.getParent(), localState, getValueOfChild(currentState, localState, 1));
                } else {
                    returnToParent(expression.getParent(), localState);
                }
            }
            // returning from within then block
            else {
                // go to next expression in then block
                enterChildExpression(localState, comingFrom + 1);
            }
            return createSmallStepResult(expression, comingFrom, currentState, localState, false, false);
        }

        // returning from else block
        int lengthOfElseBlock = expression.getElseBlock().size();
        // returning from end of else block
        if (comingFrom >= lengthOfThenBlock + lengthOfElseBlock) {
            // leave if-else-block
            ExecutionConditions<B> executionConditions = getExecutionConditions(localState);
            executionConditions.removeUntil(expression);
            if (expression instanceof QuestionmarkExpression) {
                assert lengthOfThenBlock == 1 && lengthOfElseBlock == 1;
                returnToParent(expression.getParent(), localState, getValueOfChild(currentState, localState, 2));
            } else {
                returnToParent(expression.getParent(), localState);
            }
            // can't repeat, but might improve performance to check whether taking one path or the other makes
            // any difference
            return createSmallStepResult(expression, comingFrom, currentState, localState, false, true);
        }
        // returning form within else block
        else {
            // go to next expression in then block
            enterChildExpression(localState, comingFrom + 1);
            return createSmallStepResult(expression, comingFrom, currentState, localState, false, false);
        }
    }

    public <B extends AbstractedValue<B, B, Boolean>> SmallStepResult<GlobalStateT, ProcessStateT, InfoT, TransitionResultT> handleWhileLoopExpression(
            TransitionResultT currentState, LocalStateT localState,
            LoopExpression expression, int comingFrom) {
        // entering from parent
        if (comingFrom == -1) {
            // add true condition so that removing one when breaking is always correct
            getExecutionConditions(localState).add(expression, getAsBoolean(getDeterminedValue(true)));

            if (expression instanceof WhileLoopExpression) {
                // go into condition
                enterChildExpression(localState, 0);
                return createSmallStepResult(expression, comingFrom, currentState, localState, false, true); // condition
                // check
                // might
                // repeat
            } else if (expression instanceof DoWhileLoopExpression) {
                // go into body
                enterChildExpression(localState, 1);
                return createSmallStepResult(expression, comingFrom, currentState, localState, false, false);
            }
        }

        // returning from condition
        if (comingFrom == 0) {
            B conditionResult = getAsBoolean(getValueOfChild(currentState, localState, 0));

            if (!conditionResult.isDetermined()) {
                // go into body and leave loop
                TransitionResultT copyForLoopCase = currentState.clone();
                LocalStateT localStateForLoopCase = getLocalState(copyForLoopCase);
                ExecutionConditions<B> executionConditionsForLoopCase = getExecutionConditions(localStateForLoopCase);
                executionConditionsForLoopCase.add(expression, conditionResult);
                enterChildExpression(getLocalState(copyForLoopCase), 1);

                ExecutionConditions<B> executionConditionsForBreakCase = getExecutionConditions(localState);
                executionConditionsForBreakCase.removeUntil(expression);
                // TODO: add negative of condition instead?
                returnToParent(expression.getParent(), localState);
                return createSmallStepResult(expression, comingFrom, List.of(copyForLoopCase, currentState), false,
                        false);
            } else if (conditionResult.get()) {
                // go into body

                // don't add execution condition because the condition result is determined, meaning the current
                // condition is still sufficient
                enterChildExpression(localState, 1);
                return createSmallStepResult(expression, comingFrom, currentState, localState, false, false);
            } else {
                // leave loop
                ExecutionConditions<B> executionConditions = getExecutionConditions(localState);
                executionConditions.removeUntil(expression);
                returnToParent(expression.getParent(), localState);
                return createSmallStepResult(expression, comingFrom, currentState, localState, false, false);
            }
        }

        // returning from loop body
        int lengthOfBody = expression.getLoopBody().size();

        // returning from end of loop body
        if (comingFrom >= lengthOfBody) {
            // go into condition
            enterChildExpression(localState, 0);
            // condition check might repeat
            return createSmallStepResult(expression, comingFrom, currentState, localState, false, true);
        }
        // returning form within loop body
        else {
            // goto next expression of body
            enterChildExpression(localState, comingFrom + 1);
            return createSmallStepResult(expression, comingFrom, currentState, localState, false, false);
        }
    }

    public <B extends AbstractedValue<B, B, Boolean>> SmallStepResult<GlobalStateT, ProcessStateT, InfoT, TransitionResultT> handleForLoopExpression(
            TransitionResultT currentState, LocalStateT localState,
            ForLoopExpression expression, int comingFrom) {
        // entering from parent
        if (comingFrom == -1) {
            // add true condition so that removing one when breaking is always correct
            getExecutionConditions(localState).add(expression, getAsBoolean(getDeterminedValue(true)));

            // go into initializer
            enterChildExpression(localState, 0);
            return createSmallStepResult(expression, comingFrom, currentState, localState, false, false);
        }

        // returning from initializer
        if (comingFrom == 0) {
            // go into condition
            enterChildExpression(localState, 1);
            // condition check might repeat
            return createSmallStepResult(expression, comingFrom, currentState, localState, false, true);
        }

        // returning from condition
        if (comingFrom == 1) {
            B conditionResult = getAsBoolean(getValueOfChild(currentState, localState, 1));

            if (!conditionResult.isDetermined()) {
                // go into body and leave loop
                TransitionResultT copyForLoopCase = currentState.clone();
                LocalStateT localStateForLoopCase = getLocalState(copyForLoopCase);
                ExecutionConditions<B> executionConditionsForLoopCase = getExecutionConditions(localStateForLoopCase);
                executionConditionsForLoopCase.add(expression, conditionResult);
                enterChildExpression(getLocalState(copyForLoopCase), 2);

                ExecutionConditions<B> executionConditionsForBreakCase = getExecutionConditions(localState);
                executionConditionsForBreakCase.removeUntil(expression);
                returnToParent(expression.getParent(), localState);
                return createSmallStepResult(expression, comingFrom, List.of(copyForLoopCase, currentState), false,
                        false);
            } else if (conditionResult.get()) {
                // go into body

                // don't add execution condition because the condition result is determined, meaning the current
                // condition is still sufficient
                enterChildExpression(localState, 2);
                return createSmallStepResult(expression, comingFrom, currentState, localState, false, false);
            } else {
                // leave loop

                ExecutionConditions<B> executionConditions = getExecutionConditions(localState);
                executionConditions.removeUntil(expression);
                returnToParent(expression.getParent(), localState);
                return createSmallStepResult(expression, comingFrom, currentState, localState, false, false);
            }
        }

        int lengthOfBody = expression.getLoopBody().size();

        // returning from iterator
        if (comingFrom == lengthOfBody + 2) {
            // go into condition
            enterChildExpression(localState, 1);
            // condition check might repeat
            return createSmallStepResult(expression, comingFrom, currentState, localState, false, true);
        }

        // returning from loop body

        // returning from end of loop body
        if (comingFrom == lengthOfBody + 1) {
            // go into iterator
            enterChildExpression(localState, lengthOfBody + 2);
        }
        // returning form within loop body
        else {
            // goto next expression of body
            enterChildExpression(localState, comingFrom + 1);
        }
        return createSmallStepResult(expression, comingFrom, currentState, localState, false, false);
    }

    public <B extends AbstractedValue<B, B, Boolean>> SmallStepResult<GlobalStateT, ProcessStateT, InfoT, TransitionResultT> handleSwitchExpression(
            TransitionResultT currentState, LocalStateT localState,
            SwitchExpression expression, int comingFrom) {
        // coming from parent
        if (comingFrom == -1) {
            // add true condition so that removing one when leaving is always correct
            getExecutionConditions(localState).add(expression, getAsBoolean(getDeterminedValue(true)));

            // go into expression to switch on
            enterChildExpression(localState, 0);
            return createSmallStepResult(expression, comingFrom, currentState, localState, false, false);
        }

        // returning from expression to switch on
        if (comingFrom == 0) {
            // go into first case
            enterChildExpression(localState, 1);
            return createSmallStepResult(expression, comingFrom, currentState, localState, false, false);
        }

        // returning from the last case
        if (comingFrom == expression.getNumOfChildren() - 1) {
            ExecutionConditions<B> executionConditions = getExecutionConditions(localState);
            executionConditions.removeUntil(expression);

            returnToParent(expression.getParent(), localState);
            // can't repeat, but might improve performance to check whether taking one path or the other makes
            // any difference
            return createSmallStepResult(expression, comingFrom, currentState, localState, false, true);
        }

        // returning from some case other than the last: enter the next one
        enterChildExpression(localState, comingFrom + 1);
        // can't repeat, but might improve performance to check whether taking one path or the other makes
        // any difference
        return createSmallStepResult(expression, comingFrom, currentState, localState, false, true);
    }

    public <B extends AbstractedValue<B, B, Boolean>> SmallStepResult<GlobalStateT, ProcessStateT, InfoT, TransitionResultT> handleCaseExpression(
            TransitionResultT currentState, LocalStateT localState, CaseExpression expression, int comingFrom) {
        // TODO: use expression.getParent() instead of expression when adding execution conditions=

        // coming from parent
        if (comingFrom == -1) {
            // checking case or falling through?
            EvaluationContext<ValueT> el = localState.getTopOfStack();
            int myIndex = el.getExpressionIndices().get(el.getExpressionIndices().size() - 1);
            boolean fallingThrough;
            if (myIndex <= 1) {
                fallingThrough = false;
            } else {
                ValueT fallingThroughWrapper = getValueOfExpression(currentState, localState, 1, myIndex - 1);
                fallingThrough = getAsBoolean(fallingThroughWrapper).get();
            }

            if (fallingThrough) {
                // go into body directly, if body exists

                // don't add to execution condition because the one added when entering the matching case is still
                // sufficient.
                if (expression.getBody().isEmpty()) {
                    returnToParent(expression.getParent(), localState, getDeterminedValue(true));
                } else {
                    enterChildExpression(localState, expression.isDefaultCase() ? 0 : 1);
                }
            } else {
                // does case apply?
                if (expression.isDefaultCase()) {
                    if (expression.getBody().isEmpty()) {
                        returnToParent(expression.getParent(), localState, getDeterminedValue(true));
                    } else {
                        // yes, enter body
                        enterChildExpression(localState, 0);
                    }
                } else {
                    // evaluate case value
                    enterChildExpression(localState, 0);
                }
            }
            return createSmallStepResult(expression, comingFrom, currentState, localState, false, false);
        }

        // returning from case value evaluation
        if (comingFrom == 0 && !expression.isDefaultCase()) {
            ValueT switchValue = getValueOfExpression(currentState, localState, 1, 0);
            ValueT caseValue = getValueOfChild(currentState, localState, 0);
            B equality = checkEquality(switchValue, caseValue);

            if (!equality.isDetermined()) {
                // go into case and step over case
                TransitionResultT copyForIntoCase = currentState.clone();
                LocalStateT localStateForIntoCase = getLocalState(copyForIntoCase);
                ExecutionConditions<B> executionConditionsForIntoCase = getExecutionConditions(localStateForIntoCase);
                executionConditionsForIntoCase.add(expression, equality);

                if (expression.getBody().isEmpty()) {
                    returnToParent(expression.getParent(), getLocalState(copyForIntoCase), getDeterminedValue(true));
                } else {
                    enterChildExpression(getLocalState(copyForIntoCase), 1);
                }

                ExecutionConditions<B> executionConditionsForSkipCase = getExecutionConditions(localState);
                executionConditionsForSkipCase.add(expression, equality.getAbstractedLogic().not(equality));
                returnToParent(expression.getParent(), localState, getDeterminedValue(false));
                return createSmallStepResult(expression, comingFrom, List.of(copyForIntoCase, currentState), false,
                        false);
            } else if (equality.get()) {
                // go into case
                if (expression.getBody().isEmpty()) {
                    returnToParent(expression.getParent(), localState, getDeterminedValue(true));
                } else {
                    enterChildExpression(localState, 1);
                }
                return createSmallStepResult(expression, comingFrom, currentState, localState, false, false);
            } else {
                // step over case
                returnToParent(expression.getParent(), localState, getDeterminedValue(false));
                return createSmallStepResult(expression, comingFrom, currentState, localState, false, false);
            }
        }

        // returning from last body expression
        if (comingFrom == expression.getNumOfChildren() - 1) {
            returnToParent(expression.getParent(), localState, getDeterminedValue(true));
        }
        // returning from other body expression
        else {
            // go to next
            enterChildExpression(localState, comingFrom + 1);
        }
        return createSmallStepResult(expression, comingFrom, currentState, localState, false, false);
    }

    public SmallStepResult<GlobalStateT, ProcessStateT, InfoT, TransitionResultT> handleBreakExpression(
            TransitionResultT currentState, LocalStateT localState,
            BreakExpression expression, int comingFrom) {
        assert comingFrom == -1;

        String label = expression.getLabel();
        EvaluationContext<ValueT> ec = localState.getTopOfStack();
        List<Integer> indices = ec.getExpressionIndices();

        Expression targetted;
        int levelsAbove = 1;
        for (;; levelsAbove++) {
            targetted = ec.getNextExpression(levelsAbove);
            if (isTargetted(targetted, label, true)) {
                break;
            }
        }

        getExecutionConditions(localState).removeUntil(targetted);

        int removedIndex = 0;
        List<List<ValueT>> expressionValues = ec.getExpressionValues();
        for (int i = 0; i <= levelsAbove; i++) {
            removedIndex = indices.remove(indices.size() - 1);
            expressionValues.remove(expressionValues.size() - 1);
        }
        ec.setComingFrom(removedIndex);

        List<ValueT> parentValues = ec.getExpressionValues().getLast();
        CollectionUtil.addOrSet(parentValues, ec.getComingFrom(), null);
        return createSmallStepResult(expression, comingFrom, currentState, localState, false, false);
    }

    public SmallStepResult<GlobalStateT, ProcessStateT, InfoT, TransitionResultT> handleContinueExpression(
            TransitionResultT currentState, LocalStateT localState, ContinueExpression expression, int comingFrom) {
        assert comingFrom == -1;

        String label = expression.getLabel();
        EvaluationContext<ValueT> ec = localState.getTopOfStack();
        List<Integer> indices = ec.getExpressionIndices();

        Expression childOfTargetted;
        Expression targetted = null;
        int levelsAbove = 1;
        for (;; levelsAbove++) {
            childOfTargetted = targetted;
            targetted = ec.getNextExpression(levelsAbove);
            if (isTargetted(targetted, label, true)) {
                break;
            }
        }

        getExecutionConditions(localState).removeUntil(childOfTargetted);

        List<List<ValueT>> expressionValues = ec.getExpressionValues();
        for (int i = 0; i < levelsAbove; i++) {
            indices.remove(indices.size() - 1);
            expressionValues.remove(expressionValues.size() - 1);
        }
        if (targetted instanceof ForLoopExpression fe) {
            // continue with iterator
            ec.setComingFrom(fe.getLoopBody().size() + 1);
        } else {
            ec.setComingFrom(targetted.getNumOfChildren() - 1);
        }

        return createSmallStepResult(expression, comingFrom, currentState, localState, false, false);
    }

    /**
     * Returns whether or not an expression is the target of a break or continue statement.
     * 
     * Candidates must be checked from the inside out, as this method may falsely return true for
     * candidates that enclose the actually targetted expression.
     *
     * @param candidate the expression that might be targetted
     * @param label the label of the break or continue statement
     * @param breakStatement whether the targetting statement is a break statement
     * @return whether the candidate is targetted
     */
    public boolean isTargetted(Expression candidate, String label, boolean breakStatement) {
        if (!(candidate instanceof LoopExpression) && !(breakStatement && candidate instanceof SwitchExpression)) {
            return false;
        }
        return label.isEmpty() || label.equals(candidate.getLabel());
    }

    public SmallStepResult<GlobalStateT, ProcessStateT, InfoT, TransitionResultT> handleEventNotificationExpression(
            TransitionResultT currentState, LocalStateT localState,
            EventNotificationExpression expression,
            int comingFrom) {
        // entering from parent
        if (comingFrom == -1) {
            enterChildExpression(localState, 0);
            return createSmallStepResult(expression, comingFrom, currentState, localState, false, false);
        }

        int numOfParameters = expression.getParameters().size();

        // returning from the evaluation of a parameter (not the last one)
        if (comingFrom < numOfParameters) {
            enterChildExpression(localState, comingFrom + 1);
            return createSmallStepResult(expression, comingFrom, currentState, localState, false, false);
        }

        // returning from the last parameter

        ValueT eventObject = getValueOfChild(currentState, localState, 0);
        if (!eventObject.isDetermined()) {
            throw new ExpressionCrawler.InsufficientPrecisionException(expression.toString());
        }

        Event event = (Event) eventObject.get();

        TimedBlocker delay;

        if (numOfParameters == 0) {
            // immediate notification
            delay = null;
        } else {
            ValueT firstParam = getValueOfChild(currentState, localState, 1);
            if (!firstParam.isDetermined()) {
                throw new ExpressionCrawler.InsufficientPrecisionException();
            }

            if (numOfParameters == 1) {
                if (firstParam.get() instanceof TimedBlocker tb) {
                    delay = tb;
                } else if (firstParam.get() instanceof SCTIMEUNIT tu) {
                    // delta delayed notification
                    if (tu != SCTIMEUNIT.SC_ZERO_TIME) {
                        throw new UnsupportedOperationException(
                                "notification with just one parameter which is an SCTIMEUNIT must be delta");
                    }
                    delay = DeltaTimeBlocker.INSTANCE;
                } else {
                    throw new ClassCastException();
                }
            } else {
                ValueT secondParam = getValueOfChild(currentState, localState, 2);
                if (!secondParam.isDetermined()) {
                    throw new ExpressionCrawler.InsufficientPrecisionException();
                }

                // delta delayed or timed notification
                assert numOfParameters == 2;

                int amount = getAsInteger(firstParam).get();
                SCTIMEUNIT unit = (SCTIMEUNIT) secondParam.get();
                delay = amount == 0 ? DeltaTimeBlocker.INSTANCE : new RealTimedBlocker(amount, unit);
            }
        }

        List<TransitionResultT> transitions =
                CollectionUtil.asList(this.scheduler.notifyEvents(currentState, event, delay));
        for (TransitionResultT transition : transitions) {
            returnToParent(expression.getParent(), getLocalState(transition));
        }
        return createSmallStepResult(expression, comingFrom, transitions, false, false);
    }

    public SmallStepResult<GlobalStateT, ProcessStateT, InfoT, TransitionResultT> handleSCStopExpression(
            TransitionResultT currentState, LocalStateT localState,
            SCStopExpression expression, int comingFrom) {
        returnToParent(expression.getParent(), localState);
        Collection<TransitionResultT> transitions = getScheduler().stopSimulation(currentState);
        return createSmallStepResult(expression, comingFrom, new ArrayList<>(transitions), false, false);
    }

    public SmallStepResult<GlobalStateT, ProcessStateT, InfoT, TransitionResultT> handleFunctionCallExpression(
            TransitionResultT currentState, LocalStateT localState,
            FunctionCallExpression expression,
            int comingFrom) {
        int numOfParameters = expression.getParameters().size();

        // returning from the evaluation of a parameter (not the last one)
        if (comingFrom < numOfParameters - 1) {
            enterChildExpression(localState, comingFrom + 1);
            return createSmallStepResult(expression, comingFrom, currentState, localState, false, false);
        }

        // returning from function call
        if (comingFrom >= numOfParameters) {
            returnToParent(expression.getParent(), localState,
                    getValueOfChild(currentState, localState, numOfParameters));
            return createSmallStepResult(expression, comingFrom, currentState, localState, false, false);
        }

        SmallStepResult<GlobalStateT, ProcessStateT, InfoT, TransitionResultT> result =
                handleSpecialCaseFunctionCall(currentState, localState, expression, comingFrom);
        if (result != null) {
            return result;
        }

        // returning from the last parameter, call function

        // first, find new value of "this"
        ValueT thisValue;
        if (expression.getParent() instanceof AccessExpression ae) {
            thisValue = getValueOfExpression(currentState, localState, 1, 0);
            // TODO: differentiate based on operator ('.' or '->')?
        } else {
            thisValue = localState.getTopOfStack().getThisValue();
        }

        List<List<ValueT>> executionValues = new ArrayList<>();
        executionValues.add(new ArrayList<>());

        EvaluationContext<ValueT> newContext =
                new EvaluationContext<>(wrap(expression.getFunction()), new ArrayList<>(), -1,
                        executionValues,
                        thisValue);
        localState.getExecutionStack().add(newContext);
        getExecutionConditions(localState).addCall();

        functionCalled(expression, currentState, localState);

        return createSmallStepResult(expression, comingFrom, currentState, localState, false, false);
    }

    /**
     * Called when a function has been called (i.e. entered), right before the SmallStepResult is
     * created. Not called when entering special case functions (wait, notify, request_update).
     * 
     * Subclasses may override this method to adjust the resulting state, e.g. for setting parameter
     * values.
     *
     * @param expression the evaluated function call expression
     * @param currentState the resulting state
     * @param localState the local part of that state
     */
    protected void functionCalled(FunctionCallExpression expression, TransitionResultT currentState,
            LocalStateT localState) {

    }

    /**
     * Called when a function has been returned from (i.e. left), right before the SmallStepResult is
     * created. Not called when leaving special case functions (wait, notify, request_update) or the top
     * level function.
     * 
     * Subclasses may override this method to adjust the resulting state, e.g. for resetting parameter
     * values.
     *
     * @param expression the function call expression which lead to the function beeing entered
     * @param currentState the resulting state
     * @param localState the local part of that state
     */
    protected void functionReturned(FunctionCallExpression expression, TransitionResultT currentState,
            LocalStateT localState) {

    }

    /**
     * Handles function calls that require special treatment, such as wait statements or update
     * requests.
     * 
     * If no special treatment is necessary, null is returned.
     *
     * @param currentState the current state
     * @param localState the local part of the current state
     * @param expression the function call expression in question
     * @param comingFrom from where the evaluation is reaching the expression
     * @return the result of the special treatment, or null
     */
    public SmallStepResult<GlobalStateT, ProcessStateT, InfoT, TransitionResultT> handleSpecialCaseFunctionCall(
            TransitionResultT currentState, LocalStateT localState,
            FunctionCallExpression expression, int comingFrom) {
        if (expression.getFunction().getName().equals("wait")) {
            return handleWaitExpression(currentState, localState, expression, comingFrom);
        }

        if (expression.getFunction().getName().equals("request_update")) {
            return handleRequestUpdateExpression(currentState, localState, expression, comingFrom);
        }

        return null;
    }

    public SmallStepResult<GlobalStateT, ProcessStateT, InfoT, TransitionResultT> handleReturnExpression(
            TransitionResultT currentState, LocalStateT localState,
            ReturnExpression expression, int comingFrom) {
        // entering from parent
        if (comingFrom == -1 && expression.getReturnStatement() != null) {
            // evaluate return value
            enterChildExpression(localState, 0);
            return createSmallStepResult(expression, comingFrom, currentState, localState, false, false);
        }

        // returning after evaluation of return value
        return returnFromFunction(currentState, localState, expression, comingFrom,
                getValueOfChild(currentState, localState, 0));
    }

    public SmallStepResult<GlobalStateT, ProcessStateT, InfoT, TransitionResultT> handleFunctionBody(
            TransitionResultT currentState, LocalStateT localState, int comingFrom) {
        EvaluationContext<ValueT> evaluationLocation = localState.getTopOfStack();

        if (comingFrom == -1) {
            enterChildExpression(localState, 0);
            return createSmallStepResult(null, comingFrom, currentState, localState, false, false);
        }

        int lengthOfBody = evaluationLocation.getFunction().getBody().size();

        if (comingFrom < lengthOfBody - 1) {
            enterChildExpression(localState, comingFrom + 1);
            return createSmallStepResult(null, comingFrom, currentState, localState, false, false);
        }

        return returnFromFunction(currentState, localState, null, comingFrom);
    }

    public SmallStepResult<GlobalStateT, ProcessStateT, InfoT, TransitionResultT> handleWaitExpression(
            TransitionResultT currentState, LocalStateT localState,
            FunctionCallExpression expression, int comingFrom) {
        throw new UnsupportedOperationException("This expression crawler can't handle wait expressions.");
    }

    public SmallStepResult<GlobalStateT, ProcessStateT, InfoT, TransitionResultT> handleRequestUpdateExpression(
            TransitionResultT currentState, LocalStateT localState,
            FunctionCallExpression expression, int comingFrom) {
        throw new UnsupportedOperationException("This expression crawler can't handle request update expressions.");
    }

    public SmallStepResult<GlobalStateT, ProcessStateT, InfoT, TransitionResultT> handleOtherExpression(
            TransitionResultT currentState, LocalStateT localState, Expression expression,
            int comingFrom) {
        throw new UnsupportedOperationException(
                "Expression type " + expression.getClass().getName() + " not supported.");
    }

    /**
     * Updates the execution stack of the local state to reflect entering the current expression's child
     * with the given index.
     *
     * @param localState the local state
     * @param index the index of the entered child
     */
    public void enterChildExpression(LocalStateT localState, int index) {
        EvaluationContext<ValueT> top = localState.getTopOfStack();

        top.getExpressionIndices().add(index);
        top.setComingFrom(-1);
        top.getExpressionValues().add(new ArrayList<>());
    }

    /**
     * Updates the execution stack of the local state to reflect returning from the evaluation of a
     * child back to the parent.
     * 
     * No evaluation result of the child is specified.
     *
     * @param parent the parent expression to return to
     * @param localState the local state
     */
    public void returnToParent(Expression parent, LocalStateT localState) {
        returnToParent(parent, localState, null);
    }


    /**
     * Updates the execution stack of the local state to reflect returning from the evaluation of a
     * child back to the parent, with the given value as the evaluation result.
     * 
     * @param parent the parent expression to return to
     * @param localState the local state
     * @param result evaluation result of the child
     */
    public void returnToParent(Expression parent, LocalStateT localState, ValueT result) {
        EvaluationContext<ValueT> top = localState.getTopOfStack();

        int removedIndex = top.getExpressionIndices().remove(top.getExpressionIndices().size() - 1);
        top.setComingFrom(removedIndex);


        top.getExpressionValues().remove(top.getExpressionValues().size() - 1);
        if (!caresAboutValue(parent, removedIndex)) {
            return;
        }

        List<ValueT> parentValues = top.getExpressionValues().get(top.getExpressionValues().size() - 1);
        CollectionUtil.addOrSet(parentValues, top.getComingFrom(), result);
    }

    /**
     * Updates the execution stack of the local state to reflect returning from a function call.
     * 
     * No return value is specified.
     *
     * @param currentState the current state
     * @param localState the local part of that state
     */
    public SmallStepResult<GlobalStateT, ProcessStateT, InfoT, TransitionResultT> returnFromFunction(
            TransitionResultT currentState, LocalStateT localState, ReturnExpression expression,
            int comingFrom) {
        return returnFromFunction(currentState, localState, expression, comingFrom, null);
    }

    /**
     * Updates the execution stack of the local state to reflect returning from a function call with the
     * given return value.
     * 
     * @param currentState the current state
     * @param localState the local part of that state
     * @param result the return value
     */
    public SmallStepResult<GlobalStateT, ProcessStateT, InfoT, TransitionResultT> returnFromFunction(
            TransitionResultT currentState, LocalStateT localState, ReturnExpression expression,
            int comingFrom, ValueT result) {
        List<EvaluationContext<ValueT>> stack = localState.getExecutionStack();
        stack.remove(stack.size() - 1);

        if (stack.isEmpty()) {
            return handleEndOfCodeReached(currentState, localState, stack);
        }

        EvaluationContext<ValueT> top = stack.get(stack.size() - 1);
        top.setComingFrom(top.getComingFrom() + 1);

        List<ValueT> parentValues = top.getExpressionValues().get(top.getExpressionValues().size() - 1);
        CollectionUtil.addOrSet(parentValues, top.getComingFrom(), result);

        getExecutionConditions(localState).removeCall();
        functionReturned((FunctionCallExpression) top.getNextExpression(), currentState, localState);
        return createSmallStepResult(expression, comingFrom, currentState, localState, false, false);
    }

    /**
     * Handles reaching the end of the top-level function considered by this expression crawler.
     *
     * @param currentState the current state
     * @param localState the local part of that state
     * @param stack the current local execution stack
     * @return the possible transitions and whether or not the end of an atomic block has been reached
     */
    public abstract SmallStepResult<GlobalStateT, ProcessStateT, InfoT, TransitionResultT> handleEndOfCodeReached(
            TransitionResultT currentState, LocalStateT localState,
            List<EvaluationContext<ValueT>> stack);

}
