package editor;

import Kurama.ComponentSystem.automations.PosXYTopLeftAttachPercent;
import Kurama.ComponentSystem.automations.WidthHeightPercent;
import Kurama.ComponentSystem.components.Component;
import Kurama.ComponentSystem.components.MasterWindow;
import Kurama.ComponentSystem.components.Rectangle;
import Kurama.ComponentSystem.components.constraints.ConstraintComponent;
import Kurama.Math.Vector;
import Kurama.display.Display;
import Kurama.display.DisplayLWJGL;
import Kurama.game.Game;
import Kurama.inputs.InputLWJGL;
import Kurama.renderingEngine.RenderingEngineGL;
import Kurama.renderingEngine.defaultRenderPipeline.DefaultRenderPipeline;
import Kurama.renderingEngine.ginchan.Gintoki;
import Kurama.scene.Scene;

import static org.lwjgl.glfw.GLFW.*;

public class Editor extends Game {

    Component hierarchyWindow;
    Component sceneWindow;

    public Editor(String threadName) {
        super(threadName);
    }

    @Override
    public void init() {

        scene = new Scene(this);
        renderingEngine = new RenderingEngineGL(this);

        display = new DisplayLWJGL(this);
        display.displayMode = Display.DisplayMode.WINDOWED;
        display.startScreen();

        input = new InputLWJGL(this, (DisplayLWJGL) display);

        scene.renderPipeline = new DefaultRenderPipeline(this, null,"sceneRenderer");
        ((RenderingEngineGL)renderingEngine).sceneRenderPipeline = scene.renderPipeline;
        ((RenderingEngineGL)renderingEngine).guiRenderPipeline = new Gintoki(this, null,"Gintoki");
        renderingEngine.init(scene);

        initGUI();
    }

    public void initGUI() {

        rootGuiComponent = new MasterWindow(this, display, input,"masterWindow");
        rootGuiComponent
                .setColor(new Vector(0,1,0,0.5f))
                .setContainerVisibility(false);

        hierarchyWindow =
                 new ConstraintComponent(this, rootGuiComponent, "hierarchyWindow")
                 .addConstraint(new WidthHeightPercent(0.1f, 1f))
                 .addConstraint(new PosXYTopLeftAttachPercent(0,0))
                 .setColor(new Vector(1,1,1,1));
        rootGuiComponent.addChild(hierarchyWindow);

//        var testComp =
//                new Rectangle(this, hierarchyWindow, "testComp")


        sceneWindow =
                 new Rectangle(this, rootGuiComponent, "sceneWindow")
                .addConstraint(new WidthHeightPercent(0.9f, 1f))
                .addConstraint(new PosXYTopLeftAttachPercent(0.1f, 0))
                .setColor(new Vector(1,0,0,1));
        rootGuiComponent.addChild(sceneWindow);
    }

    @Override
    public void cleanUp() {
        rootGuiComponent.cleanUp();
        renderingEngine.cleanUp();
        scene.cleanUp();
    }

    @Override
    public void tick() {
        rootGuiComponent.tick(null, rootGuiComponent.input, timeDelta);

        if(glfwWindowShouldClose(((DisplayLWJGL)display).getWindow())) {
            programRunning = false;
        }

        scene.rootSceneComp.children.forEach(m -> m.tick(null, input, timeDelta));

        input.reset();
    }

    @Override
    public void render() {
        ((RenderingEngineGL)renderingEngine).render(scene, rootGuiComponent);
        glfwSwapBuffers(((DisplayLWJGL)display).getWindow());
        glfwPollEvents();
        input.poll();
        scene.hasMatLibraryUpdated = false;
    }

}
