package raid24contribution.statespace_exploration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import raid24contribution.sc_model.SCClass;
import raid24contribution.sc_model.SCPROCESSTYPE;
import raid24contribution.sc_model.SCProcess;
import raid24contribution.sc_model.SCSystem;
import raid24contribution.sc_model.variables.SCClassInstance;
import raid24contribution.statespace_exploration.EventBlocker.Event;
import raid24contribution.util.TriFunction;
import raid24contribution.util.WrappedSCClassInstance;
import raid24contribution.util.WrappedSCFunction;

/**
 * Class representing an abstraction of the state of a SystemC design.
 * 
 * The state is split into a global state (including the scheduling states of all events) and one
 * local state per process (including the scheduling state of that process itself, i.e. whether it
 * is waiting for something and, if so, for what).
 * <p>
 * This state starts out as mutable but can be locked to be immutable by invoking the method
 * {@link #lock()}. When locked, any attempt to modify a state results in an
 * {@link IllegalStateException}.
 * <p>
 * The cached hashCode of a ConsideredState is reset whenever it is modified or any modifiable
 * reference to a part of its state is obtained. If any so obtained reference is mofified after the
 * hashCode has been computed, it must be assured that the hashCode is reset again.
 * 
 * @author Jonas Becker-Kupczok
 *
 * @param <GlobalStateT> the type of the global state
 * @param <ProcessStateT> the type of the state of one process
 * @param <T> the type of additional transition information that a {@link AnalyzedProcess}
 *        considered in this state may provide
 */
public class ConsideredState<GlobalStateT extends GlobalState<GlobalStateT>, ProcessStateT extends ProcessState<ProcessStateT, ?>, ProcessT extends AnalyzedProcess<ProcessT, GlobalStateT, ProcessStateT, ?>>
extends HashCachingLockableObject<ConsideredState<GlobalStateT, ProcessStateT, ProcessT>> {

    /**
     * Creates the initial state of a SystemC model.
     * 
     * @param <G>the type of the global state
     * @param <ProcessStateT> the type of the state of one process
     * @param <InfoT> the type of additional transition information that a {@link AnalyzedProcess}
     *        considered in this state may provide
     * @param <ValueT> the type of abstracted values used for this state
     * @param system a SystemC model
     * @param globalStateConstructor a function constructing a ConsideredGlobalState from a map of
     *        pending events and a boolean for whether simulation was stopped
     * @param processConstructor a function constructing a Process from a given SCProcess
     * @param processStateConstructor a function constructing a ConsideredProcessState from a
     *        ProcessBlocker for which the process is waiting, a list for the execution stack and a list
     *        for the expression values
     * @return the initial state for that SystemC model
     */
    // TODO: THIS IS UGLY!
    public static <ProcessT extends AnalyzedProcess<ProcessT, GlobalStateT, ProcessStateT, InfoT>, GlobalStateT extends GlobalState<GlobalStateT>, ProcessStateT extends ProcessState<ProcessStateT, ValueT>, InfoT extends TransitionInformation<InfoT>, ValueT extends AbstractedValue<ValueT, ?, ?>> ConsideredState<GlobalStateT, ProcessStateT, ProcessT> getInitialState(
            SCSystem system,
            TriFunction<Map<Event, TimedBlocker>, Set<WrappedSCClassInstance>, Boolean, ? extends GlobalStateT> globalStateConstructor,
            TriFunction<SCSystem, SCProcess, SCClassInstance, ProcessT> processConstructor,
            BiFunction<ProcessBlocker, List<EvaluationContext<ValueT>>, ? extends ProcessStateT> processStateConstructor,
            BiFunction<ProcessT, GlobalStateT, Set<Event>> initialSensitivitiesGetter,
            Function<Object, ? extends ValueT> determinedValueConstructor) {

        GlobalStateT globalState = globalStateConstructor.apply(new LinkedHashMap<>(), new LinkedHashSet<>(), false);

        Map<ProcessT, ProcessStateT> processStates = new LinkedHashMap<>();

        for (List<String> processName : system.getProcessNamesToInitialize()) {
            if (processName.size() != 2) {
                throw new UnsupportedOperationException("nested modlues are not supported yet");
            }

            SCClassInstance instance = system.getInstanceByName(processName.get(0));
            SCClass clazz = instance.getSCClass();
            SCProcess scProcess = null;
            for (SCProcess candidate : clazz.getProcesses()) {
                if (candidate.getName().equals(processName.get(1))) {
                    scProcess = candidate;
                    break;
                }
            }

            if (scProcess == null) {
                throw new RuntimeException("process " + processName + " not found");
            }

            ProcessT process = processConstructor.apply(system, scProcess, instance);

            ProcessBlocker waitingFor;
            if (scProcess.getType() == SCPROCESSTYPE.SCTHREAD) {
                waitingFor = null;
            } else { // TODO: initialize / don't initialize
                Set<Event> sensitivities = initialSensitivitiesGetter.apply(process, globalState);
                waitingFor = new EventBlocker(sensitivities, true, null);
            }

            List<List<ValueT>> expressionValues = new ArrayList<>();
            expressionValues.add(new ArrayList<>());

            List<EvaluationContext<ValueT>> executionStack =
                    List.of(new EvaluationContext<>(WrappedSCFunction.getWrapped(scProcess.getFunction()),
                            new ArrayList<>(), -1, expressionValues,
                            determinedValueConstructor.apply(new WrappedSCClassInstance(instance))));

            ProcessStateT processState = processStateConstructor.apply(waitingFor, executionStack);
            processStates.put(process, processState);
        }

        ConsideredState<GlobalStateT, ProcessStateT, ProcessT> result =
                new ConsideredState<GlobalStateT, ProcessStateT, ProcessT>(globalState, processStates);
        result.lock();
        return result;
    }

    private GlobalStateT globalState;
    private Map<ProcessT, ProcessStateT> processStates;

    /**
     * Constructs a new, mutable ConsideredState with the given global and local portions.
     * 
     * The parameters are stored in the newly created object as is, without being copied. The map of
     * local states must be modifiable. Care must be taken not to modify it externally, especially after
     * this state has been locked. Locking this state writes through to the global state and all process
     * states.
     *
     * @param globalState the global portion of this state
     * @param processStates the local states per process (must be modifiable)
     */
    public ConsideredState(GlobalStateT globalState, Map<ProcessT, ProcessStateT> processStates) {
        this.globalState = globalState;
        this.processStates = processStates;
    }

    /**
     * Constructs a new, mutable (deep) copy of the given ConsideredState.
     * 
     * @param copyOf the state to copy
     */
    protected ConsideredState(ConsideredState<GlobalStateT, ProcessStateT, ProcessT> copyOf) {
        super(copyOf);

        LinkedHashMap<ProcessT, ProcessStateT> newProcessStates = new LinkedHashMap<>(copyOf.processStates.size());
        copyOf.processStates.forEach((process, state) -> newProcessStates.put(process, state.unlockedClone()));

        this.globalState = copyOf.globalState.unlockedClone();
        this.processStates = newProcessStates;
    }

    @Override
    protected int hashCodeInternal() {
        int result = 31 * this.globalState.hashCode();
        for (Entry<ProcessT, ProcessStateT> entry : this.processStates.entrySet()) {
            result += entry.getKey().hashCode() * entry.getValue().hashCode();
        }
        return result;
    }

    /**
     * Returns the global portion of this state.
     * 
     * @return global state
     */
    public GlobalStateT getGlobalState() {
        resetHashCode();
        return this.globalState;
    }

    /**
     * Replaces the global portion of this state be the given parameter.
     * 
     * If this state has been locked, an {@link IllegalStateException} is thrown.
     * 
     * @param globalState the new global state
     */
    public void setGlobalState(GlobalStateT globalState) {
        requireNotLocked();
        resetHashCode();
        this.globalState = globalState;
    }

    /**
     * Returns a view of the local portion of this state that is modifiable iff this state is not
     * locked.
     * <p>
     * If the local portion is overwritten by {@link #setProcessStates(Map)}, a previously returned view
     * will no longer be up to date.
     * 
     * @return view of the process states
     */
    public Map<ProcessT, ProcessStateT> getProcessStates() {
        resetHashCode();
        return isLocked() ? Collections.unmodifiableMap(this.processStates) : this.processStates;
    }

    /**
     * Replaces the local portion of this state by the given parameter.
     * 
     * If this state has been locked, an {@link IllegalStateException} is thrown.
     * <p>
     * As at construction, the parameter is stored as is, without being copied. Care must be taken not
     * to modify it, especially after this state has been locked.
     * 
     * @param processStates the new process states
     */
    public void setProcessStates(Map<ProcessT, ProcessStateT> processStates) {
        requireNotLocked();
        resetHashCode();
        this.processStates = processStates;
    }

    /**
     * Returns the local state associated with the given process in this state.
     * 
     * Throws a {@link NullPointerException} if the given process is not associated with any local
     * state.
     * 
     * @param process a process
     * @return the local state associated with the given process
     * @throws NullPointerException if the given process is not associated with any local state
     */
    public ProcessStateT getProcessState(AnalyzedProcess<?, GlobalStateT, ProcessStateT, ?> process)
            throws NullPointerException {
        resetHashCode();
        return Objects.requireNonNull(this.processStates.get(process));
    }

    /**
     * Sets the local state to be associated with the given process.
     * 
     * If this state has been locked, an {@link IllegalStateException} is thrown.
     * 
     * @param process a process
     * @param state the new local state for that process
     */
    public void setState(ProcessT process, ProcessStateT state) {
        requireNotLocked();
        resetHashCode();
        this.processStates.put(process, state);
    }

    /**
     * Returns a collection of all processes which are associated with local states which imply them to
     * be ready to be scheduled.
     * 
     * The collection is guaranteed not to contain duplicates, but may not be a set for performance
     * reasons.
     *
     * @return collection of ready processes
     */
    public Collection<ProcessT> getReadyProcesses() {
        Collection<ProcessT> result = new ArrayList<>();
        for (Entry<ProcessT, ProcessStateT> entry : this.processStates.entrySet()) {
            if (entry.getValue().isReady()) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    /**
     * {@inheritDoc}
     * 
     * Locking this state writes through to the global state and all process states.
     */
    @Override
    public boolean lock() {
        if (!super.lock()) {
            return false;
        }

        this.globalState.lock();
        for (ProcessStateT processState : this.processStates.values()) {
            processState.lock();
        }

        return true;
    }

    // increase visibility
    @Override
    public ConsideredState<GlobalStateT, ProcessStateT, ProcessT> unlockedVersion() {
        return super.unlockedVersion();
    }

    // increase visibility
    @Override
    public ConsideredState<GlobalStateT, ProcessStateT, ProcessT> unlockedClone() {
        return new ConsideredState<>(this);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof ConsideredState<?, ?, ?> s)) {
            return false;
        }
        return this.globalState.equals(s.globalState) && this.processStates.equals(s.processStates);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append("(GlobalState: ").append(this.globalState.toString()).append(" ProcessStates: {");
        boolean first = true;
        for (Entry<ProcessT, ProcessStateT> entry : this.processStates.entrySet()) {
            if (first) {
                first = false;
            } else {
                builder.append(", ");
            }
            builder.append(entry.getKey().toString()).append(": ").append(entry.getValue().toString());
        }
        builder.append("})");

        return builder.toString();
    }

}
