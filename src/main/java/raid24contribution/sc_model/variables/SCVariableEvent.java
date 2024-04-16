package raid24contribution.sc_model.variables;

import java.util.ArrayList;
import raid24contribution.sc_model.SCVariable;

/**
 * this class is used for variable-events which are required for sensitivity on variables
 *
 */
public class SCVariableEvent extends SCEvent {
    
    private static final long serialVersionUID = 8650960275883404781L;
    
    protected SCVariable var = null;
    
    public SCVariableEvent(String nam, SCVariable v) {
        super(nam, false, false, new ArrayList<>());
        this.var = v;
    }
    
    public SCVariable getVar() {
        return this.var;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((this.var == null) ? 0 : this.var.hashCode());
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
        if (getClass() != obj.getClass()) {
            return false;
        }
        SCVariableEvent other = (SCVariableEvent) obj;
        if (this.var == null) {
            if (other.var != null) {
                return false;
            }
        } else if (!this.var.equals(other.var)) {
            return false;
        }
        return true;
    }
    
}
