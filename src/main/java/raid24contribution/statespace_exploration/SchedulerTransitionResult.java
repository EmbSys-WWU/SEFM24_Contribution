package raid24contribution.statespace_exploration;

import raid24contribution.statespace_exploration.standard_implementations.SchedulerConsideredState;

public record SchedulerTransitionResult<GlobalStateT extends GlobalState<GlobalStateT>, ProcessStateT extends ProcessState<ProcessStateT, ?>, LocalStateT extends LocalState<LocalStateT, ?>, InfoT extends TransitionInformation<InfoT>, ProcessT extends AnalyzedProcess<ProcessT, GlobalStateT, ProcessStateT, ?>>(
        ConsideredState<GlobalStateT, ProcessStateT, ProcessT> resultingState, LocalStateT schedulerState,
        InfoT transitionInformation) implements
        TransitionResult<SchedulerTransitionResult<GlobalStateT, ProcessStateT, LocalStateT, InfoT, ProcessT>, GlobalStateT, ProcessStateT, InfoT, ProcessT> {

    public SchedulerTransitionResult(SchedulerConsideredState<GlobalStateT, LocalStateT, ProcessStateT, ProcessT> state,
            InfoT transitionInformation) {
        this(state.consideredState(), state.schedulerState(), transitionInformation);
    }

    @Override
    public SchedulerTransitionResult<GlobalStateT, ProcessStateT, LocalStateT, InfoT, ProcessT> clone() {
        return new SchedulerTransitionResult<>(this.resultingState.unlockedClone(), this.schedulerState.unlockedClone(),
                this.transitionInformation.clone());
    }

    @Override
    public SchedulerTransitionResult<GlobalStateT, ProcessStateT, LocalStateT, InfoT, ProcessT> replaceResultingState(
            ConsideredState<GlobalStateT, ProcessStateT, ProcessT> state) {
        return new SchedulerTransitionResult<>(state, this.schedulerState, this.transitionInformation);
    }

    @Override
    public SchedulerTransitionResult<GlobalStateT, ProcessStateT, LocalStateT, InfoT, ProcessT> replaceTransitionInformation(
            InfoT transitionInformation) {
        return new SchedulerTransitionResult<>(this.resultingState, this.schedulerState, transitionInformation);
    }

}
