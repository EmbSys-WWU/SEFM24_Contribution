package raid24contribution.engine.nodetransformer;

import org.w3c.dom.Node;
import raid24contribution.engine.Environment;
import raid24contribution.engine.util.NodeUtil;
import raid24contribution.sc_model.SCFunction;
import raid24contribution.sc_model.expressions.GoalAnnotation;

public class CommentTransformer extends AbstractNodeTransformer {
	public void transformNode(Node node, Environment e) {
		if (node != null) {
			String name = NodeUtil.getAttributeValueByName(node, "name");
			if (name.equals("// GOAL")) {
				GoalAnnotation gA = new GoalAnnotation(node);
				e.getExpressionStack().add(gA);
			}
		}
	}
}
