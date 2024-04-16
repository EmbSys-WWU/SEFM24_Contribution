package raid24contribution.sc_model.variables;

import java.util.List;
import raid24contribution.sc_model.SCVariable;
import raid24contribution.sc_model.expressions.Expression;

/**
 * This class represents a Time-Object in SystemC.
 * 
 */
public class SCTime extends SCVariable {
    
    private static final long serialVersionUID = -7735653162175762184L;
    
    private boolean useFuncCall = false;
    
    public SCTime(String nam, boolean stat, boolean cons, List<String> other_mods, boolean useFuncCall) {
        super(nam);
        this.type = "sc_time";
        this._static = stat;
        this._const = cons;
        this.otherModifiers = other_mods;
        this.useFuncCall = useFuncCall;
    }
    
    /**
     * Constructor to initiate a new SCTime with a different name but the same internal state EXCEPT:
     * all linked objects will be set to null to avoid changes by accident
     *
     * @param old SCTime which will be copied
     * @param newName new name of the variable
     */
    public SCTime(SCTime old, String newName) {
        super(old, newName);
    }
    
    @Override
    public String getInitializationString() {
        if (this.useFuncCall && getInitialValueCount() > 0) {
            StringBuffer ret = new StringBuffer(" = sc_time");
            ret.append("(");
            for (Expression exp : this.declaration.getInitialValues()) {
                ret.append(exp.toStringNoSem() + ", ");
            }
            ret.setLength(ret.length() - 2);
            ret.append(")");
            return ret.toString();
        }
        return "";
    }
    
    /**
     * initiates a new SCTime with a different name but the same internal state EXCEPT: all linked
     * objects will be set to null to avoid changes by accident
     *
     * @param newName new name of the variable
     * @return new SCTime
     */
    @Override
    public SCVariable flatCopyVariableWitNewName(String newName) {
        return new SCTime(this, newName);
    }
    
    
    
}
