package de.tub.pes.syscir.statespace_exploration;

import de.tub.pes.syscir.statespace_exploration.transition_informations.TwoInformations;
import de.tub.pes.syscir.statespace_exploration.transition_informations.TwoInformationsHandler;
import java.util.function.Function;

/**
 * Interface describing the result of taking a transition, consisting of the resulting state as well
 * as potentially some additional information provided by the {@link AnalyzedProcess} or
 * {@link Scheduler}.
 * 
 * @author Jonas Becker-Kupczok
 *
 * @param <TransitionResultT> the type of this result (to specify the return type of
 *        {@link #clone()} and {@link #replaceResultingState(ConsideredState)}
 * @param <GlobalStateT> the type of global state abstraction
 * @param <ProcessStateT> the type of local state abstraction
 * @param <InfoT> the type of additional transition information
 */
public interface TransitionResult<TransitionResultT extends TransitionResult<TransitionResultT, GlobalStateT, ProcessStateT, InfoT, ProcessT>, GlobalStateT extends GlobalState<GlobalStateT>, ProcessStateT extends ProcessState<ProcessStateT, ?>, InfoT extends TransitionInformation<InfoT>, ProcessT extends AnalyzedProcess<ProcessT, GlobalStateT, ProcessStateT, ?>> {

    /**
     * Returns the state in which the transition results.
     *
     * @return resulting state
     */
    ConsideredState<GlobalStateT, ProcessStateT, ProcessT> resultingState();

    /**
     * Returns the global portion of the state in which the transition results.
     *
     * @return resulting global state
     */
    default GlobalStateT globalState() {
        return resultingState().getGlobalState();
    }

    /**
     * Returns the additional information provided for this transition.
     *
     * @return transition information
     */
    InfoT transitionInformation();

    /**
     * Returns a (shallow) copy of this result with the given resulting state and all else being equal.
     *
     * @param state the new resulting state
     * @return copy of this result with the given resulting state
     */
    TransitionResultT replaceResultingState(ConsideredState<GlobalStateT, ProcessStateT, ProcessT> state);

    /**
     * Returns a (shallow) copy of this result with the given transition information and all else being
     * equal.
     *
     * @param state the new resulting state
     * @return copy of this result with the given resulting state
     */
    TransitionResultT replaceTransitionInformation(InfoT transitionInformation);

    /**
     * Returns a deep copy of this result. The resulting state of the copy will be unlocked.
     *
     * @return deep copy
     */
    TransitionResultT clone();

    /**
     * Returns a view on this TransitionResult with a masked TransitionInformation. All interface
     * methods are supported by the result.
     * <p>
     * The result of any call to {@link #transitionInformation()} is replaced by invocing the given
     * maskings function. For any call to {@link #replaceTransitionInformation(TransitionInformation)},
     * the parameter is first replaced by invocing the reverse masking function.
     *
     * @see TwoInformations
     * @see TwoInformationsHandler
     *
     * @param <X> the type of the result
     * @param <OtherInfoT> some type of transition information
     * @param mask a function replacing the original type of transition information (InfoT) with the the
     *        new type (OtherInfoT)
     * @param reverseMask the reverse of the mask function
     * @return a view on this TransitionResult with masked information
     */
    @SuppressWarnings("unchecked")
    default <X extends TransitionResult<X, GlobalStateT, ProcessStateT, OtherInfoT, ProcessT>, OtherInfoT extends TransitionInformation<OtherInfoT>> X maskTransitionInformation(
            Function<InfoT, OtherInfoT> mask, Function<OtherInfoT, InfoT> reverseMask) {
        return (X) new TransitionResult<X, GlobalStateT, ProcessStateT, OtherInfoT, ProcessT>() {

            @Override
            public ConsideredState<GlobalStateT, ProcessStateT, ProcessT> resultingState() {
                return TransitionResult.this.resultingState();
            }

            @Override
            public OtherInfoT transitionInformation() {
                return mask.apply(TransitionResult.this.transitionInformation());
            }

            @Override
            public X replaceResultingState(ConsideredState<GlobalStateT, ProcessStateT, ProcessT> state) {
                return TransitionResult.this.replaceResultingState(state).maskTransitionInformation(mask, reverseMask);
            }

            @Override
            public X replaceTransitionInformation(OtherInfoT transitionInformation) {
                return TransitionResult.this.replaceTransitionInformation(reverseMask.apply(transitionInformation))
                        .maskTransitionInformation(mask, reverseMask);
            }

            @Override
            public X clone() {
                return TransitionResult.this.clone().maskTransitionInformation(mask, reverseMask);
            }
        };
    }

}
