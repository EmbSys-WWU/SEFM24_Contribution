package raid24contribution.sc_model.variables;

import java.util.ArrayList;
import raid24contribution.sc_model.SCPort;

/**
 * this class represents a Portevent, which is required for sensitivity on ports or sockets
 * 
 */
public class SCPortEvent extends SCEvent {
    
    private static final long serialVersionUID = -1980590687800171057L;
    
    private SCPort port_socket;
    private String eventType;
    
    public SCPortEvent(String nam, SCPort ps, String eventType) {
        super(nam, false, false, new ArrayList<>());
        
        this.port_socket = ps;
        this.eventType = eventType;
        
    }
    
    public SCPortEvent(String nam, SCPort ps) {
        this(nam, ps, "default_event");
    }
    
    public String getEventType() {
        return this.eventType;
    }
    
    public SCPort getPort() {
        return this.port_socket;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((this.eventType == null) ? 0 : this.eventType.hashCode());
        result = prime * result + ((this.port_socket == null) ? 0 : this.port_socket.hashCode());
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
        if (getClass() != obj.getClass()) {
            return false;
        }
        SCPortEvent other = (SCPortEvent) obj;
        if (this.eventType == null) {
            if (other.eventType != null) {
                return false;
            }
        } else if (!this.eventType.equals(other.eventType)) {
            return false;
        }
        if (this.port_socket == null) {
            if (other.port_socket != null) {
                return false;
            }
        } else if (!this.port_socket.equals(other.port_socket)) {
            return false;
        }
        return true;
    }
    
    
}
