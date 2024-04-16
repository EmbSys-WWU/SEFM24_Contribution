package raid24contribution.sc_model;


import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;



/**
 * Represents an enum which is defined in the model.
 * 
 * 
 */
public class SCEnumType implements Serializable {
    
    private static final long serialVersionUID = -396027654081396147L;
    
    private static Logger logger = LogManager.getLogger(SCEnumType.class.getName());
    /**
     * Name of the enum
     */
    protected String name;
    
    /**
     * List of elements in this enum
     */
    protected List<SCEnumElement> elements;
    
    private int lastInsertedValue;
    
    /**
     * true if the elements in the enum are continuous e.g. if value 2 and 5 exists, so do 3 and 4!
     */
    private boolean isContinuous = true;
    
    /**
     * The min value stored in the elements
     */
    private int min = Integer.MAX_VALUE;
    /**
     * The max value stored in the elements
     */
    private int max = Integer.MIN_VALUE;
    
    public SCEnumType(String name) {
        this.name = name;
        this.lastInsertedValue = -1;
        this.elements = new LinkedList<>();
    }
    
    public String getName() {
        return this.name;
    }
    
    public void addElement(String name, int value) {
        this.lastInsertedValue = value;
        this.elements.add(new SCEnumElement(name, value, this));
        if (value < this.min) {
            this.min = value;
        }
        if (value > this.max) {
            this.max = value;
        }
        
        
        if (value < this.lastInsertedValue) {
            logger.error(
                    "Implementation of SCEnum does not safly support adding smaller enum values than the values before");
        } else if (value == this.lastInsertedValue) {
            logger.error("The enum Value " + value + " is added a second time to the enum " + getName());
        } else if ((value - this.lastInsertedValue) != 1) {
            this.isContinuous = false;
        }
    }
    
    public void addElement(String name) {
        this.elements.add(new SCEnumElement(name, ++this.lastInsertedValue, this));
        if (this.lastInsertedValue < this.min) {
            this.min = this.lastInsertedValue;
        }
        if (this.lastInsertedValue > this.max) {
            this.max = this.lastInsertedValue;
        }
    }
    
    public List<SCEnumElement> getElements() {
        return this.elements;
    }
    
    /**
     * Checks if the elements in the enum are continuous e.g. if value 2 and 5 exists, so do 3 and 4!
     *
     * @return true if continuous, false otherwise
     */
    public boolean isContinuous() {
        return this.isContinuous;
    }
    
    public int getMinValue() {
        if (this.elements.size() < 1) {
            logger.error("Returned a minValue of an empty Enum");
            return -1;
        }
        return this.min;
    }
    
    public int getMaxValue() {
        if (this.elements.size() < 1) {
            logger.error("Returned a maxValue of an empty Enum");
            return -1;
        }
        return this.max;
    }
    
    /**
     * Checks if the given name is an element of this enum. Returns true if the given String is equal to
     * one of the elements of the enum, false otherwise.
     * 
     * @param name
     * @return
     */
    public boolean containsElement(String name) {
        for (SCEnumElement el : this.elements) {
            if (el.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Returns the element of this enum with the given name. If there is no element with this name it
     * returns null.
     * 
     * @param name
     * @return
     */
    public SCEnumElement getElementByName(String name) {
        for (SCEnumElement el : this.elements) {
            if (el.getName().equals(name)) {
                return el;
            }
        }
        return null;
    }
    
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.elements == null) ? 0 : this.elements.hashCode());
        result = prime * result + this.lastInsertedValue;
        result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
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
        SCEnumType other = (SCEnumType) obj;
        if (this.elements == null) {
            if (other.elements != null) {
                return false;
            }
        } else if (!this.elements.equals(other.elements)) {
            return false;
        }
        if (this.lastInsertedValue != other.lastInsertedValue) {
            return false;
        }
        if (this.name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!this.name.equals(other.name)) {
            return false;
        }
        return true;
    }
    
    // TODO: just for debugging
    @Override
    public String toString() {
        String elemStr = "";
        for (SCEnumElement elem : this.elements) {
            elemStr += elem.toString() + ",";
        }
        return this.name + "[" + elemStr + "]";
    }
}
