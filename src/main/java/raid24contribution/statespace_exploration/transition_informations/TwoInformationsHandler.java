package raid24contribution.statespace_exploration.transition_informations;

import java.util.Set;
import java.util.function.Function;
import raid24contribution.sc_model.expressions.Expression;
import raid24contribution.statespace_exploration.AbstractedValue;
import raid24contribution.statespace_exploration.AnalyzedProcess;
import raid24contribution.statespace_exploration.EventBlocker;
import raid24contribution.statespace_exploration.LocalState;
import raid24contribution.statespace_exploration.ProcessState;
import raid24contribution.statespace_exploration.TransitionResult;
import raid24contribution.statespace_exploration.EventBlocker.Event;
import raid24contribution.statespace_exploration.standard_implementations.ComposableTransitionInformation;
import raid24contribution.statespace_exploration.standard_implementations.InformationHandler;

public record TwoInformationsHandler<FirstInfoT extends ComposableTransitionInformation<FirstInfoT>, SecondInfoT extends ComposableTransitionInformation<SecondInfoT>, ValueT extends AbstractedValue<ValueT, ?, ?>>(
        InformationHandler<FirstInfoT, ValueT> firstHandler, InformationHandler<SecondInfoT, ValueT> secondHandler)
implements InformationHandler<TwoInformations<FirstInfoT, SecondInfoT>, ValueT> {

    private Function<TwoInformations<FirstInfoT, SecondInfoT>, FirstInfoT> firstMask() {
        return TwoInformations::first;
    }

    private Function<FirstInfoT, TwoInformations<FirstInfoT, SecondInfoT>> firstReverseMask(
            TransitionResult<?, ?, ?, TwoInformations<FirstInfoT, SecondInfoT>, ?> transitionResult) {
        return transitionResult.transitionInformation()::setFirst;
    }

    private Function<TwoInformations<FirstInfoT, SecondInfoT>, SecondInfoT> secondMask() {
        return TwoInformations::second;
    }

    private Function<SecondInfoT, TwoInformations<FirstInfoT, SecondInfoT>> secondReverseMask(
            TransitionResult<?, ?, ?, TwoInformations<FirstInfoT, SecondInfoT>, ?> transitionResult) {
        return transitionResult.transitionInformation()::setSecond;
    }

    private TransitionResult<?, ?, ?, FirstInfoT, ?> maskForFirst(
            TransitionResult<?, ?, ?, TwoInformations<FirstInfoT, SecondInfoT>, ?> transitionResult) {
        return transitionResult.maskTransitionInformation(firstMask(), firstReverseMask(transitionResult));
    }

    private TransitionResult<?, ?, ?, SecondInfoT, ?> maskForSecond(
            TransitionResult<?, ?, ?, TwoInformations<FirstInfoT, SecondInfoT>, ?> transitionResult) {
        return transitionResult.maskTransitionInformation(secondMask(), secondReverseMask(transitionResult));
    }

    @Override
    public TwoInformations<FirstInfoT, SecondInfoT> getNeutralInformation() {
        return new TwoInformations<FirstInfoT, SecondInfoT>(this.firstHandler.getNeutralInformation(),
                this.secondHandler.getNeutralInformation());
    }

    @Override
    public <LocalStateT extends LocalState<LocalStateT, ValueT>> TwoInformations<FirstInfoT, SecondInfoT> handleStartOfCode(
            TransitionResult<?, ?, ?, TwoInformations<FirstInfoT, SecondInfoT>, ?> currentState,
            LocalStateT localState) {
        return new TwoInformations<>(this.firstHandler.handleStartOfCode(maskForFirst(currentState), localState),
                this.secondHandler.handleStartOfCode(maskForSecond(currentState), localState));
    }

    @Override
    public <LocalStateT extends LocalState<LocalStateT, ValueT>> void announceEvaluation(Expression expression,
            TransitionResult<?, ?, ?, TwoInformations<FirstInfoT, SecondInfoT>, ?> currentState,
            LocalStateT localState) {
        this.firstHandler.announceEvaluation(expression, maskForFirst(currentState), localState);
        this.secondHandler.announceEvaluation(expression, maskForSecond(currentState), localState);
    }

    @Override
    public <LocalStateT extends LocalState<LocalStateT, ValueT>> TwoInformations<FirstInfoT, SecondInfoT> handleExpressionEvaluation(
            Expression evaluated, int comingFrom,
            TransitionResult<?, ?, ?, TwoInformations<FirstInfoT, SecondInfoT>, ?> resultingState, LocalStateT localState) {
        return new TwoInformations<>(
                this.firstHandler.handleExpressionEvaluation(evaluated, comingFrom, maskForFirst(resultingState), localState),
                this.secondHandler.handleExpressionEvaluation(evaluated, comingFrom, maskForSecond(resultingState), localState));
    }

    @Override
    public TwoInformations<FirstInfoT, SecondInfoT> finalizeInformation(
            TwoInformations<FirstInfoT, SecondInfoT> information) {
        return new TwoInformations<>(
                this.firstHandler.finalizeInformation(information.first()),
                this.secondHandler.finalizeInformation(information.second()));
    }

    @Override
    public TwoInformations<FirstInfoT, SecondInfoT> handleProcessWaitedForDelta(AnalyzedProcess<?, ?, ?, ?> process,
            ProcessState<?, ValueT> resultingState, TwoInformations<FirstInfoT, SecondInfoT> currentInformation) {
        return new TwoInformations<>(
                this.firstHandler.finalizeInformation(this.firstHandler.handleProcessWaitedForDelta(process,
                        resultingState, currentInformation.first())),
                this.secondHandler.finalizeInformation(this.secondHandler.handleProcessWaitedForDelta(process,
                        resultingState, currentInformation.second())));
    }

    @Override
    public TwoInformations<FirstInfoT, SecondInfoT> handleProcessWaitedForTime(AnalyzedProcess<?, ?, ?, ?> process,
            ProcessState<?, ValueT> resultingState, TwoInformations<FirstInfoT, SecondInfoT> currentInformation) {
        return new TwoInformations<>(
                this.firstHandler.finalizeInformation(this.firstHandler.handleProcessWaitedForTime(process,
                        resultingState, currentInformation.first())),
                this.secondHandler.finalizeInformation(this.secondHandler.handleProcessWaitedForTime(process,
                        resultingState, currentInformation.second())));
    }

    @Override
    public TwoInformations<FirstInfoT, SecondInfoT> handleProcessWaitedForEvents(AnalyzedProcess<?, ?, ?, ?> process,
            ProcessState<?, ValueT> resultingState, Set<Event> events, EventBlocker effectedBlocker,
            TwoInformations<FirstInfoT, SecondInfoT> currentInformation) {
        return new TwoInformations<>(
                this.firstHandler.finalizeInformation(this.firstHandler.handleProcessWaitedForEvents(process,
                        resultingState, events, effectedBlocker, currentInformation.first())),
                this.secondHandler.finalizeInformation(this.secondHandler.handleProcessWaitedForEvents(process,
                        resultingState, events, effectedBlocker, currentInformation.second())));
    }

}
