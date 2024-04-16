package de.tub.pes.syscir.analysis.statespace_exploration;

import java.util.List;
import java.util.Objects;

/**
 * Class representing an abstraction of the local state of a process in a SystemC design.
 * 
 * This includes the scheduling state of the process (is it ready, waiting for some (possibly delta)
 * time or waiting for an event) as well as the current execution stack.
 * <p>
 * This state starts out as mutable but can be locked to be immutable by invoking the method
 * {@link #lock()}. When locked, any attempt to modify this state results in an
 * {@link IllegalStateException}.
 * 
 * @author Jonas Becker-Kupczok
 *
 * @param <ProcessStateT> the type of this state (to specify the return type of
 *        {@link #unlockedClone()})
 * @param <ValueT> the type of abstracted values used for this state
 */
public abstract class ProcessState<ProcessStateT extends ProcessState<ProcessStateT, ValueT>, ValueT extends AbstractedValue<ValueT, ?, ?>>
        extends LocalState<ProcessStateT, ValueT> {

    private ProcessBlocker waitingFor; // null means ready

    /**
     * Constructs a new, mutable ProcessState waiting for the given {@link ProcessBlocker} at the given
     * execution stack.
     * <p>
     * The parameters are stored in the newly created object as is, without being copied. The lists must
     * be modifiable. Care must be taken not to modify them externally, especially after this state has
     * been locked.
     * 
     * @param waitingFor what this process is waiting for (null means it's ready to be scheduled)
     * @param executionStack where in the program this process is currently waiting (the last element in
     *        the list is at the top of the execution stack)
     */
    public ProcessState(ProcessBlocker waitingFor, List<EvaluationContext<ValueT>> executionStack) {
        super(executionStack);

        this.waitingFor = waitingFor;
    }

    /**
     * Constructs a new, mutable copy of the given ProcessState.
     * 
     * @param copyOf the state to copy
     */
    protected ProcessState(ProcessState<ProcessStateT, ValueT> copyOf) {
        super(copyOf);

        this.waitingFor = copyOf.waitingFor;
    }

    /**
     * Returns the ProcessBlocker the process is waiting for if in this state, or null if the process is
     * ready.
     *
     * @return what the process is waiting for
     */
    public ProcessBlocker getWaitingFor() {
        return this.waitingFor;
    }

    /**
     * Returns whether or not the process is ready to be scheduled in this state.
     *
     * @return whether the process is ready
     */
    public boolean isReady() {
        return this.waitingFor == null;
    }

    /**
     * Sets what the process is waiting for in this state. If the parameter is null, the process will be
     * ready.
     *
     * @param waitingFor what the process shall be waiting for
     * @throws IllegalStateException if this state has been locked
     */
    public void setWaitingFor(ProcessBlocker waitingFor) {
        requireNotLocked();
        resetHashCode();
        this.waitingFor = waitingFor;
    }

    @Override
    protected int hashCodeInternal() {
        return super.hashCodeInternal() * 31 + Objects.hashCode(this.waitingFor);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof ProcessState<?, ?> p)) {
            return false;
        }
        return super.equalsInternal(p) && Objects.equals(this.waitingFor, p.waitingFor);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (this.waitingFor == null) {
            builder.append("Ready");
        } else if (this.waitingFor == ProcessTerminatedBlocker.INSTANCE) {
            builder.append("Done");
        } else {
            builder.append("Wait ").append(this.waitingFor);
        }
        builder.append(" at ").append(getExecutionStack());
        return builder.toString();
    }

}
