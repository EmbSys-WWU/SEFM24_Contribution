package raid24contribution.sc_model.variables;

import java.util.List;
import raid24contribution.sc_model.SCVariable;
import raid24contribution.sc_model.expressions.Expression;

/**
 * This class represents a Pointer in SystemC
 * 
 */
public class SCPointer extends SCVariable {
    
    private static final long serialVersionUID = 2599334392854870773L;
    
    public SCPointer(String name) {
        this(name, null);
    }
    
    public SCPointer(String name, String type) {
        this(name, type, false, false, null, null);
    }
    
    public SCPointer(String name, String type, boolean stat, boolean cons, List<String> otherMods, Expression init) {
        super(name);
        this.type = type;
        this._static = stat;
        this._const = cons;
        this.otherModifiers = otherMods;
    }
    
    /**
     * Constructor to initiate a new SCPointer with a different name but the same internal state EXCEPT:
     * all linked objects will be set to null to avoid changes by accident
     *
     * @param old SCPointer which will be copied
     * @param newName new name of the variable
     */
    public SCPointer(SCPointer old, String newName) {
        super(old, newName);
    }
    
    @Override
    public String getDeclarationString() {
        return (this._static ? "static " : "") + (this._const ? "const " : "") + this.type + "* " + this.name;
    }
    
    /**
     * initiates a new SCPointer with a different name but the same internal state EXCEPT: all linked
     * objects will be set to null to avoid changes by accident
     *
     * @param newName new name of the variable
     * @return new SCPointer
     */
    @Override
    public SCVariable flatCopyVariableWitNewName(String newName) {
        return new SCPointer(this, newName);
    }
}
