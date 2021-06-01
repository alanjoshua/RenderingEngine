package Kurama.ComponentSystem.components.constraintGUI.interactionConstraints;

import Kurama.ComponentSystem.components.constraintGUI.BoundInteractionMessage;
import Kurama.ComponentSystem.components.constraintGUI.Boundary;
import Kurama.ComponentSystem.components.constraintGUI.ConstraintVerificationData;

public class MaxXPos implements InteractionConstraint {

    public float maxXPos;

    // relative to parent
    public MaxXPos(float maxPos) {
        maxXPos = maxPos;
    }

    @Override
    public boolean isValid(Boundary boundary, BoundInteractionMessage info, ConstraintVerificationData verificationData) {

        if(info.parentMoveDir == 1) {
            return true; // Don't check anything if not moving vertically
        }

        float cur = verificationData.pos.get(0) + verificationData.width/2f + boundary.parent.width/2f;
        float max = boundary.parent.width * maxXPos;

        if(cur >= max) {
            return false;
        }
        else {
            return true;
        }

    }
}