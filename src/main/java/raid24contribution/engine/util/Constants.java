package raid24contribution.engine.util;

public class Constants {
    
    /**
     * Most of the identifiers in our model need to be prefixed in some way, either depending on scope,
     * or thread, or to create some keywords. This String is used to separated the prefixes internally.
     */
    public static final String PREFIX_DELIMITER = "#";
    
    /**
     * Delimiter for struct and class methods from custom datatypes was "_" before, in case this leads
     * to problems: reverse only here to "_"
     */
    public static final String STRUCT_METHOD_PREFIX_DELIMITER = "$";
    
    public static final String GENERIC_TYPE = "GTYPE";
    public static final String GENERIC_TYPE_LENGTH = "GLENGTH";
    public static final String GENERIC_TYPE_DELIMITER = "_";
    
    /**
     * The version number of SysCIR.
     */
    public static final String VERSION = "0.5";
}
