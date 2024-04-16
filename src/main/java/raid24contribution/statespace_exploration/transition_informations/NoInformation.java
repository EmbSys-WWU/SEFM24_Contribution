package raid24contribution.statespace_exploration.transition_informations;

import java.util.Set;
import raid24contribution.sc_model.expressions.Expression;
import raid24contribution.statespace_exploration.AbstractedValue;
import raid24contribution.statespace_exploration.AnalyzedProcess;
import raid24contribution.statespace_exploration.EventBlocker;
import raid24contribution.statespace_exploration.LocalState;
import raid24contribution.statespace_exploration.ProcessState;
import raid24contribution.statespace_exploration.TransitionInformation;
import raid24contribution.statespace_exploration.TransitionResult;
import raid24contribution.statespace_exploration.standard_implementations.ComposableTransitionInformation;
import raid24contribution.statespace_exploration.standard_implementations.InformationHandler;

/**
 * A trivial implementation of {@link TransitionInformation} representing no information at all.
 *
 * @author Jonas Becker-Kupczok
 *
 */
public class NoInformation implements ComposableTransitionInformation<NoInformation> {

    public static final NoInformation INSTANCE = new NoInformation();

    @SuppressWarnings("rawtypes")
    private static final InformationHandler HANDLER_INSTANCE = new InformationHandler() {

        @Override
        public NoInformation getNeutralInformation() {
            return INSTANCE;
        }

        @Override
        public NoInformation handleExpressionEvaluation(Expression evaluated, int comingFrom,
                TransitionResult resultingState, LocalState localState) {
            return INSTANCE;
        }

        @Override
        public NoInformation handleProcessWaitedForDelta(AnalyzedProcess process,
                ProcessState resultingState, ComposableTransitionInformation currentInformation) {
            return INSTANCE;
        }

        @Override
        public NoInformation handleProcessWaitedForTime(AnalyzedProcess process,
                ProcessState resultingState, ComposableTransitionInformation currentInformation) {
            return INSTANCE;
        }

        @Override
        public NoInformation handleProcessWaitedForEvents(AnalyzedProcess process,
                ProcessState resultingState, Set events, EventBlocker blockerBefore,
                ComposableTransitionInformation currentInformation) {
            return INSTANCE;
        }

        @Override
        public NoInformation handleStartOfCode(TransitionResult currentState, LocalState localState) {
            return INSTANCE;
        }

    };

    @SuppressWarnings("unchecked")
    public static <ValueT extends AbstractedValue<ValueT, ?, ?>> InformationHandler<NoInformation, ValueT> getHandler() {
        return HANDLER_INSTANCE;
    }

    private NoInformation() {}

    @Override
    public NoInformation compose(NoInformation other) {
        return this;
    }

    @Override
    public NoInformation clone() {
        return this;
    }

    @Override
    public String toString() {
        return "";
    }

}
