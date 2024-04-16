package raid24contribution.sc_model.variables;

import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import raid24contribution.sc_model.SCClass;
import raid24contribution.sc_model.expressions.Expression;

/**
 * this class represents a KnownType
 * 
 */
public class SCKnownType extends SCClassInstance {
    
    private static final long serialVersionUID = -1550114392150953966L;
    
    private static transient Logger logger = LogManager.getLogger(SCKnownType.class.getName());
    
    public SCKnownType(String name, SCClass type, SCClass outerClass, List<Expression> para, boolean stat, boolean cons,
            List<String> other_mods, Expression init) {
        super(name, type, outerClass);
        initialize(para);
        this._static = stat;
        this._const = cons;
        this.otherModifiers = other_mods;
    }
    
    public void initialize(List<Expression> params) {
        // if (!this.initialized) {
        // if (!params.isEmpty()) {
        // this.initialExpressions = params;
        // this.initialized = true;
        // }
        // } else {
        // logger.warn("SCKnownType.initialize: Variable is already initialized");
        // }
    }
    
}
