package de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations;

import de.tub.pes.syscir.analysis.statespace_exploration.AnalyzedProcess;
import de.tub.pes.syscir.analysis.statespace_exploration.ConsideredState;
import de.tub.pes.syscir.analysis.statespace_exploration.GlobalState;
import de.tub.pes.syscir.analysis.statespace_exploration.LocalState;
import de.tub.pes.syscir.analysis.statespace_exploration.ProcessState;
import de.tub.pes.syscir.analysis.statespace_exploration.SchedulerTransitionResult;

public record SchedulerConsideredState<GlobalStateT extends GlobalState<GlobalStateT>, LocalStateT extends LocalState<LocalStateT, ?>, ProcessStateT extends ProcessState<ProcessStateT, ?>, ProcessT extends AnalyzedProcess<ProcessT, GlobalStateT, ProcessStateT, ?>>(
        ConsideredState<GlobalStateT, ProcessStateT, ProcessT> consideredState, LocalStateT schedulerState) {

    public SchedulerConsideredState(
            SchedulerTransitionResult<GlobalStateT, ProcessStateT, LocalStateT, ?, ProcessT> transitionResult) {
        this(transitionResult.resultingState(), transitionResult.schedulerState());
    }

    public void lock() {
        this.consideredState.lock();
        this.schedulerState.lock();
    }

    public SchedulerConsideredState<GlobalStateT, LocalStateT, ProcessStateT, ProcessT> unlockedClone() {
        return new SchedulerConsideredState<>(this.consideredState.unlockedClone(),
                this.schedulerState.unlockedClone());
    }

    public SchedulerConsideredState<GlobalStateT, LocalStateT, ProcessStateT, ProcessT> unlockedVersion() {
        if (this.consideredState.isLocked() || this.schedulerState.isLocked()) {
            return unlockedClone();
        }
        return this;
    }

}
