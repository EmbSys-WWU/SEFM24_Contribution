package raid24contribution.statespace_exploration.standard_implementations;

import java.util.Map;
import java.util.function.Supplier;
import raid24contribution.statespace_exploration.AbstractedValue;

/**
 * Interface for an entity that stores the values of variables.
 * 
 * Values may not be null, but may be non-determined or abstractions of null.
 *
 * @author Jonas Becker-Kupczok
 *
 * @param <VarT> the type of variable considered
 * @param <ValueT> the type of value stored
 */
public interface VariableHolder<VarT extends Variable<?, ?>, ValueT extends AbstractedValue<ValueT, ?, ?>> {

    /**
     * Returns the variable values as an unmodifiable map.
     *
     * @return unmodifiable map of variable values
     */
    Map<VarT, ValueT> getVariableValues();

    /**
     * Returns the value stored for the given variable, or the result of the defaultGetter if the
     * variable isn't stored.
     *
     * @param variable the variable
     * @param defaultGetter the supplier of the default value
     * @return the value stored for the variable, or the default value
     */
    default ValueT getValue(VarT variable, Supplier<ValueT> defaultGetter) {
        ValueT result = getVariableValues().get(variable);
        return result == null ? defaultGetter.get() : result;
    }

    /**
     * Sets the value of the given variable.
     *
     * @param variable the variable
     * @param value the value
     */
    void setVariableValue(VarT variable, ValueT value);

    /**
     * Removes the given variable from the storage.
     *
     * @param variable the variable
     */
    void deleteVariableValue(VarT variable);

}
