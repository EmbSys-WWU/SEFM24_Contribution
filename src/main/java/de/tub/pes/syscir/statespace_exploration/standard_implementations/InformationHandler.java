package de.tub.pes.syscir.statespace_exploration.standard_implementations;

import de.tub.pes.syscir.statespace_exploration.AbstractedValue;
import de.tub.pes.syscir.statespace_exploration.AnalyzedProcess;
import de.tub.pes.syscir.statespace_exploration.ConsideredState;
import de.tub.pes.syscir.statespace_exploration.EventBlocker;
import de.tub.pes.syscir.statespace_exploration.EventBlocker.Event;
import de.tub.pes.syscir.statespace_exploration.LocalState;
import de.tub.pes.syscir.statespace_exploration.ProcessState;
import de.tub.pes.syscir.statespace_exploration.StateSpaceExploration;
import de.tub.pes.syscir.statespace_exploration.TransitionResult;
import de.tub.pes.syscir.sc_model.expressions.Expression;
import java.util.Set;

/**
 * An interface for classes which can provide some kind of transition information.
 *
 * Unless otherwise specified it is left to the implementation whether for methods to return the
 * same instance as passed with modified values or a completely new instance.
 *
 * @author Jonas Becker-Kupczok
 *
 * @param <InfoT> the type of transition information provided by this handler
 * @param <ValueT>
 */
public interface InformationHandler<InfoT extends ComposableTransitionInformation<InfoT>, ValueT extends AbstractedValue<ValueT, ?, ?>> {

    /**
     * Returns a transition information representing no information, i.e. one that is neutral with
     * respect to {@link ComposableTransitionInformation#compose(ComposableTransitionInformation)}.
     *
     * @return neutral transition information
     */
    InfoT getNeutralInformation();

    /**
     * Called once before the evaluation of some code starts, i.e. at the start of
     * {@link AnalyzedProcess#makeStep(ConsideredState)} and before any update within
     * {@link BaseScheduler#doUpdateCycle(ConsideredState)}.
     * 
     * The result should take into account the information from previous steps already contained in
     * resultingState.
     * 
     * @param <LocalStateT> the type of local start used for the evaluation of that code
     * @param currentState the state at the start of the evaluation
     * @param localState the local part of that state
     * @return the information describing the start of the evaluation
     */
    <LocalStateT extends LocalState<LocalStateT, ValueT>> InfoT handleStartOfCode(
            TransitionResult<?, ?, ?, InfoT, ?> currentState, LocalStateT localState);

    /**
     * Called before any expression is evaluated to allow the handler to prepare for the subsequent
     * invocation of {@link #handleExpressionEvaluation(Expression, int, TransitionResult, LocalState)}.
     * 
     * @param expression the expression to be evaluated (is null when evaluating function body)
     * @param currentState the state before the evaluation
     * @param localState the local part of the state
     */
    default <LocalStateT extends LocalState<LocalStateT, ValueT>> void announceEvaluation(Expression expression,
            TransitionResult<?, ?, ?, InfoT, ?> currentState, LocalStateT localState) {}

    /**
     * Returns the information describing the evaluation step that just occured.
     * 
     * The result should take into account the information from previous steps already contained in
     * resultingState.
     * 
     * @param evaluated the expression that was just evaluated (is null when evaluating function body)
     * @param comingFrom from where the expression was entered
     * @param resultingState the result of the small step
     * @param localState the local part of the result
     * @return the information describing the step
     */
    <LocalStateT extends LocalState<LocalStateT, ValueT>> InfoT handleExpressionEvaluation(Expression evaluated,
            int comingFrom, TransitionResult<?, ?, ?, InfoT, ?> resultingState, LocalStateT localState);

    /**
     * Called once a state transition is complete to allow the handler to clean up transient data or
     * otherwise finalize the information before it is returned to the {@link StateSpaceExploration}.
     *
     * @param information the information gathered for the transition
     * @return the finalized version of that information
     */
    default InfoT finalizeInformation(InfoT information) {
        return information;
    }

    /**
     * Called whenver a process was made ready because the delta cycle it waited for ended.
     * 
     * The result should take into account the current information from previous steps.
     *
     * @param process the made ready process
     * @param resultingState the resulting state of that process
     * @param currentInformation the information gathered from previous steps
     * @return the information describing this step
     */
    InfoT handleProcessWaitedForDelta(AnalyzedProcess<?, ?, ?, ?> process, ProcessState<?, ValueT> resultingState,
            InfoT currentInformation);


    /**
     * Called whenver a process was made ready because the time it waited for passed.
     * 
     * The result should take into account the current information from previous steps.
     *
     * @param process the made ready process
     * @param resultingState the resulting state of that process
     * @param currentInformation the information gathered from previous steps
     * @return the information describing this step
     */
    InfoT handleProcessWaitedForTime(AnalyzedProcess<?, ?, ?, ?> process, ProcessState<?, ValueT> resultingState,
            InfoT currentInformation);


    /**
     * Called whenver an event a process was waiting for was triggered.
     * 
     * The result should take into account the current information from previous steps. The process may
     * not have waited for any of the notified events and therefore remain uneffected.
     *
     * @param process the process
     * @param resultingState the resulting state of that process
     * @param events the events that were notified
     * @param blockerBefore the blocker that was blocking the process before this step
     * @param currentInformation the information gathered from previous steps
     * @return the information describing this step
     */
    InfoT handleProcessWaitedForEvents(AnalyzedProcess<?, ?, ?, ?> process,
            ProcessState<?, ValueT> resultingState, Set<Event> events, EventBlocker blockerBefore,
            InfoT currentInformation);

}
