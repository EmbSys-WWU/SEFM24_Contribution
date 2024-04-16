package de.tub.pes.syscir.analysis.statespace_exploration.transition_informations;

import de.tub.pes.syscir.analysis.statespace_exploration.AbstractedValue;
import de.tub.pes.syscir.analysis.statespace_exploration.AnalyzedProcess;
import de.tub.pes.syscir.analysis.statespace_exploration.EventBlocker;
import de.tub.pes.syscir.analysis.statespace_exploration.LocalState;
import de.tub.pes.syscir.analysis.statespace_exploration.ProcessState;
import de.tub.pes.syscir.analysis.statespace_exploration.TransitionInformation;
import de.tub.pes.syscir.analysis.statespace_exploration.TransitionResult;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.ComposableTransitionInformation;
import de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations.InformationHandler;
import de.tub.pes.syscir.sc_model.expressions.Expression;
import java.util.Set;

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
