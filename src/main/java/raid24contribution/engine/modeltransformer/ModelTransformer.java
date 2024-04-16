package raid24contribution.engine.modeltransformer;

import raid24contribution.sc_model.SCSystem;

/**
 * This is an interface for all ModelTransformers for SysCIR. A ModellTransformer transforms SysCIR
 * elements into other SysCIR elements.
 * 
 * 
 */
public interface ModelTransformer {
    
    /**
     * Transforms a whole SysCIR model into another SysCIR model.
     * 
     * @param model
     * @return
     */
    public SCSystem transformModel(SCSystem model);
}
