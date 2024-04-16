package raid24contribution.statespace_exploration.standard_implementations;

import raid24contribution.statespace_exploration.TransitionInformation;

/**
 * Interface for {@link TransitionInformation} that can be composed with other information of the
 * same type.
 * 
 * @author Jonas Becker-Kupczok
 *
 * @param <InfoT> the type of this object (to specify the return type of {@link #compose(Object)})
 */
public interface ComposableTransitionInformation<InfoT extends ComposableTransitionInformation<InfoT>>
extends TransitionInformation<InfoT> {

    /**
     * Returns an object representing the merger of this and the parameter, i.e. the other information
     * is gathered from code evaluated alternatively to that which resulted in this information.
     * <p>
     * Whether the returned object is a new instance or this object with modified state is left to the
     * imlementation.
     * 
     * @param other
     * @return
     */
    InfoT compose(InfoT other);

}
