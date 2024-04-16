package raid24contribution.statespace_exploration;

/**
 * Record describing the result of taking a transition, consisting of the resulting state as well as
 * potentially some additional information provided by the {@link AnalyzedProcess} or
 * {@link Scheduler}.
 * <p>
 * This record is the minimal implementation of the interface {@link TransitionResult} and the only
 * one that is used on the top level of the exploration. Other implementations such as
 * {@link SchedulerTransitionResult} are used on lower levels.
 * 
 * @author Jonas Becker-Kupczok
 *
 * @param <GlobalStateT> the type of global state abstraction
 * @param <ProcessStateT> the type of local state abstraction
 * @param <InfoT> the type of additional transition information
 */
public record ProcessTransitionResult<GlobalStateT extends GlobalState<GlobalStateT>, ProcessStateT extends ProcessState<ProcessStateT, ?>, InfoT extends TransitionInformation<InfoT>, ProcessType extends AnalyzedProcess<ProcessType, GlobalStateT, ProcessStateT, ?>>(
        ConsideredState<GlobalStateT, ProcessStateT, ProcessType> resultingState, InfoT transitionInformation)
        implements
        TransitionResult<ProcessTransitionResult<GlobalStateT, ProcessStateT, InfoT, ProcessType>, GlobalStateT, ProcessStateT, InfoT, ProcessType> {

    @Override
    public ProcessTransitionResult<GlobalStateT, ProcessStateT, InfoT, ProcessType> clone() {
        return new ProcessTransitionResult<>(this.resultingState.unlockedClone(), this.transitionInformation.clone());
    }

    @Override
    public ProcessTransitionResult<GlobalStateT, ProcessStateT, InfoT, ProcessType> replaceResultingState(
            ConsideredState<GlobalStateT, ProcessStateT, ProcessType> state) {
        return new ProcessTransitionResult<>(state, this.transitionInformation);
    }

    @Override
    public ProcessTransitionResult<GlobalStateT, ProcessStateT, InfoT, ProcessType> replaceTransitionInformation(
            InfoT transitionInformation) {
        return new ProcessTransitionResult<>(this.resultingState, transitionInformation);
    }
}
