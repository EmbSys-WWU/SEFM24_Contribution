package de.tub.pes.syscir.analysis.statespace_exploration;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;

/**
 * Implements an exhaustive brute force exploration of the state space of a SystemC model.
 * 
 * From an initial state, every possible transition to a follow-up state is considered. The
 * {@link ExplorationRecord} supplied at construction is notified of the taken transition.
 * Implementations of {@link AnalyzedProcess} may provide additional information of type
 * {@link TransitionInformation} that is also forwarded to the {@link ExplorationRecord}. If the
 * follow-up state has not yet been explored, it will be explored as well.
 * <p>
 * The exploration only terminates after every reachable state has been explored. If the considered
 * state space is infinite, the exploration will not terminate. The precision with which the state
 * space is considered can be controlled by using suitable implementations of {@link Scheduler},
 * {@link AnalyzedProcess}, {@link GlobalState} and {@link ProcessState}.
 * <p>
 * All implementations of {@link AnalyzedProcess} and {@link Scheduler} used for state space
 * exploration must ensure that all states resulting from
 * {@link AnalyzedProcess#makeStep(ConsideredState)} or
 * {@link Scheduler#endEvaluation(ConsideredState)} are locked (see {@link ConsideredState#lock()}).
 * 
 * @author Jonas Becker-Kupczok
 *
 * @param <GlobalStateT> the type of {@link GlobalState} used in the exploration
 * @param <ProcessStateT> the type of {@link ProcessState} used in the exploration
 * @param <InfoT> the type of {@link TransitionInformation} supplied by the {@link AnalyzedProcess}
 */
public abstract class StateSpaceExploration<GlobalStateT extends GlobalState<GlobalStateT>, ProcessStateT extends ProcessState<ProcessStateT, ?>, InfoT extends TransitionInformation<InfoT>, ProcessT extends AnalyzedProcess<ProcessT, GlobalStateT, ProcessStateT, InfoT>> {

    /**
     * Thrown whenever a part of the exploration finds that the exploration has been aborted.
     *
     * @author Jonas Becker-Kupczok
     *
     */
    public static class ExplorationAbortedError extends Error {

        private static final long serialVersionUID = 1L;

    }

    // store all currently running explorations to allow any part of the exploration to access the
    // explorer, mainly to check for abortion.
    private static ThreadLocal<StateSpaceExploration<?, ?, ?, ?>> currentExplorer = new ThreadLocal<>();

    /**
     * Returns the StateSpaceExploration currently running on this thread, if any.
     *
     * @return explorer running on the current thread
     */
    public static StateSpaceExploration<?, ?, ?, ?> getCurrentExplorer() {
        return currentExplorer.get();
    }

    /**
     * Sets the StateSpaceExploration currently running on this thread, or removes it if the parameter
     * is null.
     *
     * @param explorer the explorer now running on the current thread, or null
     */
    protected static void setCurrentExplorer(StateSpaceExploration<?, ?, ?, ?> explorer) {
        if (explorer == null) {
            currentExplorer.remove();
        } else {
            if (currentExplorer.get() != null) {
                throw new IllegalStateException("no two explorations may run on the same thread at the same time");
            }
            currentExplorer.set(explorer);
        }
    }

    private Scheduler<GlobalStateT, ProcessStateT, InfoT, ProcessT> scheduler;
    private ExplorationRecord<GlobalStateT, ProcessStateT, InfoT> record;

    private volatile boolean aborted;
    private volatile boolean done;

    /**
     * Constructs a new StateSpaceExploration using the given scheduler and recording all state
     * transitions in the given record.
     * 
     * @param scheduler the {@link Scheduler} implementation used for this exploration
     * @param record the {@link ExplorationRecord} informed about every possible state transition
     */
    public StateSpaceExploration(Scheduler<GlobalStateT, ProcessStateT, InfoT, ProcessT> scheduler,
            ExplorationRecord<GlobalStateT, ProcessStateT, InfoT> record) {
        this.scheduler = Objects.requireNonNull(scheduler);
        this.record = Objects.requireNonNull(record);
    }

    /**
     * Returns the scheduler used for the exploration.
     * 
     * @return scheduler
     */
    protected Scheduler<GlobalStateT, ProcessStateT, InfoT, ProcessT> getScheduler() {
        return this.scheduler;
    }

    /**
     * Returns the record for all state transitions.
     * 
     * @return record
     */
    protected ExplorationRecord<GlobalStateT, ProcessStateT, InfoT> getRecord() {
        return this.record;
    }

    /**
     * Performs the state space exploration.
     * 
     * This will only return after all reachable states have been explored.
     */
    public abstract void run();

    /**
     * Performs one step of the exploration, meaning to find all possible successors to one state.
     * {@link handleExploration(ConsideredState, Collection)} is called for all resulting transitions,
     * possibly multiple times with different transitions for one exploration step.
     */
    protected int explorationStep(ConsideredState<GlobalStateT, ProcessStateT, ProcessT> currentState) {
        int result = 0;

        // The scheduler can advance the simulation if it's possible for no process to by ready. If the
        // state of some processes is abstracted, this might not be the same as
        // currentState#getReadyProcesses returning an empty set.
        if (getScheduler().canEndEvaluation(currentState)) {
            Set<ProcessTransitionResult<GlobalStateT, ProcessStateT, InfoT, ProcessT>> transitions =
                    getScheduler().endEvaluation(currentState);
            // if the set of transitions is empty, the end of the simulation has been reached
            result += transitions.size();
            handleExploration(currentState, transitions);
        }

        // Scheduler#getReadyProcesses may differ from ConsideredState#getReadyProcesses in that the latter
        // will only include processes that definitely are ready whereas the former may also include
        // processes whose scheduler state is simply unknown.
        Collection<ProcessT> ready = getScheduler().getReadyProcesses(currentState);
        for (ProcessT selectedProcess : ready) {
            Set<ProcessTransitionResult<GlobalStateT, ProcessStateT, InfoT, ProcessT>> transitions =
                    selectedProcess.makeStep(currentState);
            assert !transitions.isEmpty(); // a ready process should be able to make at least one transition
            result += transitions.size();
            handleExploration(currentState, transitions);
        }

        return result;
    }

    /**
     * Adds the follow-up state to the worklist and records the transition.
     * 
     * @param from some state
     * @param transitions some transitions from that state
     */
    protected abstract void handleExploration(ConsideredState<GlobalStateT, ProcessStateT, ProcessT> from,
            Collection<ProcessTransitionResult<GlobalStateT, ProcessStateT, InfoT, ProcessT>> transitions);

    /**
     * Returns the current number of states which are currently waiting to be explored.
     * 
     * This method is safe to call from any thread. If serves merely as a way to monitor the
     * explorations progress.
     * 
     * @return the number of states in the worklist
     */
    public abstract int getNumPendingStates();

    /**
     * Returns the current number of states already explored during the exploration.
     * 
     * This method is safe to call from any thread. If serves merely as a way to monitor the
     * explorations progress.
     * 
     * @return the number of states already explored
     */
    public abstract int getNumExploredStates();

    /**
     * Returns an unmodifiable view on the set of states explored during the exploration.
     *
     * @return unmodifiable view on the set of explored states
     */
    public abstract Set<ConsideredState<GlobalStateT, ProcessStateT, ProcessT>> getExploredStates();

    /**
     * Returns whether or not the exploration has been aborted for any reason.
     * 
     * This method is safe to call from any thread.
     * 
     * @return aborted
     */
    public final boolean isAborted() {
        return this.aborted;
    }

    /**
     * Aborts this exploration.
     * 
     * Implementations should regularly check for {@link #isAborted()} to react to an abortion.
     * <p>
     * This method is safe to call from any thread.
     */
    public void abort() {
        this.aborted = true;
    }

    public final boolean isDone() {
        return this.done;
    }

    protected void done() {
        this.done = true;
    }

}
