package raid24contribution.statespace_exploration.standard_implementations;

import static raid24contribution.util.WrapperUtil.wrap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.SequencedMap;
import java.util.Set;
import java.util.function.Predicate;
import raid24contribution.sc_model.SCSystem;
import raid24contribution.statespace_exploration.AbstractedValue;
import raid24contribution.statespace_exploration.AnalyzedProcess;
import raid24contribution.statespace_exploration.ConsideredState;
import raid24contribution.statespace_exploration.DeltaTimeBlocker;
import raid24contribution.statespace_exploration.EvaluationContext;
import raid24contribution.statespace_exploration.EventBlocker;
import raid24contribution.statespace_exploration.GlobalState;
import raid24contribution.statespace_exploration.LocalState;
import raid24contribution.statespace_exploration.ProcessBlocker;
import raid24contribution.statespace_exploration.ProcessState;
import raid24contribution.statespace_exploration.ProcessTerminatedBlocker;
import raid24contribution.statespace_exploration.ProcessTransitionResult;
import raid24contribution.statespace_exploration.RealTimedBlocker;
import raid24contribution.statespace_exploration.Scheduler;
import raid24contribution.statespace_exploration.SchedulerTransitionResult;
import raid24contribution.statespace_exploration.StateSpaceExploration;
import raid24contribution.statespace_exploration.TimedBlocker;
import raid24contribution.statespace_exploration.TransitionResult;
import raid24contribution.statespace_exploration.EventBlocker.Event;
import raid24contribution.statespace_exploration.StateSpaceExploration.ExplorationAbortedError;
import raid24contribution.util.CollectionUtil;
import raid24contribution.util.WrappedSCClassInstance;
import raid24contribution.util.WrappedSCFunction;

/**
 * Class implementing a basic scheduler semantic by advancing delta or real time as appropriate and
 * traversing the expression tree and control flow graph of update methods for update phases.
 * 
 * This class appears as immutable and is thread-safe except for changes in the underlying SysCIR.
 * Such changes lead to undefined behavior.
 * 
 * @author Jonas Becker-Kupczok
 *
 * @param <GlobalStateT> the type of global state abstraction which this scheduler implementation can handle
 * @param <ProcessStateT> the type of process state abstraction which this scheduler implementation can handle
 * @param <LocalStateT> the type of local scheduler state abstraction which this scheduler implementation can
 *        handle
 * @param <InfoT> the type of additional transition information which this scheduler implementation can
 *        provide.
 * @param <ValueT> the type of abstracted value which this scheduler implementation can handle
 */
public abstract class BaseScheduler<GlobalStateT extends GlobalState<GlobalStateT>, ProcessStateT extends ProcessState<ProcessStateT, ValueT>, LocalStateT extends LocalState<LocalStateT, ValueT>, ValueT extends AbstractedValue<ValueT, ?, ?>, InfoT extends ComposableTransitionInformation<InfoT>, ProcessT extends AnalyzedProcess<ProcessT, GlobalStateT, ProcessStateT, InfoT>>
extends
ExpressionCrawler<GlobalStateT, ProcessStateT, LocalStateT, SchedulerTransitionResult<GlobalStateT, ProcessStateT, LocalStateT, InfoT, ProcessT>, ValueT, InfoT, ProcessT>
implements Scheduler<GlobalStateT, ProcessStateT, InfoT, ProcessT> {

    private final SimulationStopMode stopMode;
    private final Predicate<? super Event> eventConsiderationCondition;

    // Cache ready processes because canAdvanceSimulation occurs right before getReadyProcesses in
    // StateSpaceExplorer#explore.
    private ThreadLocal<ConsideredState<GlobalStateT, ProcessStateT, ProcessT>> lastOptainedReadyProcessesFor;
    private ThreadLocal<Collection<ProcessT>> lastOptainedReadyProcesses;

    /**
     * Creates a new StandardScheduler with the given stop mode.
     *
     * @param scSystem the SysCIR representation of the entire SystemC design
     * @param stopMode the stop mode for this scheduler
     */
    public BaseScheduler(SCSystem scSystem, InformationHandler<InfoT, ValueT> informationHandler,
            SimulationStopMode stopMode, Predicate<? super Event> eventConsiderationCondition) {
        super(scSystem, null, informationHandler);

        this.stopMode = Objects.requireNonNull(stopMode);
        this.eventConsiderationCondition = Objects.requireNonNull(eventConsiderationCondition);

        this.lastOptainedReadyProcessesFor = new ThreadLocal<>();
        this.lastOptainedReadyProcesses = new ThreadLocal<>();
    }

    @Override
    public SimulationStopMode getStopMode(ConsideredState<GlobalStateT, ProcessStateT, ProcessT> currentState) {
        return this.stopMode;
    }

    @Override
    public LocalStateT getLocalState(
            SchedulerTransitionResult<GlobalStateT, ProcessStateT, LocalStateT, InfoT, ProcessT> currentState) {
        return currentState.schedulerState();
    }

    /**
     * {@inheritDoc}
     * <p>
     * If {@link GlobalState#isSimulationStopped()} returns true, the result is an empty
     * collection.
     */
    @Override
    public Collection<ProcessT> getReadyProcesses(ConsideredState<GlobalStateT, ProcessStateT, ProcessT> currentState) {
        if (currentState.getGlobalState().isSimulationStopped()
                && this.stopMode == SimulationStopMode.SC_STOP_FINISH_IMMEDIATE) {
            return Set.of();
        }

        Collection<ProcessT> guaranteedReady = getReadyProcessesInternal(currentState);
        Set<ProcessT> maybeReady = new LinkedHashSet<>();
        for (Entry<ProcessT,ProcessStateT> entry : currentState.getProcessStates().entrySet()) {
            ProcessStateT state = entry.getValue();
            if (!(state.getWaitingFor() instanceof EventBlocker eb) ){
                continue;
            }
            if (eb.isChoice()) {
                if (!eb.getEvents().stream().allMatch(this.eventConsiderationCondition)) {
                    maybeReady.add(entry.getKey());
                }
            } else {
                if (!eb.getEvents().stream().anyMatch(this.eventConsiderationCondition)) {
                    maybeReady.add(entry.getKey());
                }
            }
        }

        if (maybeReady.isEmpty()) {
            return guaranteedReady;
        }

        maybeReady.addAll(guaranteedReady);
        return maybeReady;
    }

    /**
     * {@inheritDoc}
     * <p>
     * If {@link GlobalState#isSimulationStopped()} returns true, the result is false.
     */
    @Override
    public boolean canEndEvaluation(ConsideredState<GlobalStateT, ProcessStateT, ProcessT> currentState) {
        if (currentState.getGlobalState().isSimulationStopped()
                && this.stopMode == SimulationStopMode.SC_STOP_FINISH_IMMEDIATE) {
            return false;
        }

        return getReadyProcessesInternal(currentState).isEmpty();
    }

    /**
     * Returns all processes that are guaranteed to be ready.
     * 
     * This method ignores the result of {@link GlobalState#isSimulationStopped()}.
     * 
     * @param currentState the current state
     * @return collection of processes guaranteed to be ready
     */
    protected Collection<ProcessT> getReadyProcessesInternal(ConsideredState<GlobalStateT, ProcessStateT, ProcessT> currentState) {
        if (currentState == this.lastOptainedReadyProcessesFor.get()) {
            return this.lastOptainedReadyProcesses.get();
        }

        Collection<ProcessT> result = currentState.getReadyProcesses();
        if (currentState.isLocked()) {
            this.lastOptainedReadyProcessesFor.set(currentState);
            this.lastOptainedReadyProcesses.set(result);
        }
        return result;
    }

    @Override
    public Set<ProcessTransitionResult<GlobalStateT, ProcessStateT, InfoT, ProcessT>> endEvaluation(ConsideredState<GlobalStateT, ProcessStateT, ProcessT> currentState) {
        StateSpaceExploration<?, ?, ?, ?> explorer = StateSpaceExploration.getCurrentExplorer();
        if (explorer.isAborted()) {
            throw new ExplorationAbortedError();
        }

        currentState = currentState.unlockedClone();

        // do update cycle
        Map<ConsideredState<GlobalStateT, ProcessStateT, ProcessT>, InfoT> updateResults = doUpdateCycle(currentState);

        Set<ProcessTransitionResult<GlobalStateT, ProcessStateT, InfoT, ProcessT>> results = new LinkedHashSet<>();
        for (Entry<ConsideredState<GlobalStateT, ProcessStateT, ProcessT>, InfoT> updateResult : updateResults
                .entrySet()) {
            if (explorer.isAborted()) {
                throw new ExplorationAbortedError();
            }

            ProcessTransitionResult<GlobalStateT, ProcessStateT, InfoT, ProcessT> asTransitionResult =
                    new ProcessTransitionResult<>(updateResult.getKey(), updateResult.getValue());
            ProcessTransitionResult<GlobalStateT, ProcessStateT, InfoT, ProcessT> result =
                    advanceSimulation(asTransitionResult);
            if (result == null) {
                // TODO: wrong? explorer would want to explore further from here, leading to another scheduler call.
                // but, would that do anything bad?
                results.add(finalizeTransitionResult(asTransitionResult));
            } else {
                results.add(finalizeTransitionResult(result));
            }
        }

        return results;
    }

    // parameters and results unlocked
    // TODO: should always be deterministic, right? so only one result, no set?
    // TODO: meh, maybe not? at least order of updates doesn't matter, right? RIGHT?!
    public Map<ConsideredState<GlobalStateT, ProcessStateT, ProcessT>, InfoT> doUpdateCycle(
            ConsideredState<GlobalStateT, ProcessStateT, ProcessT> currentState) {
        List<WrappedSCClassInstance> requestedUpdates =
                new ArrayList<>(currentState.getGlobalState().getRequestedUpdates());
        currentState.getGlobalState().setRequestedUpdates(new LinkedHashSet<>());
        Map<ConsideredState<GlobalStateT, ProcessStateT, ProcessT>, InfoT> currentTransitions =
                Map.of(currentState, getNeutralInformation());

        for (WrappedSCClassInstance requested : requestedUpdates) {
            currentTransitions = updatePort(requested, currentTransitions);
        }

        return currentTransitions;
    }

    public Map<ConsideredState<GlobalStateT, ProcessStateT, ProcessT>, InfoT> updatePort(
            WrappedSCClassInstance port,
            Map<ConsideredState<GlobalStateT, ProcessStateT, ProcessT>, InfoT> ingoingTransitions) {
        Map<SchedulerConsideredState<GlobalStateT, LocalStateT, ProcessStateT, ProcessT>, InfoT> seenTransitions =
                new LinkedHashMap<>();
        SequencedMap<SchedulerConsideredState<GlobalStateT, LocalStateT, ProcessStateT, ProcessT>, InfoT> transitionsToHandle =
                new LinkedHashMap<>();
        Map<ConsideredState<GlobalStateT, ProcessStateT, ProcessT>, InfoT> resultingTransitions = new LinkedHashMap<>();

        for (Entry<ConsideredState<GlobalStateT, ProcessStateT, ProcessT>, InfoT> ingoingTransition : ingoingTransitions
                .entrySet()) {
            // TODO: can there be more than one channel per port? why?
            WrappedSCFunction updateFunction = wrap(port.getSCClass().getMemberFunctionByName("update"));
            LocalStateT localState = constructLocalSchedulerState(port, updateFunction);

            SchedulerConsideredState<GlobalStateT, LocalStateT, ProcessStateT, ProcessT> initialConsideredState =
                    new SchedulerConsideredState<>(ingoingTransition.getKey(), localState);
            SchedulerTransitionResult<GlobalStateT, ProcessStateT, LocalStateT, InfoT, ProcessT> initialTransitionResult =
                    new SchedulerTransitionResult<>(initialConsideredState, ingoingTransition.getValue());
            InfoT initialInformation = getInformationHandler().handleStartOfCode(initialTransitionResult, localState);
            transitionsToHandle.put(initialConsideredState, initialInformation);
        }

        while (!transitionsToHandle.isEmpty()) {
            SchedulerTransitionResult<GlobalStateT, ProcessStateT, LocalStateT, InfoT, ProcessT> nextToHandle =
                    new SchedulerTransitionResult<>(transitionsToHandle.firstEntry().getKey(),
                            transitionsToHandle.pollFirstEntry().getValue());
            SmallStepResult<GlobalStateT, ProcessStateT, InfoT, SchedulerTransitionResult<GlobalStateT, ProcessStateT, LocalStateT, InfoT, ProcessT>> nextTransitions =
                    makeSmallStep(nextToHandle);

            if (nextTransitions.endOfStep()) {
                for (SchedulerTransitionResult<GlobalStateT, ProcessStateT, LocalStateT, InfoT, ProcessT> transition : nextTransitions.transitions()) {
                    resultingTransitions.merge(transition.resultingState(), transition.transitionInformation(),
                            InfoT::compose);
                }
            } else if (nextTransitions.possiblyRepeatingStep()) {
                // make sure that possibly repeating steps are only considered once
                for (SchedulerTransitionResult<GlobalStateT, ProcessStateT, LocalStateT, InfoT, ProcessT> transition : nextTransitions.transitions()) {
                    SchedulerConsideredState<GlobalStateT, LocalStateT, ProcessStateT, ProcessT> state =
                            new SchedulerConsideredState<>(transition);
                    state.lock();

                    InfoT oldInformation = seenTransitions.get(state);
                    if (transition.transitionInformation().equals(oldInformation)) {
                        continue;
                    }
                    InfoT newInformation =
                            seenTransitions.merge(state, transition.transitionInformation(), InfoT::compose);
                    if (newInformation.equals(oldInformation)) {
                        continue;
                    }

                    transitionsToHandle.merge(state.unlockedClone(), newInformation.clone(), InfoT::compose);
                }
            } else {
                for (SchedulerTransitionResult<GlobalStateT, ProcessStateT, LocalStateT, InfoT, ProcessT> transition : nextTransitions
                        .transitions()) {
                    SchedulerConsideredState<GlobalStateT, LocalStateT, ProcessStateT, ProcessT> state =
                            new SchedulerConsideredState<>(transition);
                    transitionsToHandle.merge(state.unlockedVersion(), transition.transitionInformation(),
                            InfoT::compose);
                }
            }
        }

        return resultingTransitions;
    }

    public abstract LocalStateT constructLocalSchedulerState(WrappedSCClassInstance port, WrappedSCFunction entryPoint);

    @Override
    public SmallStepResult<GlobalStateT, ProcessStateT, InfoT, SchedulerTransitionResult<GlobalStateT, ProcessStateT, LocalStateT, InfoT, ProcessT>> handleEndOfCodeReached(
            SchedulerTransitionResult<GlobalStateT, ProcessStateT, LocalStateT, InfoT, ProcessT> currentState, LocalStateT localState,
            List<EvaluationContext<ValueT>> stack) {
        return createSmallStepResult(null, -2, currentState, localState, true, false);
    }

    public ProcessTransitionResult<GlobalStateT, ProcessStateT, InfoT, ProcessT> advanceSimulation(
            ProcessTransitionResult<GlobalStateT, ProcessStateT, InfoT, ProcessT> updateResult) {
        ConsideredState<GlobalStateT, ProcessStateT, ProcessT> currentState = updateResult.resultingState();
        InfoT currentInformation = updateResult.transitionInformation();

        // finish simulation of sc_stop() has been called
        if (currentState.getGlobalState().isSimulationStopped()) {
            // stopMode must be finish delta, otherwise canAdvanceSimulation returns false
            assert this.stopMode == SimulationStopMode.SC_STOP_FINISH_DELTA;

            currentState.lock();
            return updateResult;
        }

        // group processes and events by what they are waiting for
        Set<Entry<ProcessT, ProcessStateT>> deltaWaitingProcesses = new LinkedHashSet<>();
        Set<Entry<ProcessT, ProcessStateT>> timeWaitingProcesses = new LinkedHashSet<>();
        Set<Entry<ProcessT, ProcessStateT>> eventWaitingProcesses = new LinkedHashSet<>();

        Set<Event> deltaWaitingEvents = new LinkedHashSet<>();

        RealTimedBlocker earliestBlocker = null;

        // find processes waiting for delta, time or event
        for (Entry<ProcessT, ProcessStateT> entry : currentState.getProcessStates().entrySet()) {
            if (entry.getValue().getWaitingFor() == DeltaTimeBlocker.INSTANCE) {
                deltaWaitingProcesses.add(entry);
            } else if (entry.getValue().getWaitingFor() instanceof RealTimedBlocker rtb) {
                timeWaitingProcesses.add(entry);
                if (earliestBlocker == null || rtb.compareTo(earliestBlocker) < 0) {
                    earliestBlocker = rtb;
                }
            } else if (entry.getValue().getWaitingFor() instanceof EventBlocker eb) {
                eventWaitingProcesses.add(entry);
                if (eb.getTimeout() != null) {
                    if (eb.getTimeout() == DeltaTimeBlocker.INSTANCE) {
                        deltaWaitingProcesses.add(entry);
                    } else {
                        timeWaitingProcesses.add(entry);
                        if (earliestBlocker == null || eb.getTimeout().compareTo(earliestBlocker) < 0) {
                            earliestBlocker = (RealTimedBlocker) eb.getTimeout();
                        }
                    }
                }
            } else {
                // no process should be ready!
                assert entry.getValue().getWaitingFor() == ProcessTerminatedBlocker.INSTANCE;
            }
        }

        // find events waiting for delta or time
        for (Entry<Event, TimedBlocker> entry : currentState.getGlobalState().getEventsWithStates()) {
            if (entry.getValue() == DeltaTimeBlocker.INSTANCE) {
                deltaWaitingEvents.add(entry.getKey());
            } else if (entry.getValue() instanceof RealTimedBlocker rtb) {
                if (earliestBlocker == null || rtb.compareTo(earliestBlocker) < 0) {
                    earliestBlocker = rtb;
                }
            } else {
                // events are either waiting for delta or for real time, or they are not pending (and then should
                // not be included in the map)
                assert false;
            }
        }

        // if something waits for delta, do delta cycle
        if (!deltaWaitingProcesses.isEmpty() || !deltaWaitingEvents.isEmpty()) {
            return doDeltaCycle(currentState, currentInformation, deltaWaitingProcesses, eventWaitingProcesses,
                    deltaWaitingEvents);
        }

        // if no process or event is waiting for delta or time, nothing will ever happen again
        if (timeWaitingProcesses.isEmpty() && currentState.getGlobalState().getEventStates().isEmpty()) {
            return null; // end of simulation
        }

        return letTimePass(currentState, currentInformation, timeWaitingProcesses, eventWaitingProcesses,
                earliestBlocker);
    }

    public ProcessTransitionResult<GlobalStateT, ProcessStateT, InfoT, ProcessT> doDeltaCycle(
            ConsideredState<GlobalStateT, ProcessStateT, ProcessT> currentState,
            InfoT currentInformation, Set<Entry<ProcessT, ProcessStateT>> deltaWaitingProcesses,
            Set<Entry<ProcessT, ProcessStateT>> eventWaitingProcesses, Set<Event> deltaWaitingEvents) {
        // compute new event states by replacing those waiting for delta by ones not pending
        Map<Event, TimedBlocker> eventStates = currentState.getGlobalState().getEventStates();
        for (Event event : deltaWaitingEvents) {
            eventStates.remove(event);
        }

        // processes waiting on events just notified are considered waiting for the same delta cycle
        // TODO: assuming that process waiting for delta-waiting event becomes ready in the same delta cycle
        // the event is called
        for (Entry<ProcessT, ProcessStateT> entry : eventWaitingProcesses) {
            currentInformation = notifyEventsForProcess(entry, currentState, deltaWaitingEvents, currentInformation);
        }

        // compute new process states by replacing those waiting for delta by ready ones
        for (Entry<ProcessT, ProcessStateT> entry : deltaWaitingProcesses) {
            entry.getValue().setWaitingFor(null);
            currentInformation = getInformationHandler().handleProcessWaitedForDelta(entry.getKey(), entry.getValue(),
                    currentInformation);
        }

        currentState.lock();
        return new ProcessTransitionResult<>(currentState, currentInformation);
    }

    public ProcessTransitionResult<GlobalStateT, ProcessStateT, InfoT, ProcessT> letTimePass(
            ConsideredState<GlobalStateT, ProcessStateT, ProcessT> currentState,
            InfoT currentInformation, Set<Entry<ProcessT, ProcessStateT>> timeWaitingProcesses,
            Set<Entry<ProcessT, ProcessStateT>> eventWaitingProcesses,
            RealTimedBlocker earliestBlocker) {
        Set<Event> notifiedEvents = new LinkedHashSet<>();
        // compute new event states by replacing those waiting for the shortest time by ones not pending and
        // subtracting the waited time from all others
        Map<Event, TimedBlocker> eventStates = currentState.getGlobalState().getEventStates();
        Iterator<Entry<Event, TimedBlocker>> entryIt = eventStates.entrySet().iterator();
        while (entryIt.hasNext()) {
            Entry<Event, TimedBlocker> entry = entryIt.next();
            RealTimedBlocker rtb = (RealTimedBlocker) entry.getValue();

            if (entry.getValue().equals(earliestBlocker)) {
                notifiedEvents.add(entry.getKey());
                entryIt.remove();
            } else {
                entry.setValue(rtb.subtract(earliestBlocker));
            }
        }

        // processes waiting on events just notified are considered waiting for the same delta cycle
        // TODO: assuming that process waiting for time-waiting event becomes ready immediately when the
        // event is called
        for (Entry<ProcessT, ProcessStateT> entry : eventWaitingProcesses) {
            currentInformation = notifyEventsForProcess(entry, currentState, notifiedEvents, currentInformation);
        }

        // compute new process states by replacing those waiting for the shortest time or for events that
        // are notified by ready ones and subtracting the waited time from all others
        for (Entry<ProcessT, ProcessStateT> entry : timeWaitingProcesses) {
            ProcessBlocker waitingFor = entry.getValue().getWaitingFor();
            RealTimedBlocker timer;
            if (waitingFor == null) {
                // might have become ready by notifying an event
                continue;
            } else if (entry.getValue().getWaitingFor() instanceof EventBlocker eb) {
                // waitingFor might already have been modified by removing some events
                timer = (RealTimedBlocker) eb.getTimeout();
            } else {
                timer = (RealTimedBlocker) waitingFor;
            }

            // reduce waiting time
            ProcessBlocker replacement = timer.equals(earliestBlocker) ? null : timer.subtract(earliestBlocker);
            if (replacement != null && waitingFor instanceof EventBlocker eb) {
                replacement = eb.replaceTimeout((RealTimedBlocker) replacement);
            }

            entry.getValue().setWaitingFor(replacement);
            currentInformation = getInformationHandler().handleProcessWaitedForTime(entry.getKey(), entry.getValue(),
                    currentInformation);
        }

        currentState.lock();
        return new ProcessTransitionResult<>(currentState, currentInformation);
    }

    /**
     * Called for each process waiting for an EventBlocker whenever events are notified.
     * 
     * Updates the waitingFor state of the process accordingly.
     * 
     * @param entry
     * @param currentState
     * @param notifiedEvents
     * @param notifiedProcesses
     * @param newProcessStates
     */
    private InfoT notifyEventsForProcess(Entry<ProcessT, ProcessStateT> entry,
            ConsideredState<GlobalStateT, ProcessStateT, ProcessT> currentState, Set<Event> notifiedEvents,
            InfoT currentInformation) {
        EventBlocker blocker = ((EventBlocker) entry.getValue().getWaitingFor());

        Set<Event> remainingEvents = CollectionUtil.setDiff(blocker.getEvents(), notifiedEvents);

        if (remainingEvents.size() == blocker.getEvents().size()) {
            return currentInformation;
        }

        if (remainingEvents.isEmpty() || blocker.isChoice()) {
            entry.getValue().setWaitingFor(null);
        } else {
            entry.getValue().setWaitingFor(blocker.replaceEvents(remainingEvents));
        }
        return getInformationHandler().handleProcessWaitedForEvents(entry.getKey(), entry.getValue(), notifiedEvents,
                blocker,
                currentInformation);

    }

    @Override
    public <TransitionResultT extends TransitionResult<TransitionResultT, GlobalStateT, ProcessStateT, InfoT, ProcessT>> Collection<TransitionResultT> notifyEvents(TransitionResultT transitionResult,
            Event event, TimedBlocker delay) {
        ConsideredState<GlobalStateT, ProcessStateT, ProcessT> currentState = transitionResult.resultingState();

        if (currentState.getGlobalState().isSimulationStopped()
                && this.stopMode == Scheduler.SimulationStopMode.SC_STOP_FINISH_IMMEDIATE) {
            return Set.of(transitionResult);
        }

        if (!this.eventConsiderationCondition.test(event)) {
            return Set.of(transitionResult);
        }

        boolean wasLocked = currentState.isLocked();
        currentState = currentState.unlockedVersion();
        InfoT currentInformation = transitionResult.transitionInformation();

        Map<Event, TimedBlocker> eventStates = currentState.getGlobalState().getEventStates();

        if (delay != null) {
            eventStates.merge(event, delay,
                    (oldBlock, newBlock) -> oldBlock.compareTo(newBlock) <= 0 ? oldBlock : newBlock);
        } else {
            eventStates.remove(event);

            Iterator<Entry<ProcessT, ProcessStateT>> entryIt = currentState.getProcessStates().entrySet().iterator();
            while (entryIt.hasNext()) {
                Entry<ProcessT, ProcessStateT> entry = entryIt.next();

                if (!(entry.getValue().getWaitingFor() instanceof EventBlocker eb)) {
                    continue;
                }

                Set<Event> remainingEvents = CollectionUtil.setDiff(eb.getEvents(), Set.of(event));
                if (remainingEvents.size() == eb.getEvents().size()) {
                    continue;
                }

                if (eb.isChoice() || remainingEvents.isEmpty()) {
                    entry.getValue().setWaitingFor(null);
                } else {
                    entry.getValue().setWaitingFor(eb.replaceEvents(remainingEvents));
                }
                currentInformation = getInformationHandler().handleProcessWaitedForEvents(entry.getKey(), entry.getValue(),
                        Set.of(event), eb, currentInformation);
            }
        }

        if (wasLocked) {
            currentState.lock();
        }

        return Set.of(transitionResult.replaceResultingState(currentState));
    }

    @Override
    public <R extends TransitionResult<R, GlobalStateT, ProcessStateT, InfoT, ProcessT>> Collection<R> stopSimulation(R transitionResult) {
        ConsideredState<GlobalStateT, ProcessStateT, ProcessT> currentState = transitionResult.resultingState();

        boolean wasLocked = currentState.isLocked();
        currentState = currentState.unlockedVersion();

        currentState.getGlobalState().setSimulationStopped(true);

        for (Entry<ProcessT, ProcessStateT> entry : currentState.getProcessStates().entrySet()) {
            ProcessStateT state = entry.getValue();
            // if only stopping after delta cycle, ignore processes that are ready
            if (this.stopMode == SimulationStopMode.SC_STOP_FINISH_DELTA) {
                if (state.getWaitingFor() == null || state.getWaitingFor() instanceof EventBlocker) {
                    continue;
                }
            }

            state.setWaitingFor(ProcessTerminatedBlocker.INSTANCE);
        }

        currentState.getGlobalState().getEventStates().clear();

        if (wasLocked) {
            currentState.lock();
        }

        return Set.of(transitionResult.replaceResultingState(currentState));
    }

}
