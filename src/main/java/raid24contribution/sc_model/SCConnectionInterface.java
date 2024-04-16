package raid24contribution.sc_model;

import raid24contribution.sc_model.variables.SCClassInstance;

public interface SCConnectionInterface {
    
    /**
     * return the name of the port- or socket-instance
     * 
     * @return String
     */
    public String getName();
    
    /**
     * return the orginalport or orginalsocket
     * 
     * @return SCPortSCSocket
     */
    public SCPort getPortSocket();
    
    /**
     * returns the owner of the port-socket-instance
     * 
     * @return SCModuleInstance
     */
    public SCClassInstance getOwner();
    
    /**
     * sets the owner of the port-socket-instance
     * 
     * @param own
     */
    public void addOwner(SCClassInstance own);
    
    /**
     * adds an PortSocketInstance to the List of connected PortSocketInstances, if their isn't one with
     * the same name
     * 
     * @param psiToAdd the PortSocketInstance which should be added
     * @return true if the PortSocketInstance have been added, false if not
     */
    public boolean addPortSocketInstance(SCConnectionInterface psiToAdd);
    
    public SCConnectionInterface getPortSocketInstance(String psi_nam);
    
}
