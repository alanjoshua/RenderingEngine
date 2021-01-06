package Kurama.GUI.constraints;

import Kurama.GUI.Component;

public class PosXAttachPercent extends Constraint {

    float posXPercent;

    public PosXAttachPercent(float posXPercent) {
        this.posXPercent = posXPercent;
    }

    @Override
    public void solveConstraint(Component parent, Component current) {
        float newX = parent.pos.get(0) + parent.width*posXPercent;
        current.pos.setDataElement(0, newX);
    }
}
