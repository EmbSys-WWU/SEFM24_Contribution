package raid24contribution.sc_model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import raid24contribution.sc_model.expressions.FunctionCallExpression;
import raid24contribution.sc_model.variables.SCClassInstance;

/**
 * this class represents an instance of a port or socket an instance is symbolized by the port and
 * an Instance of a Module it also has lists of connected Channelsm in case of a port, or a list of
 * other portsocketinstances in case of a socket
 * 
 */
public class SCSocketInstance implements SCConnectionInterface, Serializable {
    
    private static final long serialVersionUID = -3854123608062278089L;
    
    /**
     * name of the socket instance
     */
    protected String name;
    /**
     * reference to the socket
     */
    protected SCSocket socket;
    /**
     * owner of the socket, an instance of the module where the socket was declared
     */
    protected SCClassInstance owner;
    
    /**
     * ModuleInstance where the socket functions are found, usual the owner
     */
    protected SCClass socketFunctionLocation;
    /**
     * List of functions called from this socket;
     */
    protected List<FunctionCallExpression> unresolvedFunctionCalls;
    
    /**
     * list of socket instances where the socket is connected to
     */
    protected List<SCSocketInstance> connectedSocketinstances;
    
    /**
     * creates a new socket
     * 
     * @param nam name of the socket
     * @param p original socket
     */
    public SCSocketInstance(String nam, SCSocket p) {
        this.name = nam;
        this.owner = null;
        this.socket = p;
        this.socketFunctionLocation = null;
        this.unresolvedFunctionCalls = new ArrayList<>();
        this.connectedSocketinstances = new ArrayList<>();
    }
    
    /**
     * return the name of the socket instance
     * 
     * @return String
     */
    @Override
    public String getName() {
        return this.name;
    }
    
    /**
     * return the original socket
     * 
     * @return SCSocket
     */
    @Override
    public SCPort getPortSocket() {
        return this.socket;
    }
    
    /**
     * returns the owner of the socket instance
     * 
     * @return SCClassInstance
     */
    @Override
    public SCClassInstance getOwner() {
        return this.owner;
    }
    
    /**
     * sets the owner of the socket instance
     * 
     * @param own
     */
    @Override
    public void addOwner(SCClassInstance own) {
        this.owner = own;
    }
    
    /**
     * Sets the submitted class instance as the location for all socket methods. A call to a socket
     * method is always forwarded to this class instance. WARNING: once set the instance is immutable
     * and the method returns false if the setting fails.
     * 
     * @param loc
     * @return
     */
    public boolean setSocketFunctionLocation(SCClass loc) {
        if (this.socketFunctionLocation != null) {
            return false;
        } else {
            this.socketFunctionLocation = loc;
            // resolve unresolved function calls
            if (!this.connectedSocketinstances.isEmpty()) {
                for (SCSocketInstance psi : this.connectedSocketinstances) {
                    for (SCFunction supportedFunction : loc.getMemberFunctions()) {
                        psi.resolveFunctionCall(supportedFunction);
                    }
                }
            }
            return true;
        }
    }
    
    /**
     * Adds a function call to the list of unresolved calls called from this socket instance and tries
     * to resolve them.
     * 
     * @param name
     * @param fce
     */
    public void addCalledFunction(String name, FunctionCallExpression fce) {
        // lookup called function in connected socktes
        boolean resolved = false;
        if (!this.connectedSocketinstances.isEmpty()) {
            for (SCSocketInstance psi : this.connectedSocketinstances) {
                if (psi.getSocketFunctionLocation() != null) {
                    for (SCFunction targetScf : psi.getSocketFunctionLocation().getMemberFunctions()) {
                        if (targetScf.getName().equals(name)) {
                            fce.setFunction(targetScf);
                            targetScf.setIsCalled(true);
                            // we currently only support 1:1 connections
                            resolved = true;
                            break;
                        }
                    }
                }
            }
        }
        // else put in table
        if (!resolved) {
            this.unresolvedFunctionCalls.add(fce);
        }
    }
    
    /**
     * Returns the list of unresolved function calls
     * 
     * @return
     */
    public List<FunctionCallExpression> getUnresolvedFunctionCalls() {
        return this.unresolvedFunctionCalls;
    }
    
    /**
     * Tries to resolve a function call by setting the called function in all matching unresolved
     * function calls.
     * 
     * @param scf
     */
    public void resolveFunctionCall(SCFunction scf) {
        List<FunctionCallExpression> resolved = new LinkedList<>();
        for (FunctionCallExpression unresFce : this.unresolvedFunctionCalls) {
            if (unresFce.getFunction().getName().equals(scf.getName())) {
                unresFce.setFunction(scf);
                resolved.add(unresFce);
            }
        }
        this.unresolvedFunctionCalls.removeAll(resolved);
    }
    
    /**
     * adds an SocketInstance to the List of connected SocketInstances, if their isn't one with the same
     * name
     * 
     * @param siToAdd the SocketInstance which should be added
     * @return true if the SocketInstance have been added, false if not
     */
    @Override
    public boolean addPortSocketInstance(SCConnectionInterface connIf) {
        if (connIf instanceof SCSocketInstance siToAdd) {
            if (this.connectedSocketinstances.contains(siToAdd)) {
                return false;
            } else {
                this.connectedSocketinstances.add(siToAdd);
                // resolve function calls in newly connected psi
                if (this.socketFunctionLocation != null) {
                    for (SCFunction supportedFunction : this.socketFunctionLocation.getMemberFunctions()) {
                        siToAdd.resolveFunctionCall(supportedFunction);
                    }
                }
                
                return true;
            }
        } else {
            return false;
        }
    }
    
    /**
     * return the module-instance with the right name
     * 
     * @param mdl_nam name of the module-instance
     * @return SCModuleInstance
     */
    public SCClass getSocketFunctionLocation() {
        return this.socketFunctionLocation;
    }
    
    @Override
    public SCSocketInstance getPortSocketInstance(String psi_nam) {
        for (SCSocketInstance p : this.connectedSocketinstances) {
            if (p.getName().equals(psi_nam)) {
                return p;
            }
        }
        return null;
    }
    
    /**
     * returns the list of connected PortSocketInstances
     * 
     * @return List<SCPortSCSocketInstance>
     */
    public List<SCSocketInstance> getPortSocketInstances() {
        return this.connectedSocketinstances;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
        result = prime * result + ((this.owner == null) ? 0 : this.owner.toString().hashCode());
        result = prime * result + ((this.socket == null) ? 0 : this.socket.hashCode());
        result = prime * result
                + ((this.socketFunctionLocation == null) ? 0 : this.socketFunctionLocation.toString().hashCode());
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
        SCSocketInstance other = (SCSocketInstance) obj;
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
        if (this.socket == null) {
            if (other.socket != null) {
                return false;
            }
        } else if (!this.socket.equals(other.socket)) {
            return false;
        }
        if (this.socketFunctionLocation == null) {
            if (other.socketFunctionLocation != null) {
                return false;
            }
        } else if (!this.socketFunctionLocation.toString().equals(other.socketFunctionLocation.toString())) {
            return false;
        }
        return true;
    }
    
    @Override
    public String toString() {
        return this.name + ": " + this.socket.toString();
    }
    
}
