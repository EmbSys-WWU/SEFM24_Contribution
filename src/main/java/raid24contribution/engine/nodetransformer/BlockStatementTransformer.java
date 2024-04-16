package raid24contribution.engine.nodetransformer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Node;
import raid24contribution.engine.Environment;
import raid24contribution.engine.util.NodeUtil;
import raid24contribution.sc_model.SCFunction;
import raid24contribution.sc_model.expressions.Expression;

/**
 * a blockstatment-tag marks one line in the code but sometimes a blockstatment-tag is a child of a
 * blockstatment-tag both cases have to be handled different if its the head-blockstatment we
 * remeber it, clear the expression-stack handle the childnodes and add all Expressions in the right
 * order to the current function at hte second case we only handle the childnodes
 * 
 */
public class BlockStatementTransformer extends AbstractNodeTransformer {
    
    private static Logger logger = LogManager.getLogger(BlockStatementTransformer.class.getName());
    
    @Override
    public void transformNode(Node node, Environment e) {
        if (!e.isRekursiveBlockStatement()) {
            e.setRekursiveBlockStatement(true);
            e.getExpressionStack().clear();
            handleChildNodes(node, e);
            if (!e.getExpressionStack().isEmpty()) {
                for (Expression expr : e.getExpressionStack()) {
                    SCFunction scfc = e.getCurrentFunction();
                    if (scfc != null) {
                        scfc.addExpressionAtEnd(expr);
                    } else {
                        logger.error("{}: Found BlockStatement outside of a function.",
                                NodeUtil.getFixedAttributes(node));
                    }
                }
                e.getExpressionStack().clear();
            }
            e.setRekursiveBlockStatement(false);
        } else {
            handleChildNodes(node, e);
        }
        
    }
}
