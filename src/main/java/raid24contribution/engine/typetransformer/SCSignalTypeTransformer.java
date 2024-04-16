package raid24contribution.engine.typetransformer;

import java.util.List;
import raid24contribution.engine.Environment;
import raid24contribution.engine.TransformerFactory;
import raid24contribution.engine.util.Constants;
import raid24contribution.engine.util.Pair;
import raid24contribution.sc_model.expressions.Expression;
import raid24contribution.sc_model.variables.SCKnownType;

public class SCSignalTypeTransformer extends AbstractTypeTransformer {
    
    /**
     * creates the scClass of the sc_signal and applies the correct data type to the channel
     */
    @Override
    public void createType(Environment e) {
        
        if (!e.getLastType().isEmpty() && !e.getLastType().peek().startsWith("sc_signal")
                && !e.getLastType().peek().equals("void")) {
            String type = e.getLastType().peek();
            this.name = createName(type);
            Environment temp = createEnvironment(e, type);
            
            temp.getCurrentClass().setName(this.name);
            e.integrate(temp);
        } else if (!e.getLastType_TemplateArguments().isEmpty()) {
            String type = e.getLastType_TemplateArguments().get(0);
            this.name = createName(type);
        }
        
    }
    
    /**
     * creates the correct SCKnownType for the sc_signal when the channel is instantiated
     */
    @Override
    public SCKnownType initiateInstance(String instName, List<Expression> params, Environment e, boolean stat,
            boolean cons, List<String> other_mods) {
        
        if (!e.getLastType_TemplateArguments().isEmpty()) {
            String type = e.getLastType_TemplateArguments().get(0);
            this.name = createName(type);
        }
        SCKnownType knownType = super.initiateInstance(instName, params, e, stat, cons, other_mods);
        return knownType;
        
    }
    
    /**
     * creates an Environment-Object of the sc_signal implementation therefore it uses -
     * sc_signal_generic.ast.xml for data type with generic parameters (i.e. sc_uint<XX>) -
     * sc_signal.ast.xml for simple data typen (i.e. bool, int, ...)
     * 
     * @param e
     * @param type
     * @return
     */
    private Environment createEnvironment(Environment e, String type) {
        Pair<String, String>[] replacements;
        if (type.contains("<")) {
            replacements = new Pair[3];
            
            this.impl = TransformerFactory.getImplementation("sc_signal", "sc_signal_generic.ast.xml");
            String typeIdentifier = type.substring(0, type.indexOf("<"));
            String length = type.substring(type.indexOf("<") + 1, type.length() - 1);
            replacements[0] = new Pair<>(Constants.GENERIC_TYPE, typeIdentifier);
            replacements[1] = new Pair<>(Constants.GENERIC_TYPE_LENGTH, length);
            replacements[2] = new Pair<>("sc_signalx", "sc_signal_" + typeIdentifier);
        } else {
            replacements = new Pair[2];
            replacements[0] = new Pair<>(Constants.GENERIC_TYPE, type);
            replacements[1] = new Pair<>("sc_signalx", "sc_signal_" + type);
        }
        
        return super.createGenericType(e, replacements);
    }
    
    /**
     * removes special characters from the type String so that valid SCClass names will be created
     *
     * @param type
     * @return
     */
    private static String typeForTemplate(String type) {
        return type.replace(" ", "").replace("<", "").replace(">", "").replace("_", "");
    }
    
    /**
     * creates the name string for sc_signal -> sc_signal_TYPE
     *
     * @param type
     * @return
     */
    public static String createName(String type) {
        return "sc_signal" + Constants.GENERIC_TYPE_DELIMITER + typeForTemplate(type);
    }
    
}
