package raid24contribution.engine.typetransformer;

import java.util.ArrayList;
import java.util.List;
import raid24contribution.engine.Environment;
import raid24contribution.sc_model.SCClass;
import raid24contribution.sc_model.SCFunction;
import raid24contribution.sc_model.SCParameter;
import raid24contribution.sc_model.SCREFERENCETYPE;
import raid24contribution.sc_model.expressions.ConstantExpression;
import raid24contribution.sc_model.expressions.Expression;
import raid24contribution.sc_model.expressions.ReturnExpression;
import raid24contribution.sc_model.variables.SCSimpleType;
import raid24contribution.sc_model.variables.SCTime;

public class TLMFwBwIFTransformer extends AbstractTypeTransformer {
    
    @Override
    public void createType(Environment e) {
        // super.createType(e);
        this.name = "tlm_fw_bw_if";
        
        SCClass tlmfwbwIF = new SCClass("tlm_fw_bw_if");
        tlmfwbwIF.setPrimitiveChannel();
        
        SCFunction invalidate_direct_mem_ptr = generateInvalidateDirectMemPtr();
        tlmfwbwIF.addMemberFunction(invalidate_direct_mem_ptr);
        
        SCFunction nb_transport_bw = generateTransportFWBW("nb_transport_bw");
        tlmfwbwIF.addMemberFunction(nb_transport_bw);
        
        SCFunction nb_transport_fw = generateTransportFWBW("nb_transport_fw");
        tlmfwbwIF.addMemberFunction(nb_transport_fw);
        
        SCFunction b_transport = generateBTransport();
        tlmfwbwIF.addMemberFunction(b_transport);
        
        SCFunction get_direct_mem_ptr = generateGetDIrectMemPtr();
        tlmfwbwIF.addMemberFunction(get_direct_mem_ptr);
        
        SCFunction transport_dbg = generateTransportDBG(get_direct_mem_ptr);
        tlmfwbwIF.addMemberFunction(transport_dbg);
        
        if (e.getKnownTypes().isEmpty() || !e.getKnownTypes().containsKey(this.name)) {
            e.getKnownTypes().put(this.name, tlmfwbwIF);
        }
        
    }
    
    private SCFunction generateTransportDBG(SCFunction get_direct_mem_ptr) {
        SCParameter tran = new SCParameter(new SCSimpleType("tran", "data", false, false, new ArrayList<>()),
                SCREFERENCETYPE.BYREFERENCE);
        List<SCParameter> transportDbgParams = new ArrayList<>();
        transportDbgParams.add(tran);
        SCFunction transport_dbg = new SCFunction("transport_dbg", "unsigned int", transportDbgParams);
        ConstantExpression nul = new ConstantExpression(null, "0");
        ReturnExpression re2 = new ReturnExpression(null, nul);
        get_direct_mem_ptr.addExpressionAtEnd(re2);
        return transport_dbg;
    }
    
    private SCFunction generateGetDIrectMemPtr() {
        SCParameter tran = new SCParameter(new SCSimpleType("tran", "data", false, false, new ArrayList<>()),
                SCREFERENCETYPE.BYREFERENCE);
        SCParameter dmi_data =
                new SCParameter(new SCSimpleType("dmi_data", "tlm_dmi", false, false, new ArrayList<>()),
                        SCREFERENCETYPE.BYVALUE);
        List<SCParameter> getDirMemPtrParams = new ArrayList<>();
        getDirMemPtrParams.add(tran);
        getDirMemPtrParams.add(dmi_data);
        SCFunction get_direct_mem_ptr = new SCFunction("get_direct_mem_ptr", "bool", getDirMemPtrParams);
        ConstantExpression fal = new ConstantExpression(null, "false");
        Expression re1 = new ReturnExpression(null, fal);
        get_direct_mem_ptr.addExpressionAtEnd(re1);
        return get_direct_mem_ptr;
    }
    
    private SCFunction generateBTransport() {
        SCParameter tran = new SCParameter(new SCSimpleType("tran", "data", false, false, new ArrayList<>()),
                SCREFERENCETYPE.BYREFERENCE);
        SCParameter time = new SCParameter(new SCTime("t", false, false, new ArrayList<>(), false),
                SCREFERENCETYPE.BYREFERENCE);
        List<SCParameter> bTransParams = new ArrayList<>();
        bTransParams.add(tran);
        bTransParams.add(time);
        SCFunction b_transport = new SCFunction("b_transport", "void", bTransParams);
        return b_transport;
    }
    
    private SCFunction generateTransportFWBW(String name) {
        SCParameter tran = new SCParameter(new SCSimpleType("tran", "data", false, false, new ArrayList<>()),
                SCREFERENCETYPE.BYREFERENCE);
        SCParameter phase = new SCParameter(new SCSimpleType("phase", "int", false, false, new ArrayList<>()),
                SCREFERENCETYPE.BYREFERENCE);
        SCParameter time = new SCParameter(new SCTime("t", false, false, new ArrayList<>(), false),
                SCREFERENCETYPE.BYREFERENCE);
        
        List<SCParameter> nbTransBwParams = new ArrayList<>();
        nbTransBwParams.add(tran);
        nbTransBwParams.add(phase);
        nbTransBwParams.add(time);
        SCFunction nb_transport_bw = new SCFunction(name, "int", nbTransBwParams);
        return nb_transport_bw;
    }
    
    private SCFunction generateInvalidateDirectMemPtr() {
        SCParameter start_range =
                new SCParameter(new SCSimpleType("start_range", "uint64", false, false, new ArrayList<>()),
                        SCREFERENCETYPE.BYVALUE);
        SCParameter end_range =
                new SCParameter(new SCSimpleType("end_range", "uint64", false, false, new ArrayList<>()),
                        SCREFERENCETYPE.BYVALUE);
        
        List<SCParameter> invalDirMemPtrParams = new ArrayList<>();
        invalDirMemPtrParams.add(start_range);
        invalDirMemPtrParams.add(end_range);
        
        SCFunction invalidate_direct_mem_ptr =
                new SCFunction("invalidate_direct_mem_ptr", "void", invalDirMemPtrParams);
        return invalidate_direct_mem_ptr;
    }
    
}
