package raid24contribution.sc_model.variables;

import java.util.ArrayList;
import java.util.List;
import raid24contribution.sc_model.SCVariable;
import raid24contribution.sc_model.expressions.Expression;
import raid24contribution.sc_model.expressions.SCVariableDeclarationExpression;
import raid24contribution.sc_model.expressions.SCVariableExpression;

/**
 * this class represents a simpletype, such as int, unsign int, ... all possible simpletypes are
 * found in the config/simpletypes.properties
 * 
 */

public class SCSimpleType extends SCVariable {
    
    private static final long serialVersionUID = -5056967513486882322L;
    
    /**
     * constructor inherits from superclass
     * 
     * @param nam name of the variable
     */
    public SCSimpleType(String nam) {
        super(nam);
        this.otherModifiers = new ArrayList<>();
    }
    
    /**
     * constructor inherits from superclass
     * 
     * @param nam name of the variable
     * @param t type of the variable
     * @param other_mods
     * @param cons
     * @param stat
     */
    public SCSimpleType(String nam, String t, boolean stat, boolean cons, List<String> other_mods) {
        super(nam);
        this.type = t;
        this._static = stat;
        this._const = cons;
        this.otherModifiers = other_mods;
    }
    
    /**
     * creates new SimpleType-Variable which is initialized with a value
     * 
     * @param nam name of the variable
     * @param t type of the variable
     * @param val value of the variable
     */
    public SCSimpleType(String nam, String t, Expression val, boolean stat, boolean cons, List<String> other_mods) {
        super(nam);
        this.type = t;
        // if (val != null) {
        // this.setFirstInitialExpression(val);
        // }
        this._static = stat;
        this._const = cons;
        this.otherModifiers = other_mods;
    }
    
    public SCSimpleType(String name, String type, Expression val, boolean cons) {
        super(name);
        this.type = type;
        // if (val != null) {
        // this.setFirstInitialExpression(val);
        // }
        this._const = cons;
        this._static = false;
        this.otherModifiers = new ArrayList<>();
    }
    
    public SCSimpleType(String name, String type, Expression val) {
        super(name);
        this.type = type;
        // if (val != null) {
        // this.setFirstInitialExpression(val);
        // }
        // added by ammar for initializing simulationStopped and simulationTime
        if (val != null) {
            setDeclaration(new SCVariableDeclarationExpression(null,
                    new SCVariableExpression(null, new SCSimpleType(name, type)), val));
        }
        // ends here
        this._static = false;
        this._const = false;
        this.otherModifiers = new ArrayList<>();
    }
    
    public SCSimpleType(String name, String type) {
        super(name);
        this.type = type;
        
        this._static = false;
        this._const = false;
        this.otherModifiers = new ArrayList<>();
    }
    
    /**
     * Constructor to initiate a new SCSimpleType with a different name but the same internal state
     * EXCEPT: all linked objects will be set to null to avoid changes by accident
     *
     * @param old SCSimpleType which will be copied
     * @param newName new name of the variable
     */
    public SCSimpleType(SCSimpleType old, String newName) {
        super(old, newName);
    }
    
    /**
     * initiates a new SCSimpleType with a different name but the same internal state EXCEPT: all linked
     * objects will be set to null to avoid changes by accident
     *
     * @param newName new name of the variable
     * @return new SCSimpleType
     */
    @Override
    public SCVariable flatCopyVariableWitNewName(String newName) {
        return new SCSimpleType(this, newName);
    }
}
