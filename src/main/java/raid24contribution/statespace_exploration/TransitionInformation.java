package raid24contribution.statespace_exploration;

/**
 * Interface representing some kind of additional information that a {@link AnalyzedProcess} or
 * {@link Scheduler} may provide for each transition.
 * 
 * @author Jonas Becker-Kupczok
 *
 */
public interface TransitionInformation<InfoT extends TransitionInformation<InfoT>> {

    /**
     * Returns a clone of this object.
     * 
     * @return clone
     */
    InfoT clone();

}
