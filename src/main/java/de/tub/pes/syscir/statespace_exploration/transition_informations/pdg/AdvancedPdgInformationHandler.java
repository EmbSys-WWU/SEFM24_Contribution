package de.tub.pes.syscir.statespace_exploration.transition_informations.pdg;

import static de.tub.pes.syscir.util.CollectionUtil.nullSet;
import static de.tub.pes.syscir.util.WrapperUtil.wrap;

import de.tub.pes.syscir.dependencies.DgEdge.EdgeType;
import de.tub.pes.syscir.dependencies.DgNode.NodeType;
import de.tub.pes.syscir.statespace_exploration.AbstractedValue;
import de.tub.pes.syscir.statespace_exploration.AnalyzedProcess;
import de.tub.pes.syscir.statespace_exploration.EvaluationContext;
import de.tub.pes.syscir.statespace_exploration.EvaluationLocation;
import de.tub.pes.syscir.statespace_exploration.EventBlocker;
import de.tub.pes.syscir.statespace_exploration.EventBlocker.Event;
import de.tub.pes.syscir.statespace_exploration.LocalState;
import de.tub.pes.syscir.statespace_exploration.ProcessState;
import de.tub.pes.syscir.statespace_exploration.TransitionResult;
import de.tub.pes.syscir.statespace_exploration.standard_implementations.ExpressionCrawler.InsufficientPrecisionException;
import de.tub.pes.syscir.statespace_exploration.standard_implementations.GlobalVariable;
import de.tub.pes.syscir.statespace_exploration.standard_implementations.Variable;
import de.tub.pes.syscir.statespace_exploration.transition_informations.pdg.PdgNode.PdgNodeId;
import de.tub.pes.syscir.statespace_exploration.transition_informations.pdg.PdgNode.StatementId;
import de.tub.pes.syscir.util.WrappedSCClassInstance;
import de.tub.pes.syscir.util.WrappedSCFunction;
import de.tub.pes.syscir.sc_model.expressions.EventNotificationExpression;
import de.tub.pes.syscir.sc_model.expressions.Expression;
import de.tub.pes.syscir.sc_model.expressions.FunctionCallExpression;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Information handler providing an advanced program dependence graph (PDG) which, in addition to
 * the "simple" PDG captured by {@link PdgInformationHandler}, also captures information about the
 * triggering of code outside the current transition.
 * 
 * These triggers are treated as variables (see
 * {@link GlobalVariable#blockTrigger(WrappedSCClassInstance, List)} and
 * {@link GlobalVariable#eventTrigger(Event)}) with in/out nodes where appropriate. Entry nodes are
 * considered to be reading their respective block trigger, while wait, notify and request_update
 * nodes write their respective block or event trigger. When an event is actually notified (i.e.,
 * after the time specified in the notification has elapsed), the event trigger is read and the
 * block trigger is written.
 * <p>
 * When combining the PDGs for several transitions into an SDG, it must be taken into account that
 * trigger variables should be treated differently to normal variables: writing a trigger variable
 * doesn't (necessarily) kill its other reaching definitions, whereas reading it does.
 * 
 * @author Jonas Becker-Kupczok
 *
 * @param <ValueT> the type of abstracted value used in the exploration
 */
public class AdvancedPdgInformationHandler<ValueT extends AbstractedValue<ValueT, ?, ?>>
extends PdgInformationHandler<ValueT> {

    private ThreadLocal<Event> announcedEvent;

    public AdvancedPdgInformationHandler() {
        super();

        this.announcedEvent = new ThreadLocal<>();
    }

    @Override
    public <LocalStateT extends LocalState<LocalStateT, ValueT>> PdgInformation handleStartOfCode(
            TransitionResult<?, ?, ?, PdgInformation, ?> currentState, LocalStateT localState) {
        PdgInformation currentInformation = super.handleStartOfCode(currentState, localState);

        PdgNode entryNode = currentInformation.getCurrentEntryNode();
        GlobalVariable<WrappedSCClassInstance, List<EvaluationLocation>> triggerVar = GlobalVariable.blockTrigger(
                localState.getInitialThisValue(), ((StatementId) entryNode.getId().identifier()).callStack());
        // cast is not that nice, but cheaper then new construction. hm.
        PdgNode triggerInNode =
                currentInformation.getNodes().computeIfAbsent(new PdgNodeId(NodeType.IN, triggerVar), PdgNode::new);
        new PdgEdge(EdgeType.CONTROL, triggerInNode, entryNode, true);

        return currentInformation;
    }

    @Override
    public <LocalStateT extends LocalState<LocalStateT, ValueT>> void announceEvaluation(Expression expression,
            TransitionResult<?, ?, ?, PdgInformation, ?> currentState,
            LocalStateT localState) {
        super.announceEvaluation(expression, currentState, localState);

        if (expression instanceof EventNotificationExpression ee
                && localState.getTopOfStack().getComingFrom() == expression.getNumOfChildren() - 1) {
            // the notified event will no longer be stored in the local state after the evaluation has finished.
            // store it to avoid recomputing what the expression crawler already computed

            ValueT eventValue = localState.getTopOfStack().getExpressionValue(0, 0);
            if (!eventValue.isDetermined()) {
                throw new InsufficientPrecisionException();
            }
            this.announcedEvent.set((Event) eventValue.get());
        }
    }

    @Override
    public PdgInformation handleProcessWaitedForEvents(AnalyzedProcess<?, ?, ?, ?> process,
            ProcessState<?, ValueT> resultingState, Set<Event> notifiedEvents, EventBlocker effectedBlocker,
            PdgInformation currentInfo) {
        // ignore irrelevant events
        notifiedEvents = new LinkedHashSet<>(notifiedEvents);
        notifiedEvents.retainAll(effectedBlocker.getEvents());

        currentInfo = currentInfo.unlockedVersion();
        PdgNode currentNode;

        /*
         * note that for immediate notifications, this method is called _before_ handleNotify(...). so,
         * immediate and non-immediate notifications can be distinguished by whether or not an announced
         * event is present. for immediate notifications, we skip the event trigger variable all together,
         * directly using the appropriate block triggers.
         */

        Event announcedEvent = this.announcedEvent.get();
        if (announcedEvent != null) {
            // immediate notification
            assert notifiedEvents.equals(Set.of(announcedEvent));
            currentNode = currentInfo.getNodes().computeIfAbsent(new PdgNodeId(NodeType.STATEMENT, getAnnouncedLocation()),
                    PdgNode::new);
        } else {
            // non-immediate notification
            currentNode =
                    currentInfo.getNodes().computeIfAbsent(new PdgNodeId(NodeType.STATEMENT, process), PdgNode::new);

            // read event triggers
            for (Event event : notifiedEvents) {
                GlobalVariable<Event, ?> triggerVar = GlobalVariable.eventTrigger(event);
                Set<PdgNode> triggers = currentInfo.getReachingDefs().getOrDefault(triggerVar, nullSet());
                for (PdgNode trigger : triggers) {
                    if (trigger == null) {
                        trigger = currentInfo.getNodes().computeIfAbsent(new PdgNodeId(NodeType.IN, triggerVar),
                                PdgNode::new);
                    }
                    PdgEdge edge = new PdgEdge(EdgeType.DATA, trigger, currentNode, false);
                    edge.insert();
                }
            }
        }

        // write block trigger
        List<EvaluationLocation> resumptionLocation = resultingState.getExecutionStack().stream()
                .map(EvaluationContext::toLocation)
                .collect(Collectors.toCollection(ArrayList::new));
        List<Integer> resumptionIndices = resumptionLocation.getLast().getExpressionIndices();
        resumptionIndices.add(resultingState.getTopOfStack().getComingFrom() + 1);
        GlobalVariable<WrappedSCClassInstance, List<EvaluationLocation>> blockTrigger =
                GlobalVariable.blockTrigger(resultingState.getInitialThisValue(), resumptionLocation);
        currentInfo.getReachingDefs().computeIfAbsent(blockTrigger, v -> new LinkedHashSet<>()).add(currentNode);

        return currentInfo;
    }

    @Override
    protected <LocalStateT extends LocalState<LocalStateT, ValueT>> PdgNode handleNotify(PdgInformation currentInfo,
            PdgNode currentNode, Set<Variable<?, ?>> variablesRead, Set<Variable<?, ?>> variablesWritten,
            EventNotificationExpression ee, int comingFrom, TransitionResult<?, ?, ?, PdgInformation, ?> resultingState,
            LocalStateT localState,
            List<EvaluationLocation> currentLocation) {
        if (ee.getParameters().isEmpty()) {
            // immediate notification, already handled by handleProcessWaitedForEvents(...). just reset
            // announcedEvent so that the distinction there remains functional
            this.announcedEvent.set(null);
            return currentNode;
        }

        if (currentNode == null) {
            currentNode = currentInfo.getNodes().computeIfAbsent(
                    new PdgNodeId(NodeType.STATEMENT, new StatementId(localState.getInitialThisValue(), currentLocation)),
                    PdgNode::new);
        }

        // write event trigger
        GlobalVariable<Event, ?> eventTrigger = GlobalVariable.eventTrigger(this.announcedEvent.get());
        variablesWritten.add(eventTrigger);

        // reset announcedEvent so that the distinction in handleProcessWaitedForEvents(...) remains
        // functional
        this.announcedEvent.set(null);
        return currentNode;
    }

    @Override
    protected <LocalStateT extends LocalState<LocalStateT, ValueT>> PdgNode handleWait(PdgInformation currentInfo,
            PdgNode currentNode, Set<Variable<?, ?>> variablesRead, Set<Variable<?, ?>> variablesWritten,
            FunctionCallExpression fe, int comingFrom, TransitionResult<?, ?, ?, PdgInformation, ?> resultingState,
            LocalStateT localState,
            List<EvaluationLocation> currentLocation) {
        if (currentNode == null) {
            currentNode = currentInfo.getNodes().computeIfAbsent(
                    new PdgNodeId(NodeType.STATEMENT, new StatementId(localState.getInitialThisValue(), currentLocation)),
                    PdgNode::new);
        }

        // write block trigger for following block
        List<EvaluationLocation> resumptionLocation = currentLocation.stream().map(EvaluationLocation::unlockedClone).collect(Collectors.toCollection(ArrayList::new));
        List<Integer> resumptionIndices = resumptionLocation.getLast().getExpressionIndices();
        resumptionIndices.add(resumptionIndices.removeLast() + 1);
        GlobalVariable<WrappedSCClassInstance,List<EvaluationLocation>> blockTrigger = GlobalVariable.blockTrigger(localState.getInitialThisValue(), resumptionLocation);
        variablesWritten.add(blockTrigger);

        return currentNode;
    }

    @Override
    protected <LocalStateT extends LocalState<LocalStateT, ValueT>> PdgNode handleRequestUpdate(
            PdgInformation currentInfo, PdgNode currentNode, Set<Variable<?, ?>> variablesRead,
            Set<Variable<?, ?>> variablesWritten, FunctionCallExpression fe, int comingFrom,
            TransitionResult<?, ?, ?, PdgInformation, ?> resultingState, LocalStateT localState,
            List<EvaluationLocation> currentLocation) {
        if (currentNode == null) {
            currentNode = currentInfo.getNodes().computeIfAbsent(
                    new PdgNodeId(NodeType.STATEMENT, new StatementId(localState.getInitialThisValue(), currentLocation)),
                    PdgNode::new);
        }

        // find the instance of the channel requesting the update
        ValueT abstractValueOfThis = localState.getTopOfStack().getThisValue();
        if (!abstractValueOfThis.isDetermined()) {
            throw new InsufficientPrecisionException();
        }
        Object valueOfThis = abstractValueOfThis.get();
        if (!(valueOfThis instanceof WrappedSCClassInstance instance)) {
            throw new ClassCastException(
                    "expected " + WrappedSCClassInstance.class + " but found " + valueOfThis.getClass());
        }

        // write block trigger for update block
        WrappedSCFunction updateFunction = wrap(instance.getSCClass().getMemberFunctionByName("update"));
        List<EvaluationLocation> resumptionLocation = List.of(new EvaluationLocation(updateFunction, new ArrayList<>()));
        GlobalVariable<WrappedSCClassInstance, List<EvaluationLocation>> blockTrigger =
                GlobalVariable.blockTrigger(instance, resumptionLocation);
        variablesWritten.add(blockTrigger);

        return currentNode;
    }

}
