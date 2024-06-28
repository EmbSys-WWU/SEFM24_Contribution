package de.tub.pes.syscir.statespace_exploration.transition_informations;

import de.tub.pes.syscir.statespace_exploration.AbstractedValue;
import de.tub.pes.syscir.statespace_exploration.HashCachingLockableObject;
import de.tub.pes.syscir.statespace_exploration.standard_implementations.ComposableTransitionInformation;
import de.tub.pes.syscir.statespace_exploration.standard_implementations.Variable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;


public class VariablesReadWrittenInformation<BoolT extends AbstractedValue<BoolT, BoolT, Boolean>>
extends HashCachingLockableObject<VariablesReadWrittenInformation<BoolT>>
implements ComposableTransitionInformation<VariablesReadWrittenInformation<BoolT>> {

    private Map<Variable<?, ?>, BoolT> read;
    private Map<Variable<?, ?>, BoolT> written;

    public VariablesReadWrittenInformation(Map<Variable<?, ?>, BoolT> read, Map<Variable<?, ?>, BoolT> written) {
        this.read = new LinkedHashMap<>(read);
        this.written = new LinkedHashMap<>(written);
    }

    public VariablesReadWrittenInformation(VariablesReadWrittenInformation<BoolT> copyOf) {
        super(copyOf);

        this.read = new LinkedHashMap<>(copyOf.read);
        this.written = new LinkedHashMap<>(copyOf.written);
    }

    public VariablesReadWrittenInformation() {
        this(Map.of(), Map.of());
    }

    public Map<Variable<?, ?>, BoolT> getRead() {
        return Collections.unmodifiableMap(this.read);
    }

    public Map<Variable<?, ?>, BoolT> getWritten() {
        return Collections.unmodifiableMap(this.written);
    }

    public VariablesReadWrittenInformation<BoolT> concat(VariablesReadWrittenInformation<BoolT> other) {
        requireNotLocked();
        for (Map.Entry<Variable<?, ?>, BoolT> entry : other.getRead().entrySet()) {
            this.read.merge(entry.getKey(), entry.getValue(),
                    (v1, v2) -> entry.getValue().getAbstractedLogic().or(v1, v2));
        }
        for (Map.Entry<Variable<?, ?>, BoolT> entry : other.getWritten().entrySet()) {
            this.written.merge(entry.getKey(), entry.getValue(),
                    (v1, v2) -> entry.getValue().getAbstractedLogic().or(v1, v2));
        }
        resetHashCode();
        return this;
    }

    @Override
    public VariablesReadWrittenInformation<BoolT> compose(VariablesReadWrittenInformation<BoolT> other) {
        requireNotLocked();
        for (Map.Entry<Variable<?, ?>, BoolT> entry : other.getRead().entrySet()) {
            this.read.merge(entry.getKey(), entry.getValue(), BoolT::getLeastUpperBound);
        }
        for (Map.Entry<Variable<?, ?>, BoolT> entry : other.getWritten().entrySet()) {
            this.written.merge(entry.getKey(), entry.getValue(), BoolT::getLeastUpperBound);
        }
        resetHashCode();
        return this;
    }

    @Override
    public VariablesReadWrittenInformation<BoolT> clone() {
        return unlockedClone();
    }

    @Override
    public VariablesReadWrittenInformation<BoolT> unlockedClone() {
        return new VariablesReadWrittenInformation<>(this);
    }

    // increase visibility
    @Override
    public VariablesReadWrittenInformation<BoolT> unlockedVersion() {
        return super.unlockedVersion();
    }

    @Override
    protected int hashCodeInternal() {
        return 31 * this.read.hashCode() + this.written.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof VariablesReadWrittenInformation<?> vrwi)) {
            return false;
        }
        return this.read.equals(vrwi.read) && this.written.equals(vrwi.written);
    }

    @Override
    public String toString() {
        return "[read=" + this.read + ", written=" + this.written + "]";
    }

}
