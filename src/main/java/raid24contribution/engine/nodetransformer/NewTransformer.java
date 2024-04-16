package raid24contribution.engine.nodetransformer;

import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import raid24contribution.engine.Environment;
import raid24contribution.engine.util.NodeUtil;
import raid24contribution.sc_model.expressions.Expression;
import raid24contribution.sc_model.expressions.NewArrayExpression;
import raid24contribution.sc_model.expressions.NewExpression;

/**
 * Transforms 'new $type', 'new $type(init)' and 'new $type[size]' expressions.
 *
 *
 */
public class NewTransformer extends AbstractNodeTransformer {
    
    private static Logger logger = LogManager.getLogger(NewTransformer.class.getName());
    
    @Override
    public void transformNode(Node node, Environment e) {
        Node new_type_id = findChildNode(node, "new_type_id");
        // the object type (myclassname/mystructname/int/...) name:
        Node typeNode = recursiveFindChildNode(new_type_id, "qualified_id");
        if (typeNode == null) {
            typeNode = recursiveFindChildNode(new_type_id, "builtin_type_specifier"); // for 'int', 'bool'
        }
        String type = NodeUtil.getAttributeValueByName(typeNode, "name");
        Node new_declarator = recursiveFindChildNode(node, "new_declarator");
        Expression ret = null;
        if (new_declarator == null) { // normal object
            NewExpression ne = new NewExpression(node);
            ne.setObjType(type);
            
            Node new_initializer = findChildNode(node, "new_initializer");
            if (new_initializer != null) { // the contructor is called
                Node arguments_list = findChildNode(new_initializer, "arguments_list");
                if (arguments_list != null) { // arguments are provided
                    NodeList args = arguments_list.getChildNodes();
                    int len = args.getLength();
                    List<Expression> arguments = new ArrayList<>(len);
                    int prevStacksize = e.getExpressionStack().size();
                    int currStacksize;
                    Expression argExpr;
                    for (int i = 0; i < len; i++) {
                        handleNode(args.item(i), e);
                        currStacksize = e.getExpressionStack().size();
                        if (currStacksize != prevStacksize) { // something
                                                              // happened
                            if (currStacksize == prevStacksize + 1) {
                                argExpr = e.getExpressionStack().pop();
                                arguments.add(argExpr);
                            } else {
                                logger.info("1 argument != 1 expression while reading new initilizer list");
                            }
                        }
                    }
                    ne.setArguments(arguments);
                }
            }
            ret = ne;
        } else { // array declaration
            Node direct_new_declarator = recursiveFindChildNode(new_declarator, "direct_new_declarator");
            if (direct_new_declarator != null) {
                handleChildNodes(direct_new_declarator, e);
                Expression size = e.getExpressionStack().pop();
                NewArrayExpression nae = new NewArrayExpression(node);
                nae.setObjType(type);
                nae.setSize(size);
                ret = nae;
            } else {
                logger.error("{}: cant handle dyn array creation without direct declararation",
                        NodeUtil.getFixedAttributes(new_declarator));
            }
        }
        if (ret != null) {
            e.getExpressionStack().add(ret);
        } else {
            logger.error("{}: Couldn't transform 'new' expression", NodeUtil.getFixedAttributes(node));
        }
    }
}
