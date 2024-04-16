package raid24contribution.sc_model.expressions;

import java.util.LinkedList;
import java.util.List;
import org.w3c.dom.Node;
import raid24contribution.engine.util.Pair;

/**
 * this expression represents the goto-construct. Currently, it contains only the jump label. TODO
 * mp: as an enhancement it would be nice to resolve the goto.
 * 
 * 
 */
public class GoToExpression extends Expression {
    
    private static final long serialVersionUID = -1993807086866347443L;
    private String jumpLabel;
    
    public GoToExpression(Node n, String jumpLabel) {
        super(n);
        this.jumpLabel = jumpLabel;
    }
    
    @Override
    public String toString() {
        return (super.toString() + "goto " + this.jumpLabel + ";");
    }
    
    public String getJumpLabel() {
        return this.jumpLabel;
    }
    
    public void setJumpLabel(String jumpLabel) {
        this.jumpLabel = jumpLabel;
    }
    
    @Override
    public LinkedList<Expression> crawlDeeper() {
        LinkedList<Expression> ret = new LinkedList<>();
        return ret;
    }
    
    @Override
    public void replaceInnerExpressions(List<Pair<Expression, Expression>> replacements) {}
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((this.jumpLabel == null) ? 0 : this.jumpLabel.hashCode());
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
        GoToExpression other = (GoToExpression) obj;
        if (this.jumpLabel == null) {
            if (other.jumpLabel != null) {
                return false;
            }
        } else if (!this.jumpLabel.equals(other.jumpLabel)) {
            return false;
        }
        return true;
    }
    
}
