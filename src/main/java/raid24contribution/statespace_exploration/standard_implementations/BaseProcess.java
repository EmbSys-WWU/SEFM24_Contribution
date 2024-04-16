package raid24contribution.statespace_exploration.standard_implementations;

import static raid24contribution.util.WrapperUtil.wrap;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SequencedMap;
import java.util.Set;
import raid24contribution.sc_model.SCPort;
import raid24contribution.sc_model.SCProcess;
import raid24contribution.sc_model.SCSystem;
import raid24contribution.sc_model.expressions.FunctionCallExpression;
import raid24contribution.sc_model.variables.SCClassInstance;
import raid24contribution.sc_model.variables.SCTIMEUNIT;
import raid24contribution.statespace_exploration.AbstractedValue;
import raid24contribution.statespace_exploration.AnalyzedProcess;
import raid24contribution.statespace_exploration.ConsideredState;
import raid24contribution.statespace_exploration.DeltaTimeBlocker;
import raid24contribution.statespace_exploration.EvaluationContext;
import raid24contribution.statespace_exploration.EventBlocker;
import raid24contribution.statespace_exploration.EventBlocker.Event;
import raid24contribution.statespace_exploration.GlobalState;
import raid24contribution.statespace_exploration.ProcessState;
import raid24contribution.statespace_exploration.ProcessTerminatedBlocker;
import raid24contribution.statespace_exploration.ProcessTransitionResult;
import raid24contribution.statespace_exploration.RealTimedBlocker;
import raid24contribution.statespace_exploration.Scheduler;
import raid24contribution.statespace_exploration.StateSpaceExploration;
import raid24contribution.statespace_exploration.StateSpaceExploration.ExplorationAbortedError;
import raid24contribution.statespace_exploration.TimedBlocker;
import raid24contribution.util.WrappedSCClassInstance;
import raid24contribution.util.WrappedSCPortInstance;
import raid24contribution.util.WrappedSCProcess;

/**
 * Class implementing a basic process semantic by traversing the expression tree and control flow
 * graph.
 * 
 * This class is immutable and thread-safe except for changes in the underlying SysCIR. Such changes
 * lead to undefined behavior.
 * 
 * @author Jonas Becker-Kupczok
 *
 * @param <GlobalStateT> the type of global state abstraction which this process implementation can
 *        handle
 * @param <ProcessStateT> the type of local state abstraction which this process implementation can
 *        handle
 * @param <InfoT> the type of additional transition information which this process implementation
 *        can provide.
 * @param <ValueT> the type of abstracted value which this process implementation can handle
 */
public abstract class BaseProcess<ProcessT extends BaseProcess<ProcessT, GlobalStateT, ProcessStateT, ValueT, InfoT>, GlobalStateT extends GlobalState<GlobalStateT>, ProcessStateT extends ProcessState<ProcessStateT, ValueT>, ValueT extends AbstractedValue<ValueT, ?, ?>, InfoT extends ComposableTransitionInformation<InfoT>>
        extends
        ExpressionCrawler<GlobalStateT, ProcessStateT, ProcessStateT, ProcessTransitionResult<GlobalStateT, ProcessStateT, InfoT, ProcessT>, ValueT, InfoT, ProcessT>
        implements AnalyzedProcess<ProcessT, GlobalStateT, ProcessStateT, InfoT> {
    
    private final WrappedSCProcess scProcess;
    private final WrappedSCClassInstance scClassInstance;
    private final int hashCode;
    
    /**
     * Constructs a new BaseProcess representing the given SysCIR process, belonging to the given SysCIR
     * class instance (i.e. the module instance) and using the given scheduler.
     *
     * @param scSystem the SysCIR representation of the entire SystemC design
     * @param scProcess the SysCIR process
     * @param scClassInstance owning instance of this process
     * @param scheduler the scheduler used in this analysis
     */
    public BaseProcess(SCSystem scSystem, SCProcess scProcess, SCClassInstance scClassInstance,
            Scheduler<GlobalStateT, ProcessStateT, InfoT, ProcessT> scheduler,
            InformationHandler<InfoT, ValueT> informationHandler) {
        super(scSystem, scheduler, informationHandler);
        
        this.scProcess = wrap(scProcess);
        this.scClassInstance = wrap(scClassInstance);
        this.hashCode = this.scProcess.hashCode();
    }
    
    @Override
    public WrappedSCProcess getSCProcess() {
        return this.scProcess;
    }
    
    @Override
    public WrappedSCClassInstance getSCClassInstance() {
        return this.scClassInstance;
    }
    
    @Override
    public int hashCode() {
        return this.hashCode;
    }
    
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof AnalyzedProcess<?, ?, ?, ?> p)) {
            return false;
        }
        return getSCProcess().equals(p.getSCProcess()) && getSCClassInstance().equals(p.getSCClassInstance());
    }
    
    @Override
    public String toString() {
        return getName();
    }
    
    /**
     * {@inheritDoc}
     * <p>
     * This implementation repeatedly calls {@link #makeSmallStep(ProcessTransitionResult)} until
     * {@link SmallStepResult#endOfStep()} is true for every branch of the evaluation, returning the
     * gathered transition results. For efficiency, states are only locked in the end and reused in
     * between where possible.
     * 
     * @param currentState {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public Set<ProcessTransitionResult<GlobalStateT, ProcessStateT, InfoT, ProcessT>> makeStep(
            ConsideredState<GlobalStateT, ProcessStateT, ProcessT> currentState) {
        StateSpaceExploration<?, ?, ?, ?> explorer = StateSpaceExploration.getCurrentExplorer();
        
        /*
         * This uses a modified worklist (transitionsToHandle) algorithm to make one small step after the
         * other until the end of a (large) step is reached. Elements of the worklist are stored as map
         * entries, so that transitions that yield the same state but with different transition information
         * are detected. In such cases, the information is composed and the entry is put back into the
         * worklist to ensure that no transition information is lost.
         * 
         * Some transitions which may repeat themselves (e.g., loop conditions) are stored to detect if they
         * reappear and not put them into the worklist again.
         */
        
        Map<ConsideredState<GlobalStateT, ProcessStateT, ProcessT>, InfoT> seenTransitions = new LinkedHashMap<>();
        SequencedMap<ConsideredState<GlobalStateT, ProcessStateT, ProcessT>, InfoT> transitionsToHandle =
                new LinkedHashMap<>();
        Map<ConsideredState<GlobalStateT, ProcessStateT, ProcessT>, InfoT> resultingTransitions = new LinkedHashMap<>();
        
        ProcessTransitionResult<GlobalStateT, ProcessStateT, InfoT, ProcessT> initialTransitionResult =
                new ProcessTransitionResult<>(currentState, getNeutralInformation());
        InfoT initialInformation = getInformationHandler().handleStartOfCode(initialTransitionResult,
                getLocalState(initialTransitionResult));
        transitionsToHandle.put(currentState.unlockedVersion(), initialInformation);
        
        while (!transitionsToHandle.isEmpty()) {
            if (explorer.isAborted()) {
                throw new ExplorationAbortedError();
            }
            
            ProcessTransitionResult<GlobalStateT, ProcessStateT, InfoT, ProcessT> nextToHandle =
                    new ProcessTransitionResult<>(transitionsToHandle.firstEntry().getKey(),
                            transitionsToHandle.pollFirstEntry().getValue());
            SmallStepResult<GlobalStateT, ProcessStateT, InfoT, ProcessTransitionResult<GlobalStateT, ProcessStateT, InfoT, ProcessT>> nextTransitions =
                    makeSmallStep(nextToHandle);
            if (nextTransitions.endOfStep()) {
                // store resulting transitions
                for (ProcessTransitionResult<GlobalStateT, ProcessStateT, InfoT, ProcessT> transition : nextTransitions
                        .transitions()) {
                    resultingTransitions.merge(transition.resultingState(), transition.transitionInformation(),
                            InfoT::compose);
                }
            } else if (nextTransitions.possiblyRepeatingStep()) {
                // make sure that possibly repeating steps are only considered once
                for (ProcessTransitionResult<GlobalStateT, ProcessStateT, InfoT, ProcessT> transition : nextTransitions
                        .transitions()) {
                    transition.resultingState().lock();
                    InfoT oldInformation = seenTransitions.get(transition.resultingState());
                    if (transition.transitionInformation().equals(oldInformation)) {
                        continue;
                    }
                    InfoT newInformation = seenTransitions.merge(transition.resultingState(),
                            transition.transitionInformation(), InfoT::compose);
                    if (newInformation.equals(oldInformation)) {
                        continue;
                    }
                    
                    transitionsToHandle.merge(transition.resultingState().unlockedClone(), newInformation.clone(),
                            InfoT::compose);
                }
            } else {
                for (ProcessTransitionResult<GlobalStateT, ProcessStateT, InfoT, ProcessT> transition : nextTransitions
                        .transitions()) {
                    transitionsToHandle.merge(transition.resultingState().unlockedVersion(),
                            transition.transitionInformation(), InfoT::compose);
                }
            }
        }
        
        // finalize the result
        
        Set<ProcessTransitionResult<GlobalStateT, ProcessStateT, InfoT, ProcessT>> result =
                new LinkedHashSet<>(resultingTransitions.size());
        for (Entry<ConsideredState<GlobalStateT, ProcessStateT, ProcessT>, InfoT> transition : resultingTransitions
                .entrySet()) {
            result.add(finalizeTransitionResult(
                    new ProcessTransitionResult<>(transition.getKey(), transition.getValue())));
        }
        
        return result;
    }
    
    @Override
    public ProcessStateT getLocalState(
            ProcessTransitionResult<GlobalStateT, ProcessStateT, InfoT, ProcessT> currentState) {
        return currentState.resultingState().getProcessState(this);
    }
    
    /**
     * Returns the set of events that the process is statically sensitive on in the current state.
     *
     * @param currentState the current state
     * @param localState the local state of this process in the current state
     * @return the set of events the process is statically sensitive on
     */
    public abstract Set<Event> getSensitivities(
            ProcessTransitionResult<GlobalStateT, ProcessStateT, InfoT, ProcessT> currentState,
            ProcessStateT localState);
    
    @Override
    public SmallStepResult<GlobalStateT, ProcessStateT, InfoT, ProcessTransitionResult<GlobalStateT, ProcessStateT, InfoT, ProcessT>> handleWaitExpression(
            ProcessTransitionResult<GlobalStateT, ProcessStateT, InfoT, ProcessT> currentState,
            ProcessStateT localState, FunctionCallExpression expression, int comingFrom) {
        if (expression.getParameters().isEmpty()) {
            returnToParent(expression.getParent(), localState);
            localState.setWaitingFor(
                    new EventBlocker(new LinkedHashSet<>(getSensitivities(currentState, localState)), true, null));
            return createSmallStepResult(expression, comingFrom, currentState, localState, true, false);
        }
        
        AbstractedValue<ValueT, ?, ?> firstParam = getValueOfChild(currentState, localState, 0);
        if (!firstParam.isDetermined()) {
            throw new InsufficientPrecisionException(expression.toString());
        }
        Object firstValue = firstParam.get();
        
        if (expression.getParameters().size() == 1) {
            if (firstValue instanceof EventBlocker eb) {
                localState.setWaitingFor(eb);
            } else if (firstValue instanceof Event event) {
                localState.setWaitingFor(new EventBlocker(Set.of(event), true, null));
            } else if (firstValue instanceof TimedBlocker tb) {
                localState.setWaitingFor(tb);
            } else if (firstValue == SCTIMEUNIT.SC_ZERO_TIME) {
                localState.setWaitingFor(DeltaTimeBlocker.INSTANCE);
            } else {
                throw new RuntimeException("unexpected value of wait parameter: " + firstValue.getClass());
            }
            returnToParent(expression.getParent(), localState);
            return createSmallStepResult(expression, comingFrom, currentState, localState, true, false);
        }
        
        AbstractedValue<ValueT, ?, ?> secondParam = getValueOfChild(currentState, localState, 1);
        if (!secondParam.isDetermined()) {
            throw new InsufficientPrecisionException();
        }
        Object secondValue = secondParam.get();
        
        if (expression.getParameters().size() == 2) {
            if (secondValue instanceof SCTIMEUNIT unit) {
                int amount = getAsInteger(firstValue);
                localState.setWaitingFor(amount == 0 ? DeltaTimeBlocker.INSTANCE : new RealTimedBlocker(amount, unit));
            } else if (secondValue instanceof Event event) {
                TimedBlocker timeout = (TimedBlocker) firstValue;
                localState.setWaitingFor(new EventBlocker(Set.of(event), true, timeout));
            } else if (secondValue instanceof EventBlocker eb) {
                TimedBlocker timeout = (TimedBlocker) firstValue;
                localState.setWaitingFor(eb.replaceTimeout(timeout));
            } else {
                throw new RuntimeException("unexpected value of wait parameter: " + secondValue.getClass());
            }
            returnToParent(expression.getParent(), localState);
            return createSmallStepResult(expression, comingFrom, currentState, localState, true, false);
        }
        
        AbstractedValue<ValueT, ?, ?> thirdParam = getValueOfChild(currentState, localState, 2);
        if (!thirdParam.isDetermined()) {
            throw new InsufficientPrecisionException();
        }
        Object thirdValue = thirdParam.get();
        
        if (expression.getParameters().size() != 3) {
            throw new RuntimeException("unexpected call to wait with more than 3 parameters");
        }
        
        int amount = getAsInteger(firstValue);
        SCTIMEUNIT unit = (SCTIMEUNIT) secondValue;
        TimedBlocker timeout = amount == 0 ? DeltaTimeBlocker.INSTANCE : new RealTimedBlocker(amount, unit);
        
        if (thirdValue instanceof Event event) {
            localState.setWaitingFor(new EventBlocker(Set.of(event), true, timeout));
        } else if (thirdValue instanceof EventBlocker eb) {
            localState.setWaitingFor(eb.replaceTimeout(timeout));
        } else {
            throw new RuntimeException("unexpected value of wait parameter: " + thirdValue.getClass());
        }
        returnToParent(expression.getParent(), localState);
        return createSmallStepResult(expression, comingFrom, currentState, localState, true, false);
    }
    
    @Override
    public SmallStepResult<GlobalStateT, ProcessStateT, InfoT, ProcessTransitionResult<GlobalStateT, ProcessStateT, InfoT, ProcessT>> handleRequestUpdateExpression(
            ProcessTransitionResult<GlobalStateT, ProcessStateT, InfoT, ProcessT> currentState,
            ProcessStateT localState, FunctionCallExpression expression, int comingFrom) {
        ValueT portValue = localState.getTopOfStack().getThisValue();
        if (!portValue.isDetermined()) {
            throw new InsufficientPrecisionException();
        }
        
        WrappedSCClassInstance instanceToUpdate = null;
        if (portValue.get() instanceof WrappedSCClassInstance kt) {
            instanceToUpdate = kt;
        } else if (portValue.get() instanceof WrappedSCPortInstance pi) {
            instanceToUpdate = getChannel(pi);
        } else if (portValue.get() instanceof SCPort port) {
            instanceToUpdate = getChannel(port);
        } else {
            throw new ClassCastException();
        }
        
        currentState.globalState().getRequestedUpdates().add(instanceToUpdate);
        returnToParent(expression.getParent(), localState);
        return createSmallStepResult(expression, comingFrom, currentState, localState, false, false);
    }
    
    @Override
    public SmallStepResult<GlobalStateT, ProcessStateT, InfoT, ProcessTransitionResult<GlobalStateT, ProcessStateT, InfoT, ProcessT>> handleEndOfCodeReached(
            ProcessTransitionResult<GlobalStateT, ProcessStateT, InfoT, ProcessT> currentState,
            ProcessStateT localState, List<EvaluationContext<ValueT>> stack) {
        Set<Event> sensitivities = getSensitivities(currentState, localState);
        if (sensitivities.isEmpty()) {
            localState.setWaitingFor(ProcessTerminatedBlocker.INSTANCE);
        } else {
            localState.setWaitingFor(new EventBlocker(new LinkedHashSet<>(sensitivities), true, null));
            List<List<ValueT>> executionValues = new ArrayList<>();
            executionValues.add(new ArrayList<>());
            stack.add(new EvaluationContext<>(getSCProcess().getFunction(), new ArrayList<>(), -1, executionValues,
                    getDeterminedValue(getSCClassInstance())));
        }
        return createSmallStepResult(null, -2, currentState, localState, true, false);
    }
    
}
