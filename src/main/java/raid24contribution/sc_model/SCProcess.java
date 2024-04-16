package raid24contribution.sc_model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import raid24contribution.sc_model.variables.SCEvent;
import raid24contribution.sc_model.variables.SCPortEvent;

/**
 * this class represents a SCProcess it has a Link to a SCFunction, a List of Events on which this
 * process is sensitive for and en enumSet of Modifiers theirs also a name, which is usually equal
 * to the function-name an a Type, for example SCThread, SCMethod
 * 
 */

public class SCProcess implements Serializable {
    
    private static final long serialVersionUID = 4827343065412103535L;
    
    /**
     * the name of the Process
     */
    private String name;
    /**
     * the List of events, on which the Process is sensitive on
     */
    private List<SCEvent> sensitive_on;
    /**
     * the modifier of the process, like dontinitialize
     */
    private EnumSet<SCMODIFIER> modifier;
    /**
     * the Function to which this process refer
     */
    private SCFunction function;
    /**
     * the type of the process
     */
    private SCPROCESSTYPE type;
    
    /**
     * creates a dummy process
     * 
     * @param nam name of the process
     */
    public SCProcess(String nam) {
        this.name = nam;
        this.sensitive_on = new ArrayList<>();
    }
    
    /**
     * creates a new process with a function and a processtype
     * 
     * @param nam name of the new process
     * @param t type of the process
     * @param fct function of the process
     */
    public SCProcess(String nam, SCPROCESSTYPE t, SCFunction fct) {
        this.name = nam;
        this.function = fct;
        this.type = t;
        this.modifier = EnumSet.of(SCMODIFIER.NONE);
        this.sensitive_on = new ArrayList<>();
    }
    
    /**
     * creates a new process with a function, a processtype and a list of events where this process is
     * sensitive for
     * 
     * @param nam name of the process
     * @param t type of the process
     * @param fct function of the process
     * @param sens_lst sensitivity-list
     */
    public SCProcess(String nam, SCPROCESSTYPE t, SCFunction fct, List<SCEvent> sens_lst) {
        this.name = nam;
        this.sensitive_on = sens_lst;
        this.function = fct;
        this.type = t;
        this.modifier = EnumSet.of(SCMODIFIER.NONE);
    }
    
    /**
     * creates a new process with a function, a processtype and a Set of modifiers
     * 
     * @param nam name of the process
     * @param t type of the process
     * @param fct function of the process
     * @param mod set of modifiers
     */
    public SCProcess(String nam, SCPROCESSTYPE t, SCFunction fct, EnumSet<SCMODIFIER> mod) {
        this.name = nam;
        this.function = fct;
        this.type = t;
        if (mod != null) {
            this.modifier = mod;
        } else {
            this.modifier = EnumSet.of(SCMODIFIER.NONE);
        }
        this.sensitive_on = new ArrayList<>();
    }
    
    /**
     * creates a new process with a function, a processtype, a list of events where this process is
     * sensitive for and a set of modifiers
     * 
     * @param nam name of the process
     * @param t type of the process
     * @param fct function of the process
     * @param sens_lst sensitivity-list
     * @param mod set of modifiers
     */
    public SCProcess(String nam, SCPROCESSTYPE t, SCFunction fct, List<SCEvent> sens_lst, EnumSet<SCMODIFIER> mod) {
        this.name = nam;
        this.sensitive_on = sens_lst;
        this.function = fct;
        this.type = t;
        if (mod != null) {
            this.modifier = mod;
        } else {
            this.modifier = EnumSet.of(SCMODIFIER.NONE);
        }
    }
    
    /**
     * return name of the process
     * 
     * @return String
     */
    public String getName() {
        return this.name;
    }
    
    /**
     * return the list of the Events on which the process is sensitive
     * 
     * @return List<SCEvent>
     */
    public List<SCEvent> getSensitivity() {
        return this.sensitive_on;
    }
    
    /**
     * returns the function of the process
     * 
     * @return SCFunction
     */
    public SCFunction getFunction() {
        return this.function;
    }
    
    /**
     * returns the function of the process
     * 
     * @return SCFunction
     */
    public void setFunction(SCFunction f) {
        this.function = f;
    }
    
    /**
     * returns the type of the process
     * 
     * @return SCPROCESSTYPE
     */
    public SCPROCESSTYPE getType() {
        return this.type;
    }
    
    /**
     * returns the set of modifiers
     * 
     * @return EnumSet<SCMODIFIER>
     */
    public EnumSet<SCMODIFIER> getModifier() {
        return this.modifier;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    @Override
    public String toString() {
        String ret = this.type.toString() + "(" + this.function.getName() + ");\n";
        
        if (this.modifier != null) {
            for (SCMODIFIER mod : this.modifier) {
                if (mod != SCMODIFIER.NONE) {
                    ret += mod.toString() + ";\n";
                }
            }
        }
        
        if (this.sensitive_on != null && !this.sensitive_on.isEmpty()) {
            ret += "sensitive";
            for (SCEvent e : this.sensitive_on) {
                ret += " << " + e.getName();
                if (e instanceof SCPortEvent scpe) {
                    ret += "." + scpe.getEventType();
                }
            }
            ret += ";";
        }
        return ret;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.function == null) ? 0 : this.function.hashCode());
        result = prime * result + ((this.modifier == null) ? 0 : this.modifier.hashCode());
        result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
        result = prime * result + ((this.sensitive_on == null) ? 0 : this.sensitive_on.hashCode());
        result = prime * result + ((this.type == null) ? 0 : this.type.hashCode());
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
        SCProcess other = (SCProcess) obj;
        if (this.function == null) {
            if (other.function != null) {
                return false;
            }
        } else if (!this.function.equals(other.function)) {
            return false;
        }
        if (this.modifier == null) {
            if (other.modifier != null) {
                return false;
            }
        } else if (!this.modifier.equals(other.modifier)) {
            return false;
        }
        if (this.name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!this.name.equals(other.name)) {
            return false;
        }
        if (this.sensitive_on == null) {
            if (other.sensitive_on != null) {
                return false;
            }
        } else if (!this.sensitive_on.equals(other.sensitive_on)) {
            return false;
        }
        if (this.type != other.type) {
            return false;
        }
        return true;
    }
    
}
