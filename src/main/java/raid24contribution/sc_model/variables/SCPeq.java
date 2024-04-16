package raid24contribution.sc_model.variables;

import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import raid24contribution.sc_model.SCClass;
import raid24contribution.sc_model.SCFunction;
import raid24contribution.sc_model.SCVariable;

/**
 * This class represents a payload event queue in SystemC.
 * 
 */
public class SCPeq extends SCVariable {
    
    private static final long serialVersionUID = 3140481869471730100L;
    
    private static Logger logger = LogManager.getLogger(SCPeq.class.getName());
    
    protected SCClass owner = null;
    protected List<String> subtypes;
    
    protected SCFunction callback;
    
    public SCPeq(String n, SCClass owner, List<String> type, boolean stat, boolean cons, List<String> other_mods) {
        super(n);
        this.type = "peq_with_cb_and_phase";
        this.owner = owner;
        this.subtypes = type;
        this._static = stat;
        this._const = cons;
        this.otherModifiers = other_mods;
    }
    
    /**
     * Constructor to initiate a new SCPeq with a different name but the same internal state EXCEPT: all
     * linked objects will be set to null to avoid changes by accident
     *
     * @param old SCVariable which will be copied
     * @param newName name that will be set in new instantiation
     */
    public SCPeq(SCPeq old, String newName) {
        super(old, newName);
        this.owner = null;
        this.subtypes = null;
        this.callback = null;
    }
    
    public boolean setCallback(SCFunction callback) {
        if (this.callback == null) {
            this.callback = callback;
            return true;
        } else {
            return false;
        }
    }
    
    public SCFunction getCallback() {
        return this.callback;
    }
    
    public List<String> getSubtypes() {
        return new ArrayList<>(this.subtypes);
    }
    
    public SCClass getOwner() {
        return this.owner;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((this.owner == null) ? 0 : this.owner.getName().hashCode());
        result = prime * result + ((this.subtypes == null) ? 0 : this.subtypes.hashCode());
        return result;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof SCPeq other)) {
            return false;
        }
        if (this.owner == null) {
            if (other.owner != null) {
                return false;
            }
        } else if (!this.owner.getName().equals(other.owner.getName())) {
            return false;
        }
        if (this.subtypes == null) {
            if (other.subtypes != null) {
                return false;
            }
        } else if (!this.subtypes.equals(other.subtypes)) {
            return false;
        }
        return true;
    }
    
    /**
     * Returns the type of the phase of the peq. This should be the same for each PEQ.
     * 
     * @return
     */
    public String getPhaseType() {
        return "int";
    }
    
    /**
     * Returns the type of the payload of the peq. This differs between different peqs, depending on the
     * peq-declaration.
     * 
     * @return
     */
    public String getPayloadType() {
        if (this.subtypes.size() > 0) {
            return this.subtypes.get(0);
        } else {
            logger.error("Could not derive the type of the payload of the peq {}.", this.name);
            return "";
        }
        
    }
    
    /**
     * initiates a new SCPeq with a different name but the same internal state EXCEPT: all linked
     * objects will be set to null to avoid changes by accident
     *
     * @param newName newName name that will be set in new instantiation
     * @return new SCPeq
     */
    @Override
    public SCVariable flatCopyVariableWitNewName(String newName) {
        return new SCPeq(this, newName);
    }
    
}
