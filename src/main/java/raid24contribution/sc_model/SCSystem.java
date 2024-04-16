package raid24contribution.sc_model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import raid24contribution.sc_model.variables.SCClassInstance;

public class SCSystem implements Serializable {
    
    private static final long serialVersionUID = 2100483276453723964L;
    
    protected List<SCClassInstance> instances;
    protected List<SCConnectionInterface> portSocketInstances;
    protected List<SCVariable> globalVariables;
    protected List<SCFunction> globalFunctions;
    protected List<SCClass> classes;
    protected List<SCEnumType> enumTypes;
    
    /**
     * creates a new system
     */
    public SCSystem() {
        this.instances = new ArrayList<>();
        this.portSocketInstances = new ArrayList<>();
        this.globalVariables = new ArrayList<>();
        this.globalFunctions = new ArrayList<>();
        this.classes = new ArrayList<>();
        this.enumTypes = new ArrayList<>();
    }
    
    public boolean addPortSocketInstance(SCConnectionInterface psiToAdd) {
        if (this.portSocketInstances.contains(psiToAdd)) {
            return false;
        } else {
            this.portSocketInstances.add(psiToAdd);
            return true;
        }
    }
    
    public SCConnectionInterface getPortSocketInstanceByName(String name) {
        for (SCConnectionInterface p : this.portSocketInstances) {
            if (p.getName().equals(name)) {
                return p;
            }
        }
        return null;
    }
    
    public List<SCConnectionInterface> getPortSocketInstances(SCPort ps) {
        List<SCConnectionInterface> psiInstances = new ArrayList<>();
        for (SCConnectionInterface p : this.portSocketInstances) {
            // not using equals here
            if (p.getPortSocket() == ps) {
                psiInstances.add(p);
            }
        }
        return psiInstances;
    }
    
    public List<SCConnectionInterface> getPortSocketInstances() {
        return this.portSocketInstances;
    }
    
    public boolean addInstance(SCClassInstance inst) {
        if (this.instances.contains(inst)) {
            return false;
        } else {
            this.instances.add(inst);
            // XXX: only real instances get here. This way we won't have all the
            // 'this' instances in out scclasses...
            inst.getSCClass().addInstance(inst);
            return true;
        }
    }
    
    /**
     * Returns the instance with the specified name or null if no instance with this name exists.
     * 
     * @param name
     * @return
     */
    public SCClassInstance getInstanceByName(String name) {
        for (SCClassInstance p : this.instances) {
            if (p.getName().equals(name)) {
                return p;
            }
        }
        return null;
    }
    
    public void addClass(SCClass c) {
        if (!this.classes.contains(c)) {
            this.classes.add(c);
        }
    }
    
    public List<SCClassInstance> getInstances() {
        return this.instances;
    }
    
    public void setInstances(List<SCClassInstance> instancesList) {
        this.instances = instancesList;
    }
    
    public List<SCClass> getClasses() {
        return this.classes;
    }
    
    /**
     * Returns the class with the specified name or null if no such class exists.
     * 
     * @param name
     * @return
     */
    public SCClass getClassByName(String name) {
        for (SCClass cl : this.classes) {
            if (cl.getName().equals(name)) {
                return cl;
            }
        }
        return null;
    }
    
    public boolean addGlobalFunction(SCFunction fctToAdd) {
        if (this.globalFunctions.contains(fctToAdd)) {
            return false;
        } else {
            this.globalFunctions.add(fctToAdd);
            return true;
        }
    }
    
    public boolean addGlobalFunctions(List<SCFunction> fctsToAdd) {
        boolean allAdded = true;
        for (SCFunction fct : fctsToAdd) {
            allAdded &= addGlobalFunction(fct);
        }
        return allAdded;
    }
    
    public SCVariable getGlobalVariables(String var_nam) {
        for (SCVariable v : this.globalVariables) {
            if (v.getName().equals(var_nam)) {
                return v;
            }
        }
        return null;
    }
    
    public List<SCVariable> getGlobalVariables() {
        return this.globalVariables;
    }
    
    public boolean addGlobalVariable(SCVariable varToAdd) {
        if (this.globalVariables.contains(varToAdd)) {
            return false;
        } else {
            this.globalVariables.add(varToAdd);
            return true;
        }
    }
    
    public SCFunction getGlobalFunction(String fct_nam) {
        for (SCFunction f : this.globalFunctions) {
            if (f.getName().equals(fct_nam)) {
                return f;
            }
        }
        return null;
    }
    
    public List<SCFunction> getGlobalFunctions() {
        return this.globalFunctions;
    }
    
    public void addEnumType(SCEnumType et) {
        if (!this.enumTypes.contains(et)) {
            this.enumTypes.add(et);
        }
    }
    
    public List<SCEnumType> getEnumTypes() {
        return this.enumTypes;
    }
    
    
    /**
     * Stores the SCSystem (and all underlying modules etc) to the specified file. If the file does not
     * exist it will be created. If it already exists, it will be overwritten.
     * 
     * @param path - absolute path to the file. If
     */
    public void store(String path) {
        // delete an already existing file.
        File f = new File(path);
        if (f.exists()) {
            f.delete();
        }
        
        try {
            FileOutputStream fileOut = new FileOutputStream(f);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(this);
            out.close();
            fileOut.close();
            
        } catch (IOException i) {
            i.printStackTrace();
        }
        
    }
    
    /**
     * Loads the specified SCSystem (and all underlying modules etc) from the specified file.
     * 
     * @param path - absolute path to .syscir file.
     */
    public static SCSystem load(String path) {
        try {
            FileInputStream fileIn = new FileInputStream(path);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            SCSystem s = (SCSystem) in.readObject();
            in.close();
            fileIn.close();
            return s;
            
        } catch (IOException i) {
            i.printStackTrace();
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return null;
    }
    
    public void store(String path, String name) {
        if (!String.valueOf(path.charAt(path.length() - 1)).equals(File.separator)) {
            path = path + File.separator;
        }
        
        try {
            FileOutputStream fileOut = new FileOutputStream(path + name + ".syscir");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(this);
            out.close();
            fileOut.close();
            
        } catch (IOException i) {
            i.printStackTrace();
        }
        
    }
    
    /**
     * counts all processes of the system where the dont_initialize flag is not set
     * 
     * @return number of processes that must be initialized
     */
    public int countInitProcesses() {
        int cnt = 0;
        // We iterate through the module instances and not through the modules,
        // as for every module instance we have to create a process, not for
        // every module
        for (SCClassInstance ci : this.instances) {
            // Inner classes are dealt with in their respective outer class
            // instance
            if (ci.getOuterClass() == null) {
                cnt += ci.getSCClass().countInitProcesses();
            }
        }
        return cnt;
    }
    
    /**
     * Returns all processnames which needs initialization. The processes are identified by a list
     * containing the instances they are part of. Example: if a process consume is part of a
     * moduleinstance c_inst, the list will contain <c_inst, consume>. If there is a deeper hierarchy,
     * this will also be reflected in the list. The name of the process is always the last element of
     * each list.
     * 
     * @return
     */
    public List<List<String>> getProcessNamesToInitialize() {
        List<List<String>> ret = new LinkedList<>();
        for (SCClassInstance ci : this.instances) {
            ret.addAll(ci.getProcessNamesToInitialize(new LinkedList<>()));
        }
        return ret;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.globalFunctions == null) ? 0 : this.globalFunctions.hashCode());
        result = prime * result + ((this.globalVariables == null) ? 0 : this.globalVariables.hashCode());
        result = prime * result + ((this.instances == null) ? 0 : this.instances.hashCode());
        result = prime * result + ((this.classes == null) ? 0 : this.classes.hashCode());
        result = prime * result + ((this.portSocketInstances == null) ? 0 : this.portSocketInstances.hashCode());
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
        SCSystem other = (SCSystem) obj;
        if (this.globalFunctions == null) {
            if (other.globalFunctions != null) {
                return false;
            }
        } else if (!this.globalFunctions.equals(other.globalFunctions)) {
            return false;
        }
        if (this.globalVariables == null) {
            if (other.globalVariables != null) {
                return false;
            }
        } else if (!this.globalVariables.equals(other.globalVariables)) {
            return false;
        }
        if (this.instances == null) {
            if (other.instances != null) {
                return false;
            }
        } else if (!this.instances.equals(other.instances)) {
            return false;
        }
        if (this.classes == null) {
            if (other.classes != null) {
                return false;
            }
        } else if (!this.classes.equals(other.classes)) {
            return false;
        }
        if (this.portSocketInstances == null) {
            if (other.portSocketInstances != null) {
                return false;
            }
        } else if (!this.portSocketInstances.equals(other.portSocketInstances)) {
            return false;
        }
        return true;
    }
    
}
