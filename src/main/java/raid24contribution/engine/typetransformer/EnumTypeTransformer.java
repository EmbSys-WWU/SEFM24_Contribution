package raid24contribution.engine.typetransformer;

import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import raid24contribution.engine.Environment;
import raid24contribution.sc_model.SCClass;
import raid24contribution.sc_model.variables.SCKnownType;

/**
 * 
 *
 */
public class EnumTypeTransformer extends AbstractTypeTransformer {
    
    private static Logger logger = LogManager.getLogger(EnumTypeTransformer.class.getName());
    
    public EnumTypeTransformer(String enumName) {
        super();
        this.name = enumName;
    }
    
    @Override
    public SCKnownType createInstance(String instName, Environment e, boolean stat, boolean cons,
            List<String> other_mods) {
        // return super.createInstance(instName, e, stat, cons, other_mods);
        SCClass type = e.getKnownTypes().get(this.name);
        SCKnownType kt = null;
        if (type != null) {
            kt = new SCKnownType(instName, type, e.getCurrentClass(), null, stat, cons, other_mods,
                    e.getLastInitializer());
        } else {
            logger.error("Configuration error: type {} is not available", this.name);
        }
        return kt;
    }
}
