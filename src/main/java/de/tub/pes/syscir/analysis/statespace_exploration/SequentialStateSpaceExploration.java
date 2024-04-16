package de.tub.pes.syscir.analysis.statespace_exploration;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Queue;
import java.util.Set;

/**
 * A sequential implementation of the state space exploration.
 * 
 * @author Jonas Becker-Kupczok
 *
 * @param <GlobalStateT> the type of {@link GlobalState} used in the exploration
 * @param <ProcessStateT> the type of {@link ProcessState} used in the exploration
 * @param <InfoT> the type of {@link TransitionInformation} supplied by the {@link AnalyzedProcess}
 */
public class SequentialStateSpaceExploration<GlobalStateT extends GlobalState<GlobalStateT>, ProcessStateT extends ProcessState<ProcessStateT, ?>, InfoT extends TransitionInformation<InfoT>, ProcessType extends AnalyzedProcess<ProcessType, GlobalStateT, ProcessStateT, InfoT>>
extends StateSpaceExploration<GlobalStateT, ProcessStateT, InfoT, ProcessType> {

    private Set<ConsideredState<GlobalStateT, ProcessStateT, ProcessType>> exploredStates;
    private Queue<ConsideredState<GlobalStateT, ProcessStateT, ProcessType>> worklist;

    private volatile int numExploredStates;

    /**
     * Constructs a new SequentialStateSpaceExploration using the given scheduler, starting at the given
     * initialState and recording all state transitions in the given record.
     * <p>
     * All initial states are locked by this constructor.
     * 
     * @param scheduler the {@link Scheduler} implementation used for this exploration
     * @param initialStates the {@link ConsideredState}s from which the exploration shall be conducted
     * @param record the {@link ExplorationRecord} informed about every possible state transition
     */
    public SequentialStateSpaceExploration(Scheduler<GlobalStateT, ProcessStateT, InfoT, ProcessType> scheduler,
            ExplorationRecord<GlobalStateT, ProcessStateT, InfoT> record,
            Set<ConsideredState<GlobalStateT, ProcessStateT, ProcessType>> initialStates) {
        super(scheduler, record);

        this.exploredStates = new LinkedHashSet<>();
        this.worklist = new ArrayDeque<>();

        for (ConsideredState<GlobalStateT, ProcessStateT, ProcessType> state : initialStates) {
            this.worklist.add(state);
            state.lock();
        }
    }

    @Override
    public void run() {
        setCurrentExplorer(this);

        try {
            while (!this.worklist.isEmpty()) {
                ConsideredState<GlobalStateT, ProcessStateT, ProcessType> currentState = this.worklist.poll();
                if (!this.exploredStates.add(currentState)) {
                    continue;
                }

                explorationStep(currentState);

                this.numExploredStates = this.exploredStates.size();
            }
        } catch (ExplorationAbortedError e) {
            return;
        } finally {
            setCurrentExplorer(null);
        }

        if (!isAborted()) {
            done();
        }
    }

    @Override
    protected void handleExploration(ConsideredState<GlobalStateT, ProcessStateT, ProcessType> from,
            Collection<ProcessTransitionResult<GlobalStateT, ProcessStateT, InfoT, ProcessType>> transitions) {
        for (ProcessTransitionResult<GlobalStateT, ProcessStateT, InfoT, ProcessType> transition : transitions) {
            this.worklist.add(transition.resultingState());
            getRecord().explorationMade(from, transition.resultingState(), transition.transitionInformation());
        }
    }

    @Override
    public int getNumPendingStates() {
        return this.worklist.size();
    }

    @Override
    public int getNumExploredStates() {
        return this.numExploredStates;
    }

    /**
     * {@inheritDoc}
     * 
     * The resulting set is <strong>not synchronized</strong>. It should only be used after
     * {@link #run()} has returned and the calling thread has been joined with the exploring thread.
     *
     * @return unmodifiable view on the set of explored states
     */
    @Override
    public Set<ConsideredState<GlobalStateT, ProcessStateT, ProcessType>> getExploredStates() {
        return Collections.unmodifiableSet(this.exploredStates);
    }

}
