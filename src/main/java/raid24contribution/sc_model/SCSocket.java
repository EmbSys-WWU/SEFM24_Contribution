package raid24contribution.sc_model;

import java.io.Serializable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * this class represents the ports and sockets in systemC it have an name, a connection type and a
 * type. This type tells which kind of data is send through this port or socket.
 * 
 * 
 */
public class SCSocket extends SCPort implements Serializable {
    
    private static final long serialVersionUID = 8949874004040514703L;
    
    private static transient final Logger logger = LogManager.getLogger(SCSocket.class.getName());
    
    /**
     * creats a new socket
     * 
     * @param nam name of the socket
     * @param t datatype of the socket
     * @param ct specifier whether its an input-port or an output-port or both, or a socket connection
     *        specifier
     */
    public SCSocket(String nam, String t, SCPORTSCSOCKETTYPE ct) {
        super(nam, t, ct);
        if (ct != SCPORTSCSOCKETTYPE.SC_SOCKET) {
            logger.warn("Created socket '{}' with type other than SC_SOCKET: {}", nam, ct);
        }
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
        SCSocket other = (SCSocket) obj;
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
