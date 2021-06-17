package Kurama.ComponentSystem.components;

import Kurama.ComponentSystem.automations.*;
import Kurama.ComponentSystem.components.constraintGUI.Boundary;
import Kurama.ComponentSystem.components.constraintGUI.ConstraintComponent;
import Kurama.ComponentSystem.components.constraintGUI.stretchSystem.FixToBorder;
import Kurama.ComponentSystem.components.constraintGUI.stretchSystem.StretchMessage;
import Kurama.display.Display;
import Kurama.game.Game;
import Kurama.inputs.Input;

public class MasterWindow extends ConstraintComponent {

    public Display display;
    public Input input;

    public MasterWindow(Game game, Display display, Input input, String identifier) {
        super(game, null, identifier);
        this.display = display;
        this.input = input;

        addInitAutomation((cur, in, t) -> init());

        onResizeAutomations.add((cur, in, t) -> {

            var dw = display.windowResolution.geti(0) - this.getWidth();
            var dh = display.windowResolution.geti(1) - this.getHeight();

            // becoming bigger
            this.setWidth(display.windowResolution.geti(0));
            this.setHeight(display.windowResolution.geti(1));

            var left = getBoundary(identifier+"_left");
            var right = getBoundary(identifier+"_right");
            var top = getBoundary(identifier+"_top");
            var bottom = getBoundary(identifier+"_bottom");

            if(left != null) {

                left.interact(new StretchMessage(-(dw/2f), 0, null, true), null, -1);
                right.interact(new StretchMessage((dw/2f), 0, null, true), null, -1);
                top.interact(new StretchMessage(0, -(dh/2f), null, true), null, -1);
                bottom.interact(new StretchMessage(0, (dh/2f), null, true), null, -1);


//                left.initialiseInteraction(-(dw / 2f), 0);
//                getBoundary(identifier + "_right").initialiseInteraction(dw / 2f, 0);
//                getBoundary(identifier + "_top").initialiseInteraction(0, -(dh / 2f));
//                getBoundary(identifier + "_bottom").initialiseInteraction(0, dh / 2f);
            }
        });

        display.resizeEvents.add( () -> this.isResizedOrMoved = true);
    }

    public void cleanUp() {
        display.cleanUp();
    }

//    Provides the same API as Display
    public int getDPI() { return display.getDPI(); }
    public float getScalingRelativeToDPI() { return display.getScalingRelativeToDPI();}
    public void toggleWindowModes() {
//            isResizedOrMoved=true;
//            Logger.log();
            display.toggleWindowModes();
    }
    public void setFullScreen() {display.setFullScreen();}
    public void setWindowedMode() {display.setWindowedMode();}
    public void setWindowedMode(int width, int height) {display.setWindowedMode(width, height);}
    public void disableCursor() {display.disableCursor();}
    public void enableCursor() {display.enableCursor();}
    public float getRefreshRate() {return display.getRefreshRate();}


    // Initialises surrounding boundaries
    public void init() {

        // These are the default borders around the component.
        // Width and height (for vertical and horizontal boundaries respectively) are set to 0 by default

        var l = new Boundary(this.game, this, identifier+"_left", Boundary.BoundaryOrient.Vertical, false, configurator);
        var t = new Boundary(this.game, this, identifier+"_top", Boundary.BoundaryOrient.Horizontal, false, configurator);

        var r = new Boundary(this.game, this, identifier+"_right", Boundary.BoundaryOrient.Vertical, false, configurator);
        var b = new Boundary(this.game, this, identifier+"_bottom", Boundary.BoundaryOrient.Horizontal, false, configurator);

//        r.addInitAutomation(new WidthPercent(0f));
//        l.addInitAutomation(new WidthPercent(0f));
//        t.addInitAutomation(new HeightPercent(0f));
//        b.addInitAutomation(new HeightPercent(0f));

        r.addInitAutomation(new HeightPercent(1f));
        l.addInitAutomation(new HeightPercent(1f));
        t.addInitAutomation(new WidthPercent(1f));
        b.addInitAutomation(new WidthPercent(1f));

        r.addInitAutomation(new PosXYBottomRightAttachPercent(0f, 0f));
        t.addInitAutomation(new PosXYTopLeftAttachPercent(0f, 0f));
        l.addInitAutomation(new PosXYTopLeftAttachPercent(0f, 0f));
        b.addInitAutomation(new PosYBottomAttachPercent(0f)).addInitAutomation(new PosXLeftAttachPercent(0f));

        addBoundary(l).addBoundary(r).addBoundary(t).addBoundary(b);

        l.addConnectedBoundary(t, 1, 0);
        l.addConnectedBoundary(b, 1, 1);
        r.addConnectedBoundary(t, 0, 0);
        r.addConnectedBoundary(b, 0, 1);

        l.addPreInteractionValidifier(new FixToBorder(FixToBorder.AttachPoint.left));
        r.addPreInteractionValidifier(new FixToBorder(FixToBorder.AttachPoint.right));
        t.addPreInteractionValidifier(new FixToBorder(FixToBorder.AttachPoint.top));
        b.addPreInteractionValidifier(new FixToBorder(FixToBorder.AttachPoint.bottom));

        l.addPostInteractionValidifier(new FixToBorder(FixToBorder.AttachPoint.left));
        r.addPostInteractionValidifier(new FixToBorder(FixToBorder.AttachPoint.right));
        t.addPostInteractionValidifier(new FixToBorder(FixToBorder.AttachPoint.top));
        b.addPostInteractionValidifier(new FixToBorder(FixToBorder.AttachPoint.bottom));

    }

}
