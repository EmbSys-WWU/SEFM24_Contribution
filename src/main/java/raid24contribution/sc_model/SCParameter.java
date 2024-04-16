package raid24contribution.sc_model;

import java.io.Serializable;
import raid24contribution.sc_model.variables.SCPointer;

/**
 * Encapsules variables used as parameters in the head of a function. A Parameter consists of a
 * variable and a reference type (either by value or by reference).
 * 
 */
public class SCParameter implements Serializable {
    
    private static final long serialVersionUID = -8632218974613314166L;
    
    private SCVariable var;
    private SCREFERENCETYPE refType;
    /**
     * The scfunction this parameter is a parameter of.
     */
    private SCFunction function;
    
    /**
     * Constructs a parameter with the name and type specified in var and the reference type refType
     * 
     * @param var
     * @param refType
     */
    public SCParameter(SCVariable var, SCREFERENCETYPE refType) {
        assert var != null;
        this.var = var;
        this.refType = refType;
        // usually, the parameter is created before the function is created.
        // Therefore we set function to null here. It is set when a parameter is
        // added to a function in SCFunction.
        this.function = null;
    }
    
    @Override
    public String toString() {
        String ret = (this.var.isConst() ? "const " : "") + this.var.getType();
        if (this.var instanceof SCPointer) {
            return ret + " *" + this.var.getName();
        } else {
            return ret + " " + this.refType.getSymbol() + this.var.name;
        }
    }
    
    public SCVariable getVar() {
        return this.var;
    }
    
    public void setVar(SCVariable var) {
        this.var = var;
    }
    
    public SCREFERENCETYPE getRefType() {
        return this.refType;
    }
    
    public void setRefType(SCREFERENCETYPE refType) {
        this.refType = refType;
    }
    
    /**
     * Returns the function this parameter belongs to or null, if the function is not set.
     * 
     * @return
     */
    public SCFunction getFunction() {
        return this.function;
    }
    
    public void setFunction(SCFunction function) {
        this.function = function;
    }
    
    public boolean isConst() {
        return this.var.isConst();
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.refType == null) ? 0 : this.refType.hashCode());
        result = prime * result + ((this.var == null) ? 0 : this.var.hashCode());
        return result;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        SCParameter other = (SCParameter) obj;
        if (this.refType != other.refType) {
            return false;
        }
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
