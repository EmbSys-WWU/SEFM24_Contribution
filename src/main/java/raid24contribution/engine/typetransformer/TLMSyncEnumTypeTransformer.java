package raid24contribution.engine.typetransformer;

import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import raid24contribution.engine.Environment;
import raid24contribution.sc_model.SCClass;
import raid24contribution.sc_model.expressions.ConstantExpression;
import raid24contribution.sc_model.variables.SCKnownType;
import raid24contribution.sc_model.variables.SCSimpleType;

public class TLMSyncEnumTypeTransformer extends AbstractTypeTransformer {
    
    private static Logger logger = LogManager.getLogger(TLMSyncEnumTypeTransformer.class.getName());
    
    @Override
    public void createType(Environment e) {
        // TODO mp: this is still the old way and should be updated so that we
        // only create the types if necessary
        super.createType(e);
        this.name = "tlm_sync_enum";
        if (e.getKnownTypes().isEmpty() || !e.getKnownTypes().containsKey("tlm_sync_enum")) {
            SCClass sync_enum = new SCClass("tlm_sync_enum");
            e.getKnownTypes().put("tlm_sync_enum", sync_enum);
            ConstantExpression ce1 = new ConstantExpression(null, "TLM_ACCEPTED");
            SCSimpleType st0 =
                    new SCSimpleType(ce1.getValue(), "tlm_sync_enum", ce1, false, true, new ArrayList<>()); // 0
            ConstantExpression ce2 = new ConstantExpression(null, "TLM_UPDATED");
            SCSimpleType st1 =
                    new SCSimpleType(ce2.getValue(), "tlm_sync_enum", ce2, false, true, new ArrayList<>()); // 1
            ConstantExpression ce3 = new ConstantExpression(null, "TLM_COMPLETED");
            SCSimpleType st2 =
                    new SCSimpleType(ce3.getValue(), "tlm_sync_enum", ce3, false, true, new ArrayList<>()); // 2
            ConstantExpression ce4 = new ConstantExpression(null, "TLM_READ_COMMAND");
            SCSimpleType st3 =
                    new SCSimpleType(ce4.getValue(), "tlm_sync_enum", ce4, false, true, new ArrayList<>());
            ConstantExpression ce5 = new ConstantExpression(null, "TLM_WRITE_COMMAND");
            SCSimpleType st4 =
                    new SCSimpleType(ce5.getValue(), "tlm_sync_enum", ce5, false, true, new ArrayList<>());
            ConstantExpression ce6 = new ConstantExpression(null, "TLM_OK_RESPONSE");
            SCSimpleType st5 =
                    new SCSimpleType(ce6.getValue(), "tlm_sync_enum", ce6, false, true, new ArrayList<>());
            ConstantExpression ce7 = new ConstantExpression(null, "BEGIN_RESP");
            SCSimpleType st6 =
                    new SCSimpleType(ce7.getValue(), "tlm_sync_enum", ce7, false, true, new ArrayList<>());
            ConstantExpression ce8 = new ConstantExpression(null, "END_RESP");
            SCSimpleType st7 =
                    new SCSimpleType(ce8.getValue(), "tlm_sync_enum", ce8, false, true, new ArrayList<>());
            ConstantExpression ce9 = new ConstantExpression(null, "BEGIN_REQ");
            SCSimpleType st8 =
                    new SCSimpleType(ce9.getValue(), "tlm_sync_enum", ce9, false, true, new ArrayList<>());
            ConstantExpression ce10 = new ConstantExpression(null, "END_REQ");
            SCSimpleType st9 =
                    new SCSimpleType(ce10.getValue(), "tlm_sync_enum", ce10, false, true, new ArrayList<>());
            ConstantExpression ce11 = new ConstantExpression(null, "TLM_IGNORE_COMMAND");
            SCSimpleType st10 =
                    new SCSimpleType(ce11.getValue(), "tlm_sync_enum", ce11, false, true, new ArrayList<>());
            ConstantExpression ce12 = new ConstantExpression(null, "TLM_INCOMPLETE_RESPONSE");
            SCSimpleType st11 =
                    new SCSimpleType(ce12.getValue(), "tlm_sync_enum", ce12, false, true, new ArrayList<>());
            ConstantExpression ce13 = new ConstantExpression(null, "TLM_BURST_ERROR_RESPONSE");
            SCSimpleType st12 =
                    new SCSimpleType(ce13.getValue(), "tlm_sync_enum", ce13, false, true, new ArrayList<>());
            ConstantExpression ce14 = new ConstantExpression(null, "TLM_GENERIC_ERROR_RESPONSE");
            SCSimpleType st13 =
                    new SCSimpleType(ce14.getValue(), "tlm_sync_enum", ce14, false, true, new ArrayList<>());
            
            e.getSystem().addGlobalVariable(st0);
            e.getSystem().addGlobalVariable(st1);
            e.getSystem().addGlobalVariable(st2);
            e.getSystem().addGlobalVariable(st3);
            e.getSystem().addGlobalVariable(st4);
            e.getSystem().addGlobalVariable(st5);
            e.getSystem().addGlobalVariable(st6);
            e.getSystem().addGlobalVariable(st7);
            e.getSystem().addGlobalVariable(st8);
            e.getSystem().addGlobalVariable(st9);
            e.getSystem().addGlobalVariable(st10);
            e.getSystem().addGlobalVariable(st11);
            e.getSystem().addGlobalVariable(st12);
            e.getSystem().addGlobalVariable(st13);
            
            sync_enum.addMember(st0);
            sync_enum.addMember(st1);
            sync_enum.addMember(st2);
            sync_enum.addMember(st3);
            sync_enum.addMember(st4);
            sync_enum.addMember(st5);
            sync_enum.addMember(st6);
            sync_enum.addMember(st7);
            sync_enum.addMember(st8);
            sync_enum.addMember(st9);
            sync_enum.addMember(st10);
            sync_enum.addMember(st11);
            sync_enum.addMember(st12);
            sync_enum.addMember(st13);
        }
        
    }
    
    @Override
    public SCKnownType createInstance(String instName, Environment e, boolean stat, boolean cons,
            List<String> other_mods) {
        // return super.createInstance(instName, e, stat, cons, other_mods);
        SCClass type = e.getKnownTypes().get("tlm_sync_enum");
        SCKnownType kt = null;
        if (type != null) {
            kt = new SCKnownType(instName, type, e.getCurrentClass(), null, stat, cons, other_mods,
                    e.getLastInitializer());
        } else {
            logger.error("Configuration error: type {} is not available", this.name);
        }
        return kt;
    }
}
