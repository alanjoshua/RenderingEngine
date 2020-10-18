package engine.display;

import java.awt.*;

import engine.game.Game;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;

public class DisplayLWJGL extends Display {

    private long window;
    private int WIDTH = defaultWindowedWidth;
    private int HEIGHT = defaultWindowedHeight;
    private GLCapabilities capabilities;

    public DisplayLWJGL(int defaultWindowedWidth, int defaultWindowedHeight, Game game) {
        super(defaultWindowedWidth,defaultWindowedHeight,game);
    }

    public DisplayLWJGL(Game game) {
        super(game);
    }

    @Override
    public void init() {
        //System.setProperty("java.awt.headless", "true"); //To ensure fonttexture loading works properly in OSX
        startGLFW();
        initWindow();
    }

    public void startScreen() {
        //initWindow();
        if (displayMode == DisplayMode.FULLSCREEN) setFullScreen();
        else setWindowedMode();
    }

    public void toggleWindowModes() {
        if (displayMode == DisplayMode.FULLSCREEN) {
            setWindowedMode();
            displayMode = DisplayMode.WINDOWED;
        }
        else {
            setFullScreen();
            displayMode = DisplayMode.FULLSCREEN;
        }
    }

    public int getWidth() {
        return WIDTH;
    }

    public int getHeight() {
        return HEIGHT;
    }

    public void cleanUp() {
        removeWindow();
        removeGLFW();
    }

    public int getRefreshRate() {
        GLFWVidMode vid = glfwGetVideoMode(glfwGetPrimaryMonitor());
        return vid.refreshRate();
    }

    public void setClearColor(float r, float g, float b, float a) {
        glClearColor(r,g,b,a);
    }

    public void initWindow() {
        System.out.println("initing window");
        // Configure GLFW
        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation

//        glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, GLFW_TRUE);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 6);
//        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);

        // Create the window
        window = glfwCreateWindow(defaultWindowedWidth, defaultWindowedHeight, "Hello World!", NULL, NULL);
        if ( window == NULL )
            throw new RuntimeException("Failed to create the GLFW window");

        if(glfwRawMouseMotionSupported()) {
            glfwSetInputMode(window,GLFW_RAW_MOUSE_MOTION,GLFW_TRUE);
        }

        // Make the OpenGL context current

        glfwMakeContextCurrent(window);
        capabilities = GL.createCapabilities();

//        GLUtil.setupDebugMessageCallback();
        // Enable v-sync
        glfwSwapInterval(1);
    }

    public int getDPI() {
        return Toolkit.getDefaultToolkit().getScreenResolution();
    }

    public float getScalingRelativeToDPI() {
        if(OS.indexOf("mac") > 0) {
            int dpi = getDPI();
            return (float)(dpi/macDPI);
        }

        else {
            int dpi = getDPI();
            return  (float)(dpi/winDPI);
        }
    }

    public void setCapabilities() {
        GL.setCapabilities(capabilities);
    }

    public void setFullScreen() {
        GLFWVidMode vid = glfwGetVideoMode(glfwGetPrimaryMonitor());
        glfwSetWindowMonitor(window, glfwGetPrimaryMonitor(),0,0,vid.width(),vid.height(),vid.refreshRate());
        glfwWindowHint(GLFW_VISIBLE, GLFW_TRUE);
        glfwShowWindow(window);
        WIDTH = vid.width();
        HEIGHT = vid.height();
        glViewport(0,0,WIDTH,HEIGHT);

    }

    public void setWindowedMode() {
        GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        glfwSetWindowMonitor(window, NULL,0,0,defaultWindowedWidth,defaultWindowedHeight,vidmode.refreshRate());

        glfwSetWindowPos(
                window,
                (vidmode.width() - defaultWindowedWidth) / 2,
                (vidmode.height() - defaultWindowedHeight) / 2
        );
        glfwWindowHint(GLFW_VISIBLE, GLFW_TRUE);
        glfwShowWindow(window);

        WIDTH = defaultWindowedWidth;
        HEIGHT = defaultWindowedHeight;
    }

    public void setWindowedMode(int width, int height) {
        GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        glfwSetWindowMonitor(window, NULL,0,0,width,height,vidmode.refreshRate());

        glfwSetWindowPos(
                    window,
                    (vidmode.width() - width) / 2,
                    (vidmode.height() - height) / 2
            );

        glfwWindowHint(GLFW_VISIBLE, GLFW_TRUE);
        glfwShowWindow(window);

        WIDTH = defaultWindowedWidth;
        HEIGHT = defaultWindowedHeight;
    }

    protected void removeWindow() {
        if(window != NULL) {
            glfwFreeCallbacks(window);
            glfwDestroyWindow(window);
        }

        WIDTH = defaultWindowedWidth;
        HEIGHT = defaultWindowedHeight;
    }

    protected void startGLFW() {
        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        System.out.println("started GLFW");
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if ( !glfwInit() )
            throw new IllegalStateException("Unable to initialize GLFW");

    }

    protected void removeGLFW() {
        // Terminate GLFW and free the error callback
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    public void setWIDTH(int w) {
        WIDTH = w;
    }

    public void setHEIGHT(int h) {
        HEIGHT = h;
    }

    public long getWindow() {
        return window;
    }

    public void disableCursor() {
        glfwSetInputMode(window,GLFW_CURSOR,GLFW_CURSOR_DISABLED);
    }

    public void enableCursor() {
        glfwSetInputMode(window,GLFW_CURSOR,GLFW_CURSOR_NORMAL);
    }

}
