package raid24contribution.statespace_exploration.standard_implementations;

import raid24contribution.statespace_exploration.AnalyzedProcess;
import raid24contribution.statespace_exploration.ConsideredState;
import raid24contribution.statespace_exploration.GlobalState;
import raid24contribution.statespace_exploration.LocalState;
import raid24contribution.statespace_exploration.ProcessState;
import raid24contribution.statespace_exploration.SchedulerTransitionResult;

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
