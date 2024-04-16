package raid24contribution.sc_model;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import raid24contribution.sc_model.variables.SCArray;
import raid24contribution.sc_model.variables.SCClassInstance;
import raid24contribution.sc_model.variables.SCEvent;

/**
 * Represents all classes (and structs) of the SystemC model. We do not differentiate between
 * structs and scmodules. Instead all classes can have ports and sockets. This eases handling of
 * structs and modules.
 * 
 */
public class SCClass implements Serializable, Cloneable {
    
    private static final long serialVersionUID = 8483500264938392358L;
    
    /**
     * name of the SCClass (e.g. data)
     */
    protected String name = "";
    
    /**
     * list of variable which represent the members of the SCClass
     */
    protected List<SCVariable> members = null;
    
    /**
     * list of SCFunctions which represent the memberfunctions of the SCClass
     */
    protected List<SCFunction> memberFunctions = null;
    
    /**
     * a reference to the constructor-function, null if there is no constructor
     */
    protected SCFunction constructor = null;
    
    /**
     * list of SCStructs from which this struct inherits
     */
    protected List<SCClass> inheritFrom;
    
    /**
     * Determines if the struct is a channel (not NO_CHANNEL), and what kind of channel.
     */
    protected SCCHANNELTYPE channelType;
    
    /**
     * This flag describes whether the given SCClass is defined in an external source (true) or in the
     * global xml file. This is needed to prevent multiple parsing runs for the same SCClass.
     */
    protected boolean isExternal;
    
    /**
     * All instances of SCClasses the class contains. This list is only for convenience as all instances
     * are also members of the SCClass.
     */
    protected List<SCClassInstance> instances;
    
    /**
     * All ports and sockets of the SCClass. Only SC_MODULEs can have ports and sockets, so this list is
     * empty for a standard C++ class.
     */
    protected List<SCPort> portsSockets;
    
    /**
     * All processes of the SCClass. Only SC_MODULEs can have ports and sockets, so this list is empty
     * for a standard C++ class.
     */
    protected List<SCProcess> processes;
    
    /**
     * All ports and sockets of the SCClass. Only SC_MODULEs can have ports and sockets, so this list is
     * empty for a standard C++ class.
     */
    protected List<SCEvent> events;
    
    public SCClass(String name) {
        this.name = name;
        this.channelType = SCCHANNELTYPE.NO_CHANNEL;
        this.constructor = null;
        this.events = new LinkedList<>();
        this.inheritFrom = new LinkedList<>();
        this.instances = new LinkedList<>();
        this.isExternal = false;
        this.memberFunctions = new LinkedList<>();
        this.members = new LinkedList<>();
        this.portsSockets = new LinkedList<>();
        this.processes = new LinkedList<>();
    }
    
    public String getName() {
        return this.name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public List<SCVariable> getMembers() {
        return this.members;
    }
    
    /**
     * Returns the member with the specified name or null if no member with the name was found. As we do
     * not support shadowing of variables all variable names should be unique and therefore there exist
     * only one variable with the specified name per class.
     * 
     * @param name
     * @return
     */
    public SCVariable getMemberByName(String name) {
        for (SCVariable var : this.members) {
            if (var.getName().equals(name)) {
                return var;
            }
        }
        return null;
    }
    
    /**
     * Adds the specified variable to the list of members if it is not already part of the list. Returns
     * true if the variable was added during this function call, false if not.
     * 
     * @param memberFunction
     * @return
     */
    public boolean addMember(SCVariable member) {
        if (!this.members.contains(member)) {
            this.members.add(member);
            return true;
        }
        return false;
    }
    
    public void setMembers(List<SCVariable> members) {
        this.members = members;
    }
    
    public List<SCFunction> getMemberFunctions() {
        return this.memberFunctions;
    }
    
    /**
     * Returns the member function with the specified name or null if no function with the specified
     * name was found. As we do not support overloading all function names should be unique per module
     * and therefore we can return the first occurence of the name.
     * 
     * @param name
     * @return
     */
    public SCFunction getMemberFunctionByName(String name) {
        for (SCFunction fun : this.memberFunctions) {
            if (fun.getName().equals(name)) {
                return fun;
            }
        }
        
        return null;
    }
    
    /**
     * Adds the specified function to the list of member functions if it is not already part of the
     * list. Returns true if the function was added during this function call, false if not.
     * 
     * @param memberFunction
     * @return
     */
    public boolean addMemberFunction(SCFunction memberFunction) {
        if (!this.memberFunctions.contains(memberFunction)) {
            this.memberFunctions.add(memberFunction);
            memberFunction.setSCClass(this);
            return true;
        }
        return false;
    }
    
    public void setMemberFunctions(List<SCFunction> memberFunctions) {
        this.memberFunctions = memberFunctions;
    }
    
    public SCFunction getConstructor() {
        return this.constructor;
    }
    
    public void setConstructor(SCFunction constructor) {
        this.constructor = constructor;
    }
    
    public List<SCClass> getInheritFrom() {
        return this.inheritFrom;
    }
    
    public void setInheritFrom(List<SCClass> inheritFrom) {
        this.inheritFrom = inheritFrom;
    }
    
    public void addInheritFrom(SCClass superClass) {
        this.inheritFrom.add(superClass);
    }
    
    public SCCHANNELTYPE getChannelType() {
        return this.channelType;
    }
    
    public void setChannelType(SCCHANNELTYPE channelType) {
        this.channelType = channelType;
    }
    
    public boolean isExternal() {
        return this.isExternal;
    }
    
    public void setExternal(boolean isExternal) {
        this.isExternal = isExternal;
    }
    
    public List<SCClassInstance> getInstances() {
        return this.instances;
    }
    
    /**
     * Returns the class instance with the specified name or null, if no class instance with this name
     * exists.
     * 
     * @param name
     * @return
     */
    public SCClassInstance getInstanceByName(String name) {
        for (SCClassInstance ci : this.instances) {
            if (ci.getName().equals(name)) {
                return ci;
            }
        }
        
        return null;
    }
    
    public void addInstance(SCClassInstance instance) {
        this.instances.add(instance);
    }
    
    public boolean removeInstance(SCClassInstance instance) {
        return this.instances.remove(instance);
    }
    
    public List<SCPort> getPortsSockets() {
        return this.portsSockets;
    }
    
    public SCPort getPortSocketByName(String name) {
        for (SCPort ps : this.portsSockets) {
            if (ps.getName().equals(name)) {
                return ps;
            }
        }
        return null;
    }
    
    public void setPortsSockets(List<SCPort> portsSockets) {
        this.portsSockets = portsSockets;
    }
    
    /**
     * Adds the specified port or socket to the list of known ports and sockets if it is not already
     * part of the list. Returns true if the port or socket was added to the list during this function
     * call, false if not.
     * 
     * @param portSocket
     * @return
     */
    public boolean addPortSocket(SCPort portSocket) {
        if (!this.portsSockets.contains(portSocket)) {
            this.portsSockets.add(portSocket);
            return true;
        }
        return false;
    }
    
    public List<SCProcess> getProcesses() {
        return this.processes;
    }
    
    public void setProcesses(List<SCProcess> processes) {
        this.processes = processes;
    }
    
    /**
     * Adds the specified process to the list of known processes if it is not already part of it.
     * Returns true of the process was added to the list during this function call, false if not.
     * 
     * @param process
     * @return
     */
    public boolean addProcess(SCProcess process) {
        if (!this.processes.contains(process)) {
            this.processes.add(process);
            return true;
        }
        return false;
    }
    
    public List<SCEvent> getEvents() {
        return this.events;
    }
    
    /**
     * Returns the event with the specified name if it exists or null if not. As we do not support
     * shadowing of variables all events should have a unique name per module.
     * 
     * @param name
     * @return
     */
    public SCEvent getEventByName(String name) {
        for (SCEvent ev : this.events) {
            if (ev.getName().equals(name)) {
                return ev;
            }
        }
        
        return null;
    }
    
    public void setEvents(List<SCEvent> events) {
        this.events = events;
    }
    
    /**
     * Adds the specified event to the list of events if it is not already part of the list. Returns
     * true if the event was added during this function call, false if not.
     * 
     * @param event
     * @return
     */
    public boolean addEvent(SCEvent event) {
        if (!this.events.contains(event)) {
            this.events.add(event);
            return true;
        }
        return false;
    }
    
    /**
     * Makes the SCCLass a hierarchical channel. As it is possible that a struct inherits from
     * sc_prim_channel and sc_interface and is therefore a primitive channel this method will not do
     * anything if the struct is already a primitive channel.
     */
    public void setHierarchicalChannel() {
        if (this.channelType != SCCHANNELTYPE.PRIMITIVE_CHANNEL) {
            this.channelType = SCCHANNELTYPE.HIERARCHICAL_CHANNEL;
        }
    }
    
    /**
     * Makes the SCClass a primitive channel. This method blocks the use of the setHierarchicalChannel()
     * method.
     */
    public void setPrimitiveChannel() {
        this.channelType = SCCHANNELTYPE.PRIMITIVE_CHANNEL;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.channelType == null) ? 0 : this.channelType.hashCode());
        result = prime * result + ((this.constructor == null) ? 0 : this.constructor.hashCode());
        result = prime * result + ((this.events == null) ? 0 : this.events.hashCode());
        result = prime * result + ((this.inheritFrom == null) ? 0 : this.inheritFrom.hashCode());
        result = prime * result + ((this.instances == null) ? 0 : this.instances.hashCode());
        result = prime * result + (this.isExternal ? 1231 : 1237);
        result = prime * result + ((this.memberFunctions == null) ? 0 : this.memberFunctions.hashCode());
        result = prime * result + ((this.members == null) ? 0 : this.members.hashCode());
        result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
        result = prime * result + ((this.portsSockets == null) ? 0 : this.portsSockets.hashCode());
        result = prime * result + ((this.processes == null) ? 0 : this.processes.hashCode());
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
        if (!(obj instanceof SCClass other)) {
            return false;
        }
        if (this.channelType != other.channelType) {
            return false;
        }
        if (this.constructor == null) {
            if (other.constructor != null) {
                return false;
            }
        } else if (!this.constructor.equals(other.constructor)) {
            return false;
        }
        if (this.events == null) {
            if (other.events != null) {
                return false;
            }
        } else if (!this.events.equals(other.events)) {
            return false;
        }
        if (this.inheritFrom == null) {
            if (other.inheritFrom != null) {
                return false;
            }
        } else if (!this.inheritFrom.equals(other.inheritFrom)) {
            return false;
        }
        if (this.instances == null) {
            if (other.instances != null) {
                return false;
            }
        } else if (!this.instances.equals(other.instances)) {
            return false;
        }
        if (this.isExternal != other.isExternal) {
            return false;
        }
        if (this.memberFunctions == null) {
            if (other.memberFunctions != null) {
                return false;
            }
        } else if (!this.memberFunctions.equals(other.memberFunctions)) {
            return false;
        }
        if (this.members == null) {
            if (other.members != null) {
                return false;
            }
        } else if (!this.members.equals(other.members)) {
            return false;
        }
        if (this.name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!this.name.equals(other.name)) {
            return false;
        }
        if (this.portsSockets == null) {
            if (other.portsSockets != null) {
                return false;
            }
        } else if (!this.portsSockets.equals(other.portsSockets)) {
            return false;
        }
        if (this.processes == null) {
            if (other.processes != null) {
                return false;
            }
        } else if (!this.processes.equals(other.processes)) {
            return false;
        }
        return true;
    }
    
    /**
     * Returns the number of processes of this class which need initialization (= the *dont_initialize*
     * flag is not set). This is done recursively for all members, which are class instances.
     * 
     * @return
     */
    public int countInitProcesses() {
        int count = 0;
        for (SCProcess pro : this.processes) {
            if (!pro.getModifier().contains(SCMODIFIER.DONTINITIALIZE)) {
                count++;
            }
        }
        for (SCVariable member : getMembers()) {
            if (member instanceof SCClassInstance) {
                count += ((SCClassInstance) member).getSCClass().countInitProcesses();
            }
        }
        if (this.constructor != null) {
            for (SCVariable member : this.constructor.getLocalVariables()) {
                if (member instanceof SCClassInstance) {
                    count += ((SCClassInstance) member).getSCClass().countInitProcesses();
                }
            }
        }
        return count;
    }
    
    @Override
    public String toString() {
        String ret = "struct " + this.name + " ";
        if (!this.inheritFrom.isEmpty()) {
            ret += ": ";
            for (SCClass in : this.inheritFrom) {
                ret += in.getName() + ", ";
            }
            ret = ret.substring(0, ret.lastIndexOf(", ")) + " ";
        }
        ret += "{\n";
        for (SCPort ps : this.portsSockets) {
            ret += ps.toString() + "\n";
        }
        for (SCVariable var : this.members) {
            ret += var.toString() + "\n";
        }
        for (SCFunction fun : this.memberFunctions) {
            ret += fun.toString() + "\n";
        }
        ret += "}";
        
        return ret;
        
    }
    
    /**
     * Returns a string representation of the sc_class containing only the name of the class and its
     * members but neither sockets nor functions.
     * 
     * @return
     */
    public String toStringWithoutFunctions() {
        String ret = "struct " + this.name + "{\n";
        for (SCVariable variable : getMembers()) {
            if (!(variable instanceof SCArray)) {
                ret += variable.getDeclarationString() + ";\n";
            }
        }
        
        ret += "};";
        return ret;
    }
    
    /**
     * Returns true if the class is a sc_module. Classes are sc_modules if they inherit from sc_module,
     * directly or indirectly (e.g. by inheriting from a class which inherits from sc_module).
     * 
     * @return
     */
    public boolean isSCModule() {
        for (SCClass cl : this.inheritFrom) {
            if (cl.getName().equals("sc_module") || cl.isSCModule()) {
                return true;
            }
        }
        return false;
    }
    
    public boolean isPrimitiveChannel() {
        return this.channelType == SCCHANNELTYPE.PRIMITIVE_CHANNEL;
    }
    
    public boolean isHirarchicalChannel() {
        return this.channelType == SCCHANNELTYPE.HIERARCHICAL_CHANNEL;
    }
    
    public boolean isChannel() {
        return this.channelType != SCCHANNELTYPE.NO_CHANNEL;
    }
    
    public boolean hasInstances() {
        return !this.instances.isEmpty();
    }
    
    public String toStringWithoutFunctionsWithArrays() {
        String ret = "struct " + this.name + "{\n";
        for (SCVariable variable : getMembers()) {
            ret += variable.getDeclarationString() + ";\n";
        }
        ret += "};";
        return ret;
    }
    
    public SCClass createClone() {
        try {
            return (SCClass) clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }
    
}
