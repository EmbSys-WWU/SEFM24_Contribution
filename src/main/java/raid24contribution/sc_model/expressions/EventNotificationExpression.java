package raid24contribution.sc_model.expressions;

import java.util.LinkedList;
import java.util.List;
import org.w3c.dom.Node;
import raid24contribution.engine.util.Pair;

/**
 * Represents the event notification expression. An event notification consists of a
 * variableExpression containing the event and either 0 (immediate notification) one or two
 * parameters (delta delay or timed notification).
 * 
 * 
 */
public class EventNotificationExpression extends Expression {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * The event which is notified.
     */
    private Expression event;
    
    /**
     * The parameters for the event notification. This list may be empty if the notification is an
     * immediate notification or contain an SC_ZERO_DELAY timing expression (delta delay notification),
     * an sc_time variable or two parameters, one of them a timingExpression (timed notification).
     */
    private List<Expression> parameters;
    
    public EventNotificationExpression(Node node, SCVariableExpression event, List<Expression> params) {
        super(node);
        this.event = event;
        this.event.setParent(this);
        setParameters(params);
    }
    
    @Override
    public String toString() {
        String ret = super.toString();
        ret += this.event.toString().replace(";", "") + ".notify(";
        // parameters can only have 3 different (valid) sizes
        if (this.parameters.size() == 1) {
            ret += this.parameters.get(0).toString();
        } else if (this.parameters.size() == 2) {
            ret += this.parameters.get(0).toString() + ", " + this.parameters.get(1);
        }
        
        ret += ");";
        return ret;
    }
    
    @Override
    public LinkedList<Expression> crawlDeeper() {
        LinkedList<Expression> ret = new LinkedList<>();
        ret.add(this.event);
        ret.addAll(this.parameters);
        return ret;
    }
    
    @Override
    public Expression getChild(int index) {
        return switch (index) {
            case 0 -> this.event;
            default -> this.parameters.get(index - 1);
        };
    }
    
    @Override
    public int getNumOfChildren() {
        return this.parameters.size() + 1;
    }
    
    @Override
    public List<Expression> getInnerExpressions() {
        List<Expression> exps = new LinkedList<>();
        for (Expression exp : this.parameters) {
            exps.add(exp);
            exps.addAll(exp.getInnerExpressions());
        }
        return exps;
    }
    
    @Override
    public void replaceInnerExpressions(List<Pair<Expression, Expression>> replacements) {
        this.event = replaceSingleExpression(this.event, replacements);
        replaceExpressionList(this.parameters, replacements);
    }
    
    public Expression getEvent() {
        return this.event;
    }
    
    public void setEvent(SCVariableExpression event) {
        this.event = event;
        this.event.setParent(this);
    }
    
    public List<Expression> getParameters() {
        return this.parameters;
    }
    
    public void setParameters(List<Expression> parameters) {
        this.parameters = parameters;
        for (Expression exp : this.parameters) {
            exp.setParent(this);
        }
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((this.event == null) ? 0 : this.event.hashCode());
        result = prime * result + ((this.parameters == null) ? 0 : this.parameters.hashCode());
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
        EventNotificationExpression other = (EventNotificationExpression) obj;
        if (this.event == null) {
            if (other.event != null) {
                return false;
            }
        } else if (!this.event.equals(other.event)) {
            return false;
        }
        if (this.parameters == null) {
            if (other.parameters != null) {
                return false;
            }
        } else if (!this.parameters.equals(other.parameters)) {
            return false;
        }
        return true;
    }
    
}
