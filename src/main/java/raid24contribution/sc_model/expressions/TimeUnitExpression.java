package raid24contribution.sc_model.expressions;

import java.util.LinkedList;
import java.util.List;
import org.w3c.dom.Node;
import raid24contribution.engine.util.Pair;
import raid24contribution.sc_model.variables.SCTIMEUNIT;

/**
 * Expression that encapsulates all predefined time units in systemc as defined in the
 * {@link SCTIMEUNIT}-enumeration.
 */
public class TimeUnitExpression extends Expression {
    
    private static final long serialVersionUID = -4271070922645907775L;
    
    SCTIMEUNIT timeUnit;
    
    public TimeUnitExpression(Node n, String label, SCTIMEUNIT timeUnit) {
        super(n, label);
        this.timeUnit = timeUnit;
    }
    
    public SCTIMEUNIT getTimeUnit() {
        return this.timeUnit;
    }
    
    public void setTimeUnit(SCTIMEUNIT timeUnit) {
        this.timeUnit = timeUnit;
    }
    
    @Override
    public String toString() {
        return this.timeUnit.name();
    }
    
    @Override
    public LinkedList<Expression> crawlDeeper() {
        LinkedList<Expression> ret = new LinkedList<>();
        return ret;
    }
    
    @Override
    public Expression getChild(int index) {
        throw new IndexOutOfBoundsException(index);
    }
    
    @Override
    public int getNumOfChildren() {
        return 0;
    }
    
    @Override
    public void replaceInnerExpressions(List<Pair<Expression, Expression>> replacements) {}
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((this.timeUnit == null) ? 0 : this.timeUnit.hashCode());
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
        TimeUnitExpression other = (TimeUnitExpression) obj;
        if (this.timeUnit != other.timeUnit) {
            return false;
        }
        return true;
    }
    
}
