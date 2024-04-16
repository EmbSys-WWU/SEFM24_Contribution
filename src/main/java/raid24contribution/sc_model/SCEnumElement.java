package raid24contribution.sc_model;

import java.io.Serializable;

/**
 * Represents the element of an enum.
 * 
 */
public class SCEnumElement implements Serializable {
    
    private static final long serialVersionUID = 2676294182599519357L;
    
    protected String name;
    protected int value;
    
    protected SCEnumType type;
    
    public SCEnumElement(String name, int value, SCEnumType type) {
        this.name = name;
        this.value = value;
        this.type = type;
    }
    
    public String getName() {
        return this.name;
    }
    
    public int getValue() {
        return this.value;
    }
    
    public SCEnumType getType() {
        return this.type;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
        result = prime * result + this.value;
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
        SCEnumElement other = (SCEnumElement) obj;
        if (this.name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!this.name.equals(other.name)) {
            return false;
        }
        if (this.value != other.value) {
            return false;
        }
        return true;
    }
    
    // TODO: just for debugging
    @Override
    public String toString() {
        return this.name;
    }
}
