package raid24contribution.sc_model.variables;

import java.util.LinkedList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import raid24contribution.sc_model.SCClass;
import raid24contribution.sc_model.SCConnectionInterface;
import raid24contribution.sc_model.SCMODIFIER;
import raid24contribution.sc_model.SCProcess;
import raid24contribution.sc_model.SCVariable;
import raid24contribution.sc_model.expressions.Expression;

/**
 * Represents an instance of an scclass. Contains a mapping for the ports/sockets of the class, the
 * expressions used in the initialization of the scclass and all modifiers.
 * 
 */
public class SCClassInstance extends SCVariable {
    
    private static Logger logger = LogManager.getLogger(SCClassInstance.class.getName());
    
    private static final long serialVersionUID = -3456898813749532732L;
    
    /**
     * The scclass this instance is an instance of.
     */
    protected SCClass scclass;
    
    /**
     * In hierarchical designs classinstances can be members of other classes. If this class is a member
     * of another class, outerClass is not null.
     */
    protected SCClass outerClass;
    
    /**
     * the port/socket instances this instance has.
     */
    protected List<SCConnectionInterface> portSocketInstances;
    
    /**
     * The label of the instance. Mostly passed as an argument when invoking the constructor.
     */
    protected String instanceLabel;
    
    public SCClassInstance(String name, SCClass scclass, SCClass outerClass, boolean _static, boolean _const,
            List<String> otherModifiers, String instanceLabel) {
        super(name);
        this._static = _static;
        this._const = _const;
        this.otherModifiers = otherModifiers;
        this.outerClass = outerClass;
        
        this.scclass = scclass;
        this.portSocketInstances = new LinkedList<>();
        this.type = scclass.getName();
        this.instanceLabel = instanceLabel;
    }
    
    public SCClassInstance(String name, SCClass scclass, SCClass outerClass) {
        this(name, scclass, outerClass, false, false, new LinkedList<>(), null);
    }
    
    /**
     * Constructor to initiate a new SCClassInstance with a different name but the same internal state
     * EXCEPT: all linked objects will be set to null to avoid changes by accident
     *
     * @param old SCClassInstance which will be copied
     * @param newName new name of the variable
     */
    public SCClassInstance(SCClassInstance old, String newName) {
        super(old, newName);
        this.scclass = null;
        this.outerClass = null;
        this.portSocketInstances = null;
    }
    
    
    public SCClass getSCClass() {
        return this.scclass;
    }
    
    public void setSCClass(SCClass scclass) {
        if (!this.scclass.removeInstance(this)) {
            logger.error("Error changing SCClass of instance. It has not been an instance of its previous class.");
        }
        this.scclass = scclass;
        this.scclass.addInstance(this);
    }
    
    /**
     * Returns the outer class of this instance. A class instance has an outer class if it is a member
     * of a class.
     * 
     * @return
     */
    public SCClass getOuterClass() {
        return this.outerClass;
    }
    
    public void setOuterClass(SCClass outerClass) {
        this.outerClass = outerClass;
    }
    
    public List<SCConnectionInterface> getPortSocketInstances() {
        return this.portSocketInstances;
    }
    
    /**
     * Returns the portsocketinstance with the corresponding name or null, if no portsocket instance
     * with this name exists.
     * 
     * @param name
     * @return
     */
    public SCConnectionInterface getPortSocketInstanceByName(String name) {
        for (SCConnectionInterface psi : this.portSocketInstances) {
            if (psi.getName().equals(name)) {
                return psi;
            }
        }
        return null;
    }
    
    /**
     * Adds the submitted portsocketinstance to the list of portsocketinstances if it is not already
     * part of the list. Returns true if the instance was added during this function call, false if not.
     * 
     * @param psi
     * @return
     */
    public boolean addPortSocketInstance(SCConnectionInterface psi) {
        if (!this.portSocketInstances.contains(psi)) {
            this.portSocketInstances.add(psi);
            return true;
        }
        return false;
    }
    
    /**
     * Sets the PortSocketInstances
     *
     * @param portInstances the instances to Set
     */
    public void setPortSocketInstances(List<SCConnectionInterface> portInstances) {
        this.portSocketInstances = portInstances;
    }
    
    /**
     * Returns the label of the instance.
     * 
     * @return The label of the instance.
     */
    public String getInstanceLabel() {
        return this.instanceLabel;
    }
    
    public void setInstanceLabel(String instanceLabel) {
        this.instanceLabel = instanceLabel;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((this.portSocketInstances == null) ? 0 : this.portSocketInstances.hashCode());
        result = prime * result + ((this.scclass == null) ? 0 : this.scclass.toString().hashCode());
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
        if (!(obj instanceof SCClassInstance other)) {
            return false;
        }
        if (this.portSocketInstances == null) {
            if (other.portSocketInstances != null) {
                return false;
            }
        } else if (!this.portSocketInstances.equals(other.portSocketInstances)) {
            return false;
        }
        if (this.scclass == null) {
            if (other.scclass != null) {
                return false;
            }
        } else if (!this.scclass.toString().equals(other.scclass.toString())) {
            return false;
        }
        return true;
    }
    
    @Override
    public String toString() {
        // SCClassInstances are printed the same way as normal variables are
        // declared
        return super.getDeclarationString() + ";";
    }
    
    @Override
    public String getInitializationString() {
        if (hasInitialValue()) {
            StringBuffer out = new StringBuffer(" = ");
            
            out.append(getType() + "(");
            for (Expression exp : this.declaration.getInitialValues()) {
                out.append(exp.toString() + ", ");
            }
            out.setLength(out.length() - 2);
            out.append(")");
            return out.toString();
        } else {
            return "";
        }
    }
    
    @Override
    public String getType() {
        if (this.scclass != null) {
            return this.scclass.getName();
        } else {
            return this.type;
        }
    }
    
    // just for convenience
    @Override
    public boolean isSCModule() {
        return this.scclass.isSCModule();
    }
    
    @Override
    public boolean isNotSCModule() {
        return !isSCModule();
    }
    
    public boolean isSCKnownType() {
        return (this instanceof SCKnownType);
    }
    
    @Override
    public boolean isChannel() {
        return this.scclass.isChannel();
    }
    
    @Override
    public boolean isNotChannel() {
        return !isChannel();
    }
    
    /**
     * Returns all processnames which needs initialization. The processes are identified by a list
     * containing the instances they are part of. Example: if a process consume is part of a
     * moduleinstance c_inst, the list will contain <c_inst, consume>. If there is a deeper hierarchy,
     * this will also be reflected in the list. The name of the process is always the last element of
     * each list.
     * 
     * @param predecessors the module instance hierarchy
     * @return
     */
    public List<List<String>> getProcessNamesToInitialize(LinkedList<String> predecessors) {
        LinkedList<List<String>> ret = new LinkedList<>();
        LinkedList<String> newPred = (LinkedList<String>) predecessors.clone();
        newPred.add(this.name);
        for (SCProcess p : this.scclass.getProcesses()) {
            if (!p.getModifier().contains(SCMODIFIER.DONTINITIALIZE)) {
                LinkedList<String> clone = (LinkedList<String>) newPred.clone();
                clone.add(p.getName());
                ret.add(clone);
            }
        }
        
        for (SCVariable member : this.scclass.getMembers()) {
            if (member instanceof SCClassInstance) {
                ret.addAll(((SCClassInstance) member).getProcessNamesToInitialize(newPred));
            }
        }
        if (this.scclass.getConstructor() != null) {
            for (SCVariable member : this.scclass.getConstructor().getLocalVariables()) {
                if (member instanceof SCClassInstance) {
                    ret.addAll(((SCClassInstance) member).getProcessNamesToInitialize(newPred));
                }
            }
        }
        
        return ret;
    }
    
    /**
     * initiates a new SCClassInstance with a different name but the same internal state EXCEPT: all
     * linked objects will be set to null to avoid changes by accident
     *
     * @param newName new name of the variable
     * @return new SCClassInstance
     */
    @Override
    public SCVariable flatCopyVariableWitNewName(String newName) {
        return new SCClassInstance(this, newName);
    }
    
}
