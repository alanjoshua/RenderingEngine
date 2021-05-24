package Kurama.ComponentSystem.components.constraintGUI;

import Kurama.ComponentSystem.automations.WidthPercent;
import Kurama.ComponentSystem.components.Component;
import Kurama.Math.Vector;
import Kurama.game.Game;

public class HorizontalBoundary extends Boundary {

    public HorizontalBoundary(Game game, Component parent, String identifier) {
        super(game, parent, identifier);
        this.color = new Vector(1,1,1,1);
        this.height = 10;
        this.initAutomations.add(new WidthPercent(1));
    }

    @Override
    public boolean canBeMoved(BoundMoveDataPack info) {
        return true;
    }

    public void move(BoundMoveDataPack info) {

        if(info.isParent) {
            pos = pos.add(new Vector(0, (int) info.deltaMove, 0));
        }
        else {
            pos = pos.add(new Vector((int) info.deltaMove,0, 0));
        }

        info.isParent = false;

        negativeAttachments.forEach(b -> b.move(info));
        positiveAttachments.forEach(b -> b.move(info));
    }
}
