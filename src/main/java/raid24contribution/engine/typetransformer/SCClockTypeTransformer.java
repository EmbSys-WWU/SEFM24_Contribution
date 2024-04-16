package raid24contribution.engine.typetransformer;

import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import raid24contribution.engine.Environment;
import raid24contribution.sc_model.SCClass;
import raid24contribution.sc_model.expressions.ConstantExpression;
import raid24contribution.sc_model.expressions.Expression;
import raid24contribution.sc_model.expressions.TimeUnitExpression;
import raid24contribution.sc_model.variables.SCKnownType;
import raid24contribution.sc_model.variables.SCTIMEUNIT;

public class SCClockTypeTransformer extends AbstractTypeTransformer {
    
    private static Logger logger = LogManager.getLogger(SCClockTypeTransformer.class.getName());
    
    @Override
    public void createType(Environment e) {
        super.createType(e);
        this.name = "sc_clock";
    }
    
    @Override
    public SCKnownType createInstance(String instName, Environment e, boolean stat, boolean cons,
            List<String> other_mods) {
        SCClass type = e.getKnownTypes().get(this.name);
        SCKnownType kt = null;
        if (type != null) {
            kt = new SCKnownType(instName, type, e.getCurrentClass(), null, stat, cons, other_mods,
                    e.getLastInitializer());
        } else {
            logger.error("Configuration error: type sc_clock not available");
        }
        return kt;
    }
    
    @Override
    public SCKnownType initiateInstance(String instName, List<Expression> params, Environment e, boolean stat,
            boolean cons, List<String> other_mods) {
        SCClass type = e.getKnownTypes().get(this.name);
        SCKnownType kt = null;
        if (type != null) {
            if (e.getKnownTypes().get(this.name).getConstructor().getParameters().size() != params.size() - 1)
            // Constructor in implementation-file has only 2 params, because the
            // implementation
            // only handle SC_NS, so its default, but the
            // systemC-Constructor-call has 3 parameters
            
            {
                
                logger.error("{} not the right number of parameters to initiate {}", this, instName);
                return null;
            } else {
                if (params.size() == 3 && params.get(1) instanceof ConstantExpression
                        && params.get(2) instanceof TimeUnitExpression) {
                    int period = Integer.parseInt(((ConstantExpression) params.get(1)).getValue());
                    TimeUnitExpression unit = (TimeUnitExpression) params.get(2);
                    period = SCTIMEUNIT.convert(period, unit.getTimeUnit(), SCTIMEUNIT.valueOf("SC_NS"));
                    unit.setTimeUnit(SCTIMEUNIT.valueOf("SC_NS"));
                    ConstantExpression periodExpr = (ConstantExpression) params.get(1);
                    periodExpr.setValue("" + period);
                    params.set(2, unit);
                    params.set(1, periodExpr);
                    
                }
                kt = new SCKnownType(instName, type, e.getCurrentClass(), params, stat, cons, other_mods,
                        e.getLastInitializer());
            }
        } else {
            logger.error("Configuration error: type sc_clock not available");
        }
        return kt;
        
    }
    
}
