package de.tub.pes.syscir.analysis.statespace_exploration;

/**
 * Interface describing any abstraction of a semantic value.
 * <p>
 * Any such abstraction must account for absolutely determined values on the one hand as well as
 * values about which nothing is known on the other. Arbitrary levels of information in between are
 * permissible at the abstraction's discretion.
 * <p>
 * Instances of this class are assumed to be immutable.
 * 
 * @author Jonas Becker-Kupczok
 *
 * @param <X> the type of value abstracted
 */
public interface AbstractedValue<ValueT extends AbstractedValue<ValueT, BoolT, X>, BoolT extends AbstractedValue<BoolT, BoolT, Boolean>, X> {

    public static interface AbstractedLogic<BoolT extends AbstractedValue<BoolT, BoolT, Boolean>> {

        BoolT not(BoolT value);

        BoolT and(BoolT v1, BoolT v2);

        BoolT or(BoolT v1, BoolT v2);

        BoolT xor(BoolT v1, BoolT v2);

    }

    /**
     * Returns whether or not the value is absolutely determined.
     * 
     * @return whether the value is determined
     */
    boolean isDetermined();

    /**
     * Returns whether or not the value is at least somewhat undetermined.
     *
     * @return whether the value is undetermiend
     */
    default boolean isUndetermined() {
        return !isDetermined();
    }

    /**
     * Returns the determined value.
     * 
     * If this value is not determined, a RuntimeException at the discretion of the implementation is
     * thrown.
     * 
     * @return the determined value.
     * @throws RuntimeException if the value is not determined
     */
    X get() throws RuntimeException;

    /**
     * Returns an abstracted value representing that the actual value could be any value allowed by this
     * or the other abstracted value.
     *
     * @param other another abstracted value
     * @return the least upper bound of both abstracted values
     */
    ValueT getLeastUpperBound(ValueT other);

    /**
     * Returns an abstracted boolean logic compatible with booleans in this abstraction.
     *
     * @return abstracted boolean logic
     */
    AbstractedLogic<BoolT> getAbstractedLogic();

}
