package de.tub.pes.syscir.analysis.util;

import de.tub.pes.syscir.sc_model.SCFunction;
import de.tub.pes.syscir.sc_model.expressions.Expression;
import java.util.Map;

/**
 * Wrapper for {@link SCFunction} that allows no modifications and caches the hashCode. Assumes that
 * the original is not modified externally.
 *
 * @author Jonas Becker-Kupczok
 *
 */
public class WrappedExpression {

    // cache of wrappers, avoiding the creation of a new wrapper every time the original is encountered
    private static final Map<Expression, WrappedExpression> wrapperCache = new WeakIdentityHashMap<>();

    /**
     * Returns a new wrapper around the original.
     * 
     * No guarantee is made with regards to the identity of the wrapper. If a wrapper already exists, it
     * may be reused. No check is made for whether or not the hashCode of that wrapper is still valid.
     * 
     * @param original an WrappedSCFunction
     * @return a wrapper around the original
     */
    public static WrappedExpression getWrapped(Expression original) {
        return wrapperCache.computeIfAbsent(original, WrappedExpression::new);
    }

    private Expression original;

    private int hashCode;

    /**
     * Creates a new wrapper around the original, caching the originals current hashCode.
     *
     * @param original an SCFunction
     */
    public WrappedExpression(Expression original) {
        this.original = original;
        this.hashCode = original.hashCode();
    }

    public Expression getOriginal() {
        return this.original;
    }

    @Override
    public int hashCode() {
        return this.hashCode;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof WrappedExpression wrapped)) {
            return false;
        }
        return this.original.equals(wrapped.original);
    }

    @Override
    public String toString() {
        return this.original.toString();
    }

}
