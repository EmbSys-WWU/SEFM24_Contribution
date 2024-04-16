package raid24contribution.sc_model.variables;

import java.util.List;
import raid24contribution.sc_model.SCVariable;

/**
 * this class represents an event in SystemC the only important is it's name
 * 
 */
public class SCEvent extends SCVariable {
    
    private static final long serialVersionUID = -8291494536976028138L;
    
    /**
     * Creats a new Event
     * 
     * @param nam Name of the Event
     * @param other_mods
     * @param cons
     * @param stat
     */
    public SCEvent(String nam, boolean stat, boolean cons, List<String> other_mods) {
        super(nam);
        this.type = "SCEvent";
        this._static = stat;
        this._const = cons;
        this.otherModifiers = other_mods;
    }
    
    /**
     * Constructor to initiate a new SCEvent with a different name but the same internal state EXCEPT:
     * all linked objects will be set to null to avoid changes by accident
     *
     * @param old SCEvent which will be copied
     * @param newName new name of the variable
     */
    protected SCEvent(SCEvent old, String newName) {
        super(old, newName);
    }
    
    /**
     * initiates a new SCEvent with a different name but the same internal state EXCEPT: all linked
     * objects will be set to null to avoid changes by accident
     *
     * @param newName new name of the variable
     * @return new SCEvent
     */
    @Override
    public SCVariable flatCopyVariableWitNewName(String newName) {
        return new SCEvent(this, newName);
    }
    
    
    
}
