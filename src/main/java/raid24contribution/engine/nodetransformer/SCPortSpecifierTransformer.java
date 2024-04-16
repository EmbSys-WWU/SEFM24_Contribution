package raid24contribution.engine.nodetransformer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Node;
import raid24contribution.engine.Environment;
import raid24contribution.engine.typetransformer.KnownTypeTransformer;
import raid24contribution.engine.util.NodeUtil;
import raid24contribution.sc_model.SCPORTSCSOCKETTYPE;

/**
 * first we get the portType according to this type, we set the lastPortSocketType in the
 * environment and handle the childnodes afterwards we create a new known-type because the
 * channel-types are knowntypes
 *
 */
public class SCPortSpecifierTransformer extends AbstractNodeTransformer {
    
    private static Logger logger = LogManager.getLogger(SCPortSpecifierTransformer.class.getName());
    
    @Override
    public void transformNode(Node node, Environment e) {
        String portType = NodeUtil.getAttributeValueByName(node, "name");
        e.setFoundMemberType("PortSocket");
        handleChildNodes(node, e);
        e.setLastPortSocketType(null);
        
        if (portType.equals("sc_port")) {
            e.setLastPortSocketType(SCPORTSCSOCKETTYPE.SC_PORT);
            return;
        } else if (portType.equals("sc_in")) {
            e.setLastPortSocketType(SCPORTSCSOCKETTYPE.SC_IN);
            portType = "sc_signal";
            handleChildNodes(node, e);
        } else if (portType.equals("sc_out")) {
            e.setLastPortSocketType(SCPORTSCSOCKETTYPE.SC_OUT);
            portType = "sc_signal";
            handleChildNodes(node, e);
        } else if (portType.equals("sc_inout")) {
            e.setLastPortSocketType(SCPORTSCSOCKETTYPE.SC_INOUT);
            portType = "sc_signal";
            handleChildNodes(node, e);
        } else if (portType.equals("sc_fifo_in")) {
            e.setLastPortSocketType(SCPORTSCSOCKETTYPE.SC_FIFO_IN);
            portType = "sc_fifo";
            handleChildNodes(node, e);
        } else if (portType.equals("sc_fifo_out")) {
            e.setLastPortSocketType(SCPORTSCSOCKETTYPE.SC_FIFO_OUT);
            portType = "sc_fifo";
            handleChildNodes(node, e);
        } else if (portType.equals("tlm_initiator_socket") || portType.equals("tlm_target_socket")) {
            e.setLastPortSocketType(SCPORTSCSOCKETTYPE.SC_SOCKET);
            portType = "tlm_fw_bw_if";
            handleChildNodes(node, e);
        } else {
            e.setLastPortSocketType(null);
            logger.error("{}: {}: Unknown port type.", NodeUtil.getFixedAttributes(node), portType);
            return;
        }
        
        
        
        KnownTypeTransformer tpTrans = e.getTransformerFactory().getTypeTransformer(portType, e);
        if (tpTrans != null) {
            tpTrans.createType(e);
        } else {
            logger.error("{}: Configuration error: Can not find implementation for type {}.",
                    NodeUtil.getFixedAttributes(node), portType);
        }
    }
    
}
