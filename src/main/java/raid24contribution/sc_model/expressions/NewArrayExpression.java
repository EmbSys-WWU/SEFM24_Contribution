package raid24contribution.sc_model.expressions;

import java.util.LinkedList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Node;
import raid24contribution.engine.util.Pair;

/**
 * Represents a 'new $type[size]' call.
 *
 */
public class NewArrayExpression extends Expression {
    
    private static Logger logger = LogManager.getLogger(NewArrayExpression.class.getName());
    
    private static final long serialVersionUID = -1605241181529469122L;
    private String objType;
    private Expression size;
    
    public NewArrayExpression(Node n) {
        super(n);
        Expression size = new ConstantExpression(n, "");
        setSize(size);
    }
    
    @Override
    public LinkedList<Expression> crawlDeeper() {
        LinkedList<Expression> ll = new LinkedList<>();
        ll.add(this.size);
        return ll;
    }
    
    @Override
    public Expression getChild(int index) {
        return switch (index) {
            case 0 -> this.size;
            default -> throw new IndexOutOfBoundsException(index);
        };
    }
    
    @Override
    public int getNumOfChildren() {
        return 1;
    }
    
    @Override
    public void replaceInnerExpressions(List<Pair<Expression, Expression>> replacements) {
        this.size = replaceSingleExpression(this.size, replacements);
    }
    
    @Override
    public String toString() {
        return "new " + this.objType + "[" + this.size.toString().replace(";", "") + "]";
    }
    
    /**
     * @return the objType
     */
    public String getObjType() {
        return this.objType;
    }
    
    /**
     * @param objType the objType to set
     */
    public void setObjType(String objType) {
        this.objType = objType;
    }
    
    public Expression getSize() {
        return this.size;
    }
    
    public void setSize(Expression size) {
        size.setParent(this);
        this.size = size;
    }
}
