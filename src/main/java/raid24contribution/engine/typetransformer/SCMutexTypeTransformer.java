package raid24contribution.engine.typetransformer;

import raid24contribution.engine.Environment;

public class SCMutexTypeTransformer extends AbstractTypeTransformer {
    
    @Override
    public void createType(Environment e) {
        super.createType(e);
        this.name = "sc_mutex";
        // SCModule clock = e.moduleList.get(name);
        // clock.setDefaultEvent("edge");
    }
}
