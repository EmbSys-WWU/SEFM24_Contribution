package raid24contribution.sc_model.variables;

import java.io.Serializable;

/**
 * Enumeration for all predefined SystemC time units, including SC_ZERO_TIME.
 * 
 */
public enum SCTIMEUNIT implements Serializable {
    
    SC_ZERO_TIME(Integer.MAX_VALUE), SC_FS(-15), SC_PS(-12), SC_NS(-9), SC_US(-6), SC_MS(-3), SC_SEC(0);
    
    /**
     * Exponent for timeunit conversion
     */
    private final int exp;
    
    public static boolean containsValue(String s) {
        for (SCTIMEUNIT E : SCTIMEUNIT.values()) {
            if (E.toString().equals(s)) {
                return true;
            }
        }
        return false;
        
    }
    
    private SCTIMEUNIT(int exp) {
        this.exp = exp;
    }
    
    /**
     * Returns the exponent for the time unit. The time unit is a 10^exponent-th part of a second.
     * 
     * @return
     */
    public int getExponent() {
        return this.exp;
    }
    
    /**
     * Converts the given value from time unit input to timeunit output. Bigger time units can be
     * converted to smaller ones (e.g., SC_SEC to SC_NS) without loss, but not necessarily in smaller to
     * bigger. WARNING: The conversion from or to SC_ZERO_TIME (deltacycles) is not possible.
     * 
     * @param value
     * @param input
     * @param output
     * @return the converted value or 0, if the conversion would produce a value smaller than 0 or -1 if
     *         the user tries to convert from or to deltacycles (which is not possible).
     */
    public static int convert(int value, SCTIMEUNIT input, SCTIMEUNIT output) {
        if (input == SC_ZERO_TIME || output == SC_ZERO_TIME) {
            return -1;
        }
        int helpExp = input.exp - output.exp;
        int retval = value;
        if (helpExp > 0) {
            for (int i = 0; i < helpExp; i++) {
                retval *= 10;
            }
        } else if (helpExp < 0) {
            for (int i = 0; i > helpExp; i--) {
                retval /= 10;
            }
        }
        return retval;
    }
    
}
