package de.tub.pes.syscir.analysis.statespace_exploration.standard_implementations;

import de.tub.pes.syscir.analysis.statespace_exploration.AbstractedValue;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * Simple implementation of {@link AbstractedValue} that either knows the exact value or doesn't
 * know anything.
 * 
 * @author Jonas Becker-Kupczok
 *
 * @param <X> the type of the value
 */
public class BinaryAbstractedValue<X>
implements AbstractedValue<BinaryAbstractedValue<X>, BinaryAbstractedValue<Boolean>, X> {

    public static class BinaryAbstractedLogic implements AbstractedLogic<BinaryAbstractedValue<Boolean>> {

        public static final BinaryAbstractedLogic INSTANCE = new BinaryAbstractedLogic();

        private BinaryAbstractedLogic() {}

        @Override
        public BinaryAbstractedValue<Boolean> not(BinaryAbstractedValue<Boolean> value) {
            return value.isDetermined() ? of(!value.get()) : empty();
        }

        @Override
        public BinaryAbstractedValue<Boolean> and(BinaryAbstractedValue<Boolean> v1,
                BinaryAbstractedValue<Boolean> v2) {
            if (v1.isDetermined() && !v1.get()) {
                return of(false);
            }
            if (v2.isDetermined() && !v2.get()) {
                return of(false);
            }
            return v1.isDetermined() && v2.isDetermined() ? of(true) : empty();
        }

        @Override
        public BinaryAbstractedValue<Boolean> or(BinaryAbstractedValue<Boolean> v1, BinaryAbstractedValue<Boolean> v2) {
            if (v1.isDetermined() && v1.get()) {
                return of(true);
            }
            if (v2.isDetermined() && v2.get()) {
                return of(true);
            }
            return v1.isDetermined() && v2.isDetermined() ? of(false) : empty();
        }

        @Override
        public BinaryAbstractedValue<Boolean> xor(BinaryAbstractedValue<Boolean> v1,
                BinaryAbstractedValue<Boolean> v2) {
            return v1.isDetermined() && v2.isDetermined() ? of(v1.get() != v2.get()) : empty();
        }

    }

    /**
     * Represents an abstracted value where the real value is unknown.
     */
    public static final BinaryAbstractedValue<?> EMPTY = new BinaryAbstractedValue<>(false, null);

    private static final int EMPTY_VALUE_HASH = 1063647192; // randomly chosen

    /**
     * Returns {@link #EMPTY}, cast to the desired generic type for convenience.
     *
     * @param <X> the type of the value
     * @return an abstracted value representing no known information
     */
    @SuppressWarnings("unchecked")
    public static <X> BinaryAbstractedValue<X> empty() {
        return (BinaryAbstractedValue<X>) EMPTY;
    }

    /**
     * Returns an abstracted value representing the given parameter.
     * 
     * @param <X> the type of the parameter
     * @param value a value
     * @return an abstracted value of the given parameter
     */
    public static <X> BinaryAbstractedValue<X> of(X value) {
        return new BinaryAbstractedValue<>(true, value);
    }

    private final boolean determined;
    private final X value;

    protected BinaryAbstractedValue(boolean determined, X value) {
        this.determined = determined;
        this.value = value;

        assert determined || value == null;
    }

    @Override
    public boolean isDetermined() {
        return this.determined;
    }

    @Override
    public X get() {
        if (!this.determined) {
            throw new NoSuchElementException();
        }

        return this.value;
    }

    @Override
    public BinaryAbstractedValue<X> getLeastUpperBound(BinaryAbstractedValue<X> other) {
        return equals(other) ? this : empty();
    }

    @Override
    public BinaryAbstractedLogic getAbstractedLogic() {
        return BinaryAbstractedLogic.INSTANCE;
    }

    @Override
    public int hashCode() {
        return this.determined ? this.value.hashCode() : EMPTY_VALUE_HASH;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof BinaryAbstractedValue<?> bav)) {
            return false;
        }
        return (!this.determined && !bav.determined)
                || (this.determined && bav.determined && Objects.equals(this.value, bav.value));
    }

    @Override
    public String toString() {
        return this.determined ? "\"" + Objects.toString(this.value) + "\"" : "?";
    }

}
