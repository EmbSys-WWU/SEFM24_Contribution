package raid24contribution.sc_model;

import java.io.Serializable;

/**
 * this class represents the ports and sockets in systemC it have an name, a connection type and a
 * type. This type tells which kind of data is send through this port or socket.
 * 
 */
public class SCPort implements Serializable {
    
    private static final long serialVersionUID = 8949874004040514703L;
    
    protected String name;
    protected String type;
    protected SCPORTSCSOCKETTYPE con_type;
    
    /**
     * creats a new port or socket
     * 
     * @param nam name of the port or socket
     * @param t datatype of the port
     * @param ct specifier wether its an input-port or an output-port or both, or a socket connection
     *        specifier
     */
    public SCPort(String nam, String t, SCPORTSCSOCKETTYPE ct) {
        this.name = nam;
        this.type = t;
        this.con_type = ct;
    }
    
    /**
     * returns the name of the port or socket
     * 
     * @return String
     */
    public String getName() {
        return this.name;
    }
    
    /**
     * return the datatype of the port or socket
     * 
     * @return SCTYPES
     */
    public String getType() {
        return this.type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    /**
     * returns the specifier of the port or socket
     * 
     * @return
     */
    public SCPORTSCSOCKETTYPE getConType() {
        return this.con_type;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.con_type == null) ? 0 : this.con_type.hashCode());
        result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
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
        SCPort other = (SCPort) obj;
        if (this.con_type != other.con_type) {
            return false;
        }
        if (this.name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!this.name.equals(other.name)) {
            return false;
        }
        if (this.type == null) {
            if (other.type != null) {
                return false;
            }
        } else if (!this.type.equals(other.type)) {
            return false;
        }
        return true;
    }
    
    @Override
    public String toString() {
        return this.con_type.toString().toLowerCase() + "<" + this.type + "> " + this.name;
    }
    
}
