package Kurama.ComponentSystem.components;

import Kurama.ComponentSystem.animations.Animation;
import Kurama.ComponentSystem.automations.Automation;
import Kurama.Math.Matrix;
import Kurama.Math.Quaternion;
import Kurama.Math.Vector;
import Kurama.Mesh.Texture;
import Kurama.game.Game;
import Kurama.inputs.Input;

import java.util.ArrayList;
import java.util.List;

public abstract class Component {

    public Vector pos = new Vector(new float[]{0,0,0});
    public Quaternion orientation = Quaternion.getAxisAsQuat(1,0,0,0);
    public int width;
    public int height;
    public Matrix objectToWorldMatrix = Matrix.getIdentityMatrix(4);
    public Matrix objectToWorldNoScaleMatrix = Matrix.getIdentityMatrix(4);

    public Vector color = new Vector(0,0,0,1);
    public Vector overlayColor = null;
    public Texture texture = null;
    public float alphaMask = 1;

    public String identifier;
    public boolean isContainerVisible = true;
    public boolean shouldRenderGroup = true;
    public boolean shouldTriggerOnClick = false;
    public boolean shouldTriggerOnMouseOver = false;
    public boolean shouldTriggerOnMouseLeave = false;

    public boolean isResizedOrMoved = true;
    public boolean isClicked = false;
    public boolean isClickDragged = false;
    public boolean isClickedOutside = false;
    public boolean currentIsMouseOver = false;
    public boolean isMouseLeft = false;
    protected boolean previousIsMouseOver = false;
    public boolean isKeyInputFocused = false;
    protected boolean shouldForceCheckKeyInputFocusUpdate = false;

    public Component parent;
    public Game game;
    public List<Component> children = new ArrayList<>();

    // Constraints are updated only when components are resized.
    // WARNING: ALWAYS ADD SIZE CONSTRAINTS BEFORE POSITIONAL CONSTRAINTS
    public List<Automation> constraints = new ArrayList<>();
    public List<Automation> globalChildrenConstraints = new ArrayList<>();

    public boolean isFirstRun = true;
    public List<Kurama.ComponentSystem.automations.Automation> initAutomations = new ArrayList<>();
    public List<Kurama.ComponentSystem.automations.Automation> automations = new ArrayList<>();
    public List<Kurama.ComponentSystem.automations.Automation> onClickActions = new ArrayList<>();
    public List<Kurama.ComponentSystem.automations.Automation> onClickedOutsideActions = new ArrayList<>();
    public List<Kurama.ComponentSystem.automations.Automation> onClickDraggedActions = new ArrayList<>();
    public List<Kurama.ComponentSystem.automations.Automation> onClickDragEndActions = new ArrayList<>();
    public List<Kurama.ComponentSystem.automations.Automation> onMouseOverActions = new ArrayList<>();
    public List<Kurama.ComponentSystem.automations.Automation> onMouseLeaveActions = new ArrayList<>();
    public List<Kurama.ComponentSystem.automations.Automation> onKeyInputFocused = new ArrayList<>();
    public List<Kurama.ComponentSystem.automations.Automation> onKeyInputFocusedInit = new ArrayList<>();
    public List<Kurama.ComponentSystem.automations.Automation> onKeyInputFocusLossInit = new ArrayList<>();

    public List<Animation> animations = new ArrayList<>();

    public List<Kurama.ComponentSystem.automations.Automation> finalAutomationsBeforePosConfirm = new ArrayList<>();
    public List<Kurama.ComponentSystem.automations.Automation> finalAutomationsAfterPosConfirm = new ArrayList<>();
    public List<Kurama.ComponentSystem.automations.Automation> automationsAfterChildTick = new ArrayList<>();

    public Component(Game game, Component parent, String identifier) {
        this.identifier = identifier;
        this.parent = parent;
        this.game = game;
    }

    public Matrix getOrthoProjection() {
        return Matrix.buildOrtho2D(0, width, height, 0);
    }

    public Component findComponent(String id) {
        if(this.identifier.equals(id))
            return this;

        for(var child: children) {
            var res = child.findComponent(id);
            if(res != null)
                return res;
        }
        return null;
    }

    public Component getRoot() {
        if(this.parent == null)
            return this;
        this.parent.getRoot();
        return null; // this would theoretically never happen
    }

    public Component addOnClickAction(Kurama.ComponentSystem.automations.Automation action) {
        onClickActions.add(action);
        shouldTriggerOnClick = true;
        return this;
    }

    public Component addChild(Component child) {
        children.add(child);
        child.parent = this;
        return this;
    }

    public Component addOnClickOutsideAction(Kurama.ComponentSystem.automations.Automation action) {
        onClickedOutsideActions.add(action);
        shouldTriggerOnClick = true;
        return this;
    }

    public Component addOnKeyInputFocusedAction(Kurama.ComponentSystem.automations.Automation action) {
        onKeyInputFocused.add(action);
        return this;
    }

    public Component addOnClickDraggedAction(Kurama.ComponentSystem.automations.Automation action) {
        onClickDraggedActions.add(action);
        shouldTriggerOnClick = true;
        return this;
    }

    public Component addOnClickDragEndedAction(Kurama.ComponentSystem.automations.Automation action) {
        onClickDragEndActions.add(action);
        shouldTriggerOnClick = true;
        return this;
    }

    public Component addOnKeyInputFocusedInitAction(Kurama.ComponentSystem.automations.Automation action) {
        onKeyInputFocusedInit.add(action);
        return this;
    }

    public Component addOnKeyInputFocusLossInitAction(Kurama.ComponentSystem.automations.Automation action) {
        onKeyInputFocusLossInit.add(action);
        return this;
    }

    public Component addOnMouseOvertAction(Kurama.ComponentSystem.automations.Automation action) {
        this.shouldTriggerOnMouseOver = true;
        onMouseOverActions.add(action);
        return this;
    }

    public Component addOnMouseLeftAction(Kurama.ComponentSystem.automations.Automation action) {
        this.shouldTriggerOnMouseLeave = true;
        onMouseLeaveActions.add(action);
        return this;
    }

    public Component addAnimation(Animation animation) {
        animations.add(animation);
        return this;
    }

    public Component addConstraint(Automation constraint) {
        this.constraints.add(constraint);
        return this;
    }

    public Component addAutomation(Kurama.ComponentSystem.automations.Automation automation) {
        this.automations.add(automation);
        return this;
    }

    public Component addInitAutomation(Kurama.ComponentSystem.automations.Automation automation) {
        this.initAutomations.add(automation);
        return this;
    }

    public Component addAutomationAfterChildTick(Kurama.ComponentSystem.automations.Automation automation) {
        this.automationsAfterChildTick.add(automation);
        return this;
    }

    public Component setColor(Vector color) {
        this.color = color;
        return this;
    }

    public Component setWidth(int width) {
        this.width = width;
        return this;
    }

    public Component setHeight(int height) {
        this.height = height;
        return this;
    }

    public Component setAlphaMask(float alphaMask) {
        this.alphaMask = alphaMask;
        return this;
    }

    public Component setTexture(Texture tex) {
        this.texture = tex;
        return this;
    }

    public Component setOverlayColor(Vector color) {
        this.overlayColor = color;
        return this;
    }

    public Vector getTranslatedPos() {
        return objectToWorldMatrix.getColumn(3).removeDimensionFromVec(3);
    }

    public Component setShouldTriggerOnClick(boolean isClickable) {
        this.shouldTriggerOnClick = isClickable;
        return this;
    }

    public Component setKeyInputFocused(boolean isKeyboardInputFocusable) {
        shouldForceCheckKeyInputFocusUpdate = !(isKeyInputFocused == isKeyboardInputFocusable);
        this.isKeyInputFocused = isKeyboardInputFocusable;
        return this;
    }

    public Component setShouldTriggerOnMouseOver(boolean shouldTriggerOnMouseOver) {
        this.shouldTriggerOnMouseOver = shouldTriggerOnMouseOver;
        return this;
    }

    public Component setShouldTriggerOnMouseLeave(boolean shouldTriggerOnMouseLeave) {
        this.shouldTriggerOnMouseLeave = shouldTriggerOnMouseLeave;
        return this;
    }

    public Component setContainerVisibility(boolean isVisible) {
        this.isContainerVisible = isVisible;
        return this;
    }

    public Component setShouldRenderGroup(boolean shouldRender) {
        this.shouldRenderGroup = shouldRender;
        return this;
    }

    public void setupTransformationMatrices() {
        Matrix rotationMatrix = orientation.getRotationMatrix();
        Matrix scalingMatrix = Matrix.getDiagonalMatrix(new Vector(width, height, 1));
        Matrix rotScalMatrix = rotationMatrix.matMul(scalingMatrix);

        objectToWorldNoScaleMatrix = rotationMatrix.addColumn(pos);
        objectToWorldNoScaleMatrix = objectToWorldNoScaleMatrix.addRow(new Vector(new float[]{0, 0, 0, 1}));

        objectToWorldMatrix = rotScalMatrix.addColumn(pos);
        objectToWorldMatrix = objectToWorldMatrix.addRow(new Vector(new float[]{0, 0, 0, 1}));


        if(parent!=null) {
            objectToWorldNoScaleMatrix = parent.objectToWorldNoScaleMatrix.matMul(objectToWorldNoScaleMatrix);
            objectToWorldMatrix = parent.objectToWorldNoScaleMatrix.matMul(objectToWorldMatrix);
        }
        else {
            pos = new Vector(new float[]{width/2f, height/2f, 0});
        }

    }

    public Matrix getObjectToWorldMatrix() {
        return objectToWorldMatrix;
    }

    public void onClick(Input input, float timeDelta) {
        for(var actions: onClickActions) {
            actions.run(this, input, timeDelta);
        }
    }

    public void onClickedOutside(Input input, float timeDelta) {
        for(var actions: onClickedOutsideActions) {
            actions.run(this, input, timeDelta);
        }
    }

    public void onClickDragged(Input input, float timeDelta) {
        for(var actions: onClickDraggedActions) {
            actions.run(this, input, timeDelta);
        }
    }

    public void onClickDragEnd(Input input, float timeDelta) {
        for(var actions: onClickDragEndActions) {
            actions.run(this, input, timeDelta);
        }
    }

    public void onMouseOver(Input input, float timeDelta) {
        for(var actions: onMouseOverActions) {
            actions.run(this, input, timeDelta);
        }
    }

    public void onMouseLeave(Input input, float timeDelta) {
        for(var actions: onMouseLeaveActions) {
            actions.run(this, input, timeDelta);
        }
    }

    public void onKeyFocus(Input input, float timeDelta) {
        for(var actions: onKeyInputFocused) {
            actions.run(this, input, timeDelta);
        }
    }

    public void onKeyFocusInit(Input input, float timeDelta) {
        for(var actions: onKeyInputFocusedInit) {
            actions.run(this, input, timeDelta);
        }
    }

    public void onKeyFocusLossInit(Input input, float timeDelta) {
        for(var actions: onKeyInputFocusLossInit) {
            actions.run(this, input, timeDelta);
        }
    }

    // A default component can't have mouse over
    public boolean isMouseOverComponent(Input input) { return false; }

    public boolean isClicked(Input input, boolean isMouseOver) {
        if(shouldTriggerOnClick && input.isCursorEnabled && input.isLeftMouseButtonPressed) {
            return isMouseOver;
        }
        return false;
    }

    public boolean isClickDragged(Input input, boolean isClicked, boolean isClickedOutside, boolean previousIsClickDragged) {
        if(isClicked) return true;
        else if(previousIsClickDragged && input.isLeftMouseButtonPressed()) return true;
        else return false;
    }

    public boolean isClickedOutside(Input input, boolean isMouseOver) {
        if(shouldTriggerOnClick && input.isLeftMouseButtonPressed) {
            return !isMouseOver;
        }
        return false;
    }

    public boolean isMouseLeft(Input input, boolean isMouseOver) {
        if(previousIsMouseOver && !isMouseOver) {
            return true;
        }
        else {
            return false;
        }
    }

    public void tick(List<Automation> parentGlobalConstraints, Input input, float timeDelta) {

        if(!shouldRenderGroup) {
            return;
        }

        if(isFirstRun) {
            initAutomations.forEach(a -> a.run(this, input, timeDelta));
            isFirstRun = false;
            isResizedOrMoved = true;
        }

        isClicked = false; // Reset before processing inputs for current frame
        currentIsMouseOver = false;
        isMouseLeft = false;
        isClickedOutside = false;
        boolean previousKeyFocus = isKeyInputFocused;
        boolean previousClickDragged = isClickDragged;

        currentIsMouseOver = isMouseOverComponent(input);  // This should always be first, since its result is used by isClicked
        isClicked = isClicked(input, currentIsMouseOver);
        isMouseLeft = isMouseLeft(input, currentIsMouseOver);
        isClickedOutside = isClickedOutside(input, currentIsMouseOver);
        isClickDragged = isClickDragged(input, isClicked, isClickedOutside, isClickDragged);

        // Constraints are updated only when components are resized.
        // WARNING: ALWAYS ADD SIZE CONSTRAINTS BEFORE POSITIONAL CONSTRAINTS
        if(this.isResizedOrMoved || (parent != null && parent.isResizedOrMoved)) {
            this.isResizedOrMoved = true;
            for (var constraint : constraints) {
                constraint.run(this, input, timeDelta);
            }

            if (parentGlobalConstraints != null) {
                for (var globalConstraints : parentGlobalConstraints) {
                    globalConstraints.run(this, input, timeDelta);
                }
            }
        }

        for(var automation: automations) {
            automation.run(this, input, timeDelta);
        }

        List<Animation> toBeRemoved = new ArrayList<>();
        for(var anim: animations) {
            anim.run(this, input, timeDelta);
            if(anim.hasAnimEnded) {
                toBeRemoved.add(anim);
            }
        }
        animations.removeAll(toBeRemoved);

        finalAutomationsBeforePosConfirm.forEach(a -> a.run(this, input, timeDelta));
        setupTransformationMatrices();  // This finalised transformation matrices, and other positional information. The mouse events should not directly change the positional information in this tick cycle
        finalAutomationsAfterPosConfirm.forEach(a -> a.run(this, input, timeDelta));

        for(var child: children) {
            child.tick(globalChildrenConstraints, input, timeDelta);
        }

        if(isClicked) {
            onClick(input, timeDelta);
        }

        if(currentIsMouseOver) {
            onMouseOver(input, timeDelta);
            previousIsMouseOver = true;
        }

        if(isMouseLeft) {
            onMouseLeave(input, timeDelta);
            previousIsMouseOver = false;
        }

        if(isClickedOutside) {
            onClickedOutside(input, timeDelta);
        }

        // Called whenever component has keyboard focus
        if(isKeyInputFocused) {
            onKeyFocus(input, timeDelta);
        }

        if(isClickDragged) {
            onClickDragged(input, timeDelta);
        }

        if(previousClickDragged != isClickDragged) {
            onClickDragEnd(input, timeDelta);
        }

        // Called only once right after keyboard focus is lost or gained
        if(previousKeyFocus != isKeyInputFocused || shouldForceCheckKeyInputFocusUpdate) {
            if(!isKeyInputFocused) {
                onKeyFocusLossInit(input, timeDelta);
            }
            else {
                onKeyFocusInit(input, timeDelta);
            }
            shouldForceCheckKeyInputFocusUpdate = false;
        }

        automationsAfterChildTick.forEach(a -> a.run(this, input, timeDelta));
        isResizedOrMoved = false;

    }

    // Should probably be overwritten, or more control should be specified
    public boolean isValidLocation(Vector newPos, int width, int height) {
            return true;
    }

}