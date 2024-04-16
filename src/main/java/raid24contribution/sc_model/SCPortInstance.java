package raid24contribution.sc_model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import raid24contribution.sc_model.variables.SCClassInstance;
import raid24contribution.sc_model.variables.SCKnownType;

/**
 * this class represents an instance of a port or socket an instance is symbolized by the port and
 * an Instance of a Module it also has lists of connected Channelsm in case of a port, or a list of
 * other portsocketinstances in case of a socket
 * 
 */
public class SCPortInstance implements SCConnectionInterface, Serializable {
    
    private static final long serialVersionUID = -3854123608062278089L;
    
    /**
     * name of the port instance
     */
    protected String name;
    /**
     * reference to the port
     */
    protected SCPort port;
    /**
     * owner of the port, an instance of the module where the port was declared
     */
    protected SCClassInstance owner;
    
    /**
     * list of channels where the port or socket is connected to
     */
    protected List<SCKnownType> connectedChannels;
    
    /**
     * list of instances, the port is connected to. Contains all self defined channels
     */
    protected List<SCClassInstance> connectedClassInstances;
    
    /**
     * list of port instances where the port is connected to
     */
    protected List<SCPortInstance> connectedPortinstances;
    
    /**
     * creates a new port instance
     * 
     * @param nam name of the port
     * @param p original port
     */
    public SCPortInstance(String nam, SCPort p) {
        this.name = nam;
        this.owner = null;
        this.port = p;
        this.connectedChannels = new ArrayList<>();
        this.connectedPortinstances = new ArrayList<>();
        this.connectedClassInstances = new ArrayList<>();
    }
    
    /**
     * return the name of the port instance
     * 
     * @return String
     */
    @Override
    public String getName() {
        return this.name;
    }
    
    /**
     * return the original port
     * 
     * @return SCPort
     */
    @Override
    public SCPort getPortSocket() {
        return this.port;
    }
    
    /**
     * returns the owner of the port instance
     * 
     * @return SCClassInstance
     */
    @Override
    public SCClassInstance getOwner() {
        return this.owner;
    }
    
    /**
     * sets the owner of the port instance
     * 
     * @param own
     */
    @Override
    public void addOwner(SCClassInstance own) {
        this.owner = own;
    }
    
    /**
     * adds a class instance to the connected List
     * 
     * @param mdlToAdd
     * @return true if it was added, false if not
     */
    public boolean addChannel(SCKnownType chnToAdd) {
        if (existChannel(chnToAdd)) {
            return false;
        } else {
            this.connectedChannels.add(chnToAdd);
            return true;
        }
    }
    
    /**
     * adds an PortInstance to the List of connected PortInstances, if there isn't one with the same
     * name already
     * 
     * @param piToAdd the PortInstance which should be added
     * @return true if the PortInstance have been added, false if not
     */
    @Override
    public boolean addPortSocketInstance(SCConnectionInterface connIf) {
        if (connIf instanceof SCPortInstance piToAdd) {
            if (this.connectedPortinstances.contains(piToAdd)) {
                return false;
            } else {
                this.connectedPortinstances.add(piToAdd);
                return true;
            }
        } else {
            return false;
        }
    }
    
    /**
     * adds an ModuleInstance to the List of connected ModuleInstances, if it is not already added.
     * 
     * @param inst the ModuleInstance which should be added
     * @return true if the ModuleInstance have been added, false if not
     */
    public boolean addInstanceConnection(SCClassInstance inst) {
        if (this.connectedClassInstances.contains(inst)) {
            return false;
        } else {
            this.connectedClassInstances.add(inst);
            return true;
        }
    }
    
    /**
     * return the instance with the right name
     * 
     * @param name name of the instance
     * @return SCKnownType
     */
    public SCKnownType getChannel(String name) {
        for (SCKnownType p : this.connectedChannels) {
            if (p.getName().equals(name)) {
                return p;
            }
        }
        return null;
    }
    
    @Override
    public SCPortInstance getPortSocketInstance(String psi_nam) {
        for (SCPortInstance p : this.connectedPortinstances) {
            if (p.getName().equals(psi_nam)) {
                return p;
            }
        }
        return null;
    }
    
    /**
     * return the list of module instances where this port or socket is connected to
     * 
     * @return List<SCModuleInstance>
     */
    public List<SCKnownType> getChannels() {
        return this.connectedChannels;
    }
    
    /**
     * returns the list of connected PortInstances
     * 
     * @return List<SCPortSCSocketInstance>
     */
    public List<SCPortInstance> getPortSocketInstances() {
        return this.connectedPortinstances;
    }
    
    /**
     * returns the list of connected ModuleInstances
     * 
     * @return List<SCModuleInstance>
     */
    public List<SCClassInstance> getModuleInstances() {
        return this.connectedClassInstances;
    }
    
    /**
     * checks if a module-instance already exists in the list
     * 
     * @param mdl module which has to be checked
     * @return true if it exists, false if not
     */
    public boolean existChannel(SCKnownType mdl) {
        for (SCKnownType m : this.connectedChannels) {
            if (m.getName().equals(mdl.getName())) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
        result = prime * result + ((this.owner == null) ? 0 : this.owner.toString().hashCode());
        result = prime * result + ((this.port == null) ? 0 : this.port.hashCode());
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
        SCPortInstance other = (SCPortInstance) obj;
        if (this.name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!this.name.equals(other.name)) {
            return false;
        }
        if (this.owner == null) {
            if (other.owner != null) {
                return false;
            }
        } else if (!this.owner.toString().equals(other.owner.toString())) {
            return false;
        }
        if (this.port == null) {
            if (other.port != null) {
                return false;
            }
        } else if (!this.port.equals(other.port)) {
            return false;
        }
        return true;
    }
    
    @Override
    public String toString() {
        return this.name + ": " + this.port.toString();
    }
    
}
