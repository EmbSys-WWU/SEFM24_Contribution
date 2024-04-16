package raid24contribution.engine.nodetransformer;

import java.util.ArrayList;
import org.w3c.dom.Node;
import raid24contribution.engine.Environment;
import raid24contribution.sc_model.SCPROCESSTYPE;
import raid24contribution.sc_model.SCProcess;

/**
 * we clear the arguments of a scthread, so we can handle the childnode where they are declared with
 * this arguments we create a new scthread and add it to the current module
 * 
 */
public class SCThreadBlockTransformer extends AbstractNodeTransformer {
    
    @Override
    public void transformNode(Node node, Environment e) {
        e.setSensitivityList(new ArrayList<>());
        e.setLastProcessModifier(null);
        e.setLastProcessFunction(null);
        e.setLastProcessName("");
        
        handleChildNodes(node, e);
        
        SCProcess mth = new SCProcess(e.getLastProcessName(), SCPROCESSTYPE.SCTHREAD, e.getLastProcessFunction(),
                e.getSensitivityList(), e.getLastProcessModifier());
        e.getCurrentClass().addProcess(mth);
        
        e.setSensitivityList(new ArrayList<>());
        e.setLastProcessModifier(null);
        e.setLastProcessFunction(null);
        e.setLastProcessName("");
    }
}
