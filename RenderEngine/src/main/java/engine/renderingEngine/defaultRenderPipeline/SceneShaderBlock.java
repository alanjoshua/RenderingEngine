package engine.renderingEngine.defaultRenderPipeline;

import engine.Effects.Material;
import engine.Math.Matrix;
import engine.Math.Vector;
import engine.Mesh.InstancedMesh;
import engine.Mesh.Mesh;
import engine.geometry.MeshBuilder;
import engine.lighting.DirectionalLight;
import engine.lighting.SpotLight;
import engine.model.AnimatedModel;
import engine.model.Model;
import engine.renderingEngine.LightDataPackage;
import engine.renderingEngine.RenderBlockInput;
import engine.renderingEngine.RenderingEngine;
import engine.renderingEngine.RenderingEngineGL;
import engine.scene.Scene;
import engine.shader.ShaderProgram;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.glBindFramebuffer;

public class SceneShaderBlock extends engine.renderingEngine.RenderBlock {

    String shadow_ShaderID = "shadow_shader";
    String scene_shader_id = "scene_shader";

    public ShaderProgram shadow_shader;
    public ShaderProgram scene_shader;

    public int MAX_DIRECTIONAL_LIGHTS = 5;
    public int MAX_SPOTLIGHTS = 10;
    public int MAX_POINTLIGHTS = 10;
    public int MAX_JOINTS = 150;
    protected Mesh axes;

    public SceneShaderBlock(String id) {
        super(id);
    }

    @Override
    public void setup(RenderBlockInput input) {
        axes = MeshBuilder.buildAxes();
        setupSceneShader();
        setupShadowShader();
    }

    @Override
    public void render(RenderBlockInput input) {
//        glCullFace(GL_FRONT);  //this means meshes with no back face will not cast shadows.
        ShadowDepthRenderPackage shadowPackage =  renderDepthMap(input.scene);
//        glCullFace(GL_BACK);

        glViewport(0,0,input.game.getDisplay().getWidth(),input.game.getDisplay().getHeight());
        RenderingEngineGL.clear();
        renderScene(input.scene,shadowPackage);
    }

    @Override
    public void cleanUp() {
        shadow_shader.cleanUp();
        scene_shader.cleanUp();
    }

    public void setupSceneShader() {

        scene_shader = new ShaderProgram(scene_shader_id);

        try {

            scene_shader.createVertexShader("src/main/java/engine/renderingEngine/defaultShaders/SceneVertexShader.glsl");
            scene_shader.createFragmentShader("src/main/java/engine/renderingEngine/defaultShaders/SceneFragmentShader.glsl");
            scene_shader.link();

            scene_shader.createUniform("projectionMatrix");

//            scene_shader.createUniformArray("modelLightViewMatrix",MAX_DIRECTIONAL_LIGHTS);
//            scene_shader.createUniformArray("modelSpotLightViewMatrix",MAX_SPOTLIGHTS);
            scene_shader.createUniformArray("directionalShadowMaps",MAX_DIRECTIONAL_LIGHTS);
            scene_shader.createUniformArray("spotLightShadowMaps",MAX_SPOTLIGHTS);
            scene_shader.createUniform("numDirectionalLights");
            scene_shader.createUniform("numberOfSpotLights");
            scene_shader.createUniform("isAnimated");
            scene_shader.createMaterialListUniform("materials","mat_textures","mat_normalMaps","mat_diffuseMaps","mat_specularMaps",26);
            scene_shader.createUniform("isInstanced");
            scene_shader.createUniform("ambientLight");

            scene_shader.createPointLightListUniform("pointLights",MAX_POINTLIGHTS);
            scene_shader.createDirectionalLightListUniform("directionalLights",MAX_DIRECTIONAL_LIGHTS);
            scene_shader.createSpotLightListUniform("spotLights",MAX_SPOTLIGHTS);

            scene_shader.createFogUniform("fog");
            scene_shader.createUniform("allDirectionalLightStatic");

            scene_shader.createUniformArray("worldToDirectionalLightMatrix", MAX_DIRECTIONAL_LIGHTS);
            scene_shader.createUniformArray("worldToSpotlightMatrix", MAX_SPOTLIGHTS);
            scene_shader.createUniformArray("jointMatrices", MAX_JOINTS);
            scene_shader.createUniform("modelToWorldMatrix");
            scene_shader.createUniform("worldToCam");

            scene_shader.createUniform("materialsGlobalLoc");
            scene_shader.createUniform("materialsAtlas");

        }catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void setupShadowShader() {
        shadow_shader = new ShaderProgram(shadow_ShaderID);
        try {
            shadow_shader.createVertexShader("src/main/java/engine/renderingEngine/defaultShaders/depthDirectionalLightVertexShader.glsl");
            shadow_shader.createFragmentShader("src/main/java/engine/renderingEngine/defaultShaders/depthDirectionalLightFragmentShader.glsl");
            shadow_shader.link();

            shadow_shader.createUniform("projectionMatrix");
            shadow_shader.createUniform("modelLightViewMatrix");
            shadow_shader.createUniformArray("jointMatrices", MAX_JOINTS);
            shadow_shader.createUniform("isAnimated");
            shadow_shader.createUniform("isInstanced");

        }catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public Matrix generateMaterialMatrix(List<Material> mats, List<Integer> altasOffsets) {
        float[][] res = new float[4][4];
        for(int i = 0;i < mats.size();i++) {
            int r = i / 4;
            int c = i % 4;
            res[r][c] = mats.get(i).globalSceneID;
            res[r+2][c] = altasOffsets.get(i);
        }
        return new Matrix(res);
    }

    public void renderScene(Scene scene, ShadowDepthRenderPackage shadowPackage) {

        boolean curShouldCull = true;
        int currCull = GL_BACK;

        glEnable(GL_CULL_FACE);
        glCullFace(currCull);

        ShaderProgram sceneShaderProgram = scene_shader;
        sceneShaderProgram.bind();

        Matrix worldToCam = scene.camera.getWorldToCam();
        Matrix projectionMatrix = scene.camera.getPerspectiveProjectionMatrix();

        sceneShaderProgram.setUniform("projectionMatrix",projectionMatrix);
        sceneShaderProgram.setUniform("worldToCam", worldToCam);

        sceneShaderProgram.setUniform("ambientLight",scene.ambientLight);

        LightDataPackage lights = RenderingEngine.processLights(scene.pointLights, scene.spotLights, scene.directionalLights, worldToCam);
        sceneShaderProgram.setUniform("spotLights",lights.spotLights);
        sceneShaderProgram.setUniform("pointLights",lights.pointLights);
        sceneShaderProgram.setUniform("directionalLights",lights.directionalLights);
        sceneShaderProgram.setUniform("numDirectionalLights",scene.directionalLights.size());
        sceneShaderProgram.setUniform("numberOfSpotLights",scene.spotLights.size());
        sceneShaderProgram.setUniform("fog", scene.fog);

        int offset = sceneShaderProgram.setAndActivateMaterials("materials","mat_textures",
                "mat_normalMaps","mat_diffuseMaps","mat_specularMaps",scene.materialLibrary,0);

        Vector c = new Vector(3,0);
        for(int i = 0;i < scene.directionalLights.size(); i++) {
            DirectionalLight l = scene.directionalLights.get(i);
            c = c.add(l.color.scalarMul(l.intensity));
            sceneShaderProgram.setUniform("worldToDirectionalLightMatrix["+i+"]",
                    l.shadowProjectionMatrix.matMul(shadowPackage.worldToDirectionalLights.get(i)));
        }
        sceneShaderProgram.setUniform("allDirectionalLightStatic", c);

        for(int i = 0;i < scene.spotLights.size(); i++) {
            SpotLight l = scene.spotLights.get(i);
            sceneShaderProgram.setUniform("worldToSpotlightMatrix["+i+"]",
                    l.shadowProjectionMatrix.matMul(shadowPackage.worldToSpotLights.get(i)));
        }

        offset = sceneShaderProgram.setAndActivateDirectionalShadowMaps("directionalShadowMaps", scene.directionalLights,offset);
        offset = sceneShaderProgram.setAndActivateSpotLightShadowMaps("spotLightShadowMaps", scene.spotLights, offset);

        for(String meshId :scene.shaderblock_mesh_model_map.get(blockID).keySet()) {

            Mesh mesh = scene.meshID_mesh_map.get(meshId);
            if (curShouldCull != mesh.shouldCull) {
                if(mesh.shouldCull) {
                    glEnable(GL_CULL_FACE);
                }
                else {
                    glDisable(GL_CULL_FACE);
                }
                curShouldCull = mesh.shouldCull;
            }

            if(currCull != mesh.cullmode) {
                glCullFace(mesh.cullmode);
                currCull = mesh.cullmode;
            }

            mesh.initRender();

            if(mesh instanceof InstancedMesh) {

                sceneShaderProgram.setUniform("isInstanced", 1);

                List<Model> models = new ArrayList<>();
                for (String modelId : scene.shaderblock_mesh_model_map.get(blockID).get(meshId).keySet()) {
                    Model model = scene.modelID_model_map.get(modelId);
                    if(model.shouldRender) {
                        models.add(model);
                    }
                }

                var inst_mesh = (InstancedMesh) mesh;
                var chunks = inst_mesh.getRenderChunks(models);
                for (var chunk: chunks) {
                    inst_mesh.instanceDataBuffer.clear();

                    for(Model m: chunk) {
                        Matrix objectToWorld = m.getObjectToWorldMatrix();
                        objectToWorld.setValuesToFloatBuffer(inst_mesh.instanceDataBuffer);

                        Vector matsGlobalLoc = new Vector(4, 0);
                        for(int i = 0;i < m.materials.get(meshId).size();i++) {
                            matsGlobalLoc.setDataElement(i, m.materials.get(meshId).get(i).globalSceneID);
                        }

                        Vector matsAtlas = new Vector(4, 0);
                        for(int i = 0;i < m.matAtlasOffset.get(meshId).size();i++) {
                            matsAtlas.setDataElement(i, m.matAtlasOffset.get(meshId).get(i));
                        }

                        for(var v: matsGlobalLoc.getData()) {
                            inst_mesh.instanceDataBuffer.put(v);
                        }
                        for(var v: matsAtlas.getData()) {
                            inst_mesh.instanceDataBuffer.put(v);
                        }
                    }
                    inst_mesh.instanceDataBuffer.flip();

                    glBindBuffer(GL_ARRAY_BUFFER, inst_mesh.instanceDataVBO);
                    glBufferData(GL_ARRAY_BUFFER, inst_mesh.instanceDataBuffer, GL_DYNAMIC_DRAW);

                    inst_mesh.render(chunk.size());
                }
            }

            else {  // Non instanced meshes

                sceneShaderProgram.setUniform("isInstanced", 0);

                for (String modelId : scene.shaderblock_mesh_model_map.get(blockID).get(meshId).keySet()) {
                    Model model = scene.modelID_model_map.get(modelId);

                    if (model.shouldRender) {

                        if (model instanceof AnimatedModel) {
                            scene_shader.setUniform("isAnimated", 1);

                            AnimatedModel anim = (AnimatedModel) model;
                            for (int i = 0; i < anim.currentJointTransformations.size(); i++) {
                                var matrix = anim.currentJointTransformations.get(i);
                                sceneShaderProgram.setUniform("jointMatrices[" + i + "]", matrix);
                            }
                        } else {
                            scene_shader.setUniform("isAnimated", 0);
                        }

                        Vector matsGlobalLoc = new Vector(4, 0);
                        for(int i = 0;i < model.materials.get(meshId).size();i++) {
                            matsGlobalLoc.setDataElement(i, model.materials.get(meshId).get(i).globalSceneID);
                        }

                        Vector matsAtlas = new Vector(4, 0);
                        for(int i = 0;i < model.matAtlasOffset.get(meshId).size();i++) {
                            matsAtlas.setDataElement(i, model.matAtlasOffset.get(meshId).get(i));
                        }

                        scene_shader.setUniform("materialsGlobalLoc", matsGlobalLoc);
                        scene_shader.setUniform("materialsAtlas", matsAtlas);
                        Matrix objectToWorld = model.getObjectToWorldMatrix();
                        scene_shader.setUniform("modelToWorldMatrix", objectToWorld);
//
                        mesh.render();
                    }
                }
            }

            mesh.endRender();
        }

        sceneShaderProgram.unbind();
    }

    public ShadowDepthRenderPackage renderDepthMap(Scene scene) {

        boolean curShouldCull = true;
        int currCull = GL_BACK;

        glEnable(GL_CULL_FACE);
        glCullFace(currCull);

        ShaderProgram depthShaderProgram = shadow_shader;
        depthShaderProgram.bind();
        List<Matrix> worldToDirectionalLights = new ArrayList<>();
        List<Matrix> worldToSpotLights = new ArrayList<>();

        for(int i =0;i < scene.directionalLights.size();i++) {
            DirectionalLight light = scene.directionalLights.get(i);
            glViewport(0,0, light.shadowMap.shadowMapWidth, light.shadowMap.shadowMapHeight);
            glBindFramebuffer(GL_FRAMEBUFFER, light.shadowMap.depthMapFBO);
            glClear(GL_DEPTH_BUFFER_BIT);

            depthShaderProgram.setUniform("projectionMatrix", light.shadowProjectionMatrix);

            Matrix worldToLight = light.getWorldToObject();
            worldToDirectionalLights.add(worldToLight);

            for(String meshId :scene.shaderblock_mesh_model_map.get(blockID).keySet()) {
                Mesh mesh = scene.meshID_mesh_map.get(meshId);

                if (curShouldCull != mesh.shouldCull) {
                    if(mesh.shouldCull) {
                        glEnable(GL_CULL_FACE);
                    }
                    else {
                        glDisable(GL_CULL_FACE);
                    }
                    curShouldCull = mesh.shouldCull;
                }

                if(currCull != mesh.cullmode) {
                    glCullFace(mesh.cullmode);
                    currCull = mesh.cullmode;
                }

                mesh.initRender();

                if(mesh instanceof InstancedMesh) {
                    depthShaderProgram.setUniform("isInstanced", 1);
                    var inst_mesh = (InstancedMesh)mesh;
                    var models = new ArrayList<Model>();

                    for (String modelId : scene.shaderblock_mesh_model_map.get(blockID).get(meshId).keySet()) {
                         var m = scene.modelID_model_map.get(modelId);
                         if(m.shouldRender && m.shouldCastShadow) {
                             models.add(m);
                         }
                    }
                    var chunks = inst_mesh.getRenderChunks(models);
                    for(var chunk: chunks) {
                        inst_mesh.instanceDataBuffer.clear();

                        for(Model m: chunk) {
                            Matrix objectToLight = worldToLight.matMul(m.getObjectToWorldMatrix());
                            objectToLight.setValuesToFloatBuffer(inst_mesh.instanceDataBuffer);
                            for(int counter = 0;counter < 8;counter++) {
                                inst_mesh.instanceDataBuffer.put(0f);
                            }
                        }
                        inst_mesh.instanceDataBuffer.flip();

                        glBindBuffer(GL_ARRAY_BUFFER, inst_mesh.instanceDataVBO);
                        glBufferData(GL_ARRAY_BUFFER, inst_mesh.instanceDataBuffer, GL_DYNAMIC_DRAW);

                        inst_mesh.render(chunk.size());
                    }
                }

                else {
                    depthShaderProgram.setUniform("isInstanced", 0);
                    for (String modelId : scene.shaderblock_mesh_model_map.get(blockID).get(meshId).keySet()) {
                        Model m = scene.modelID_model_map.get(modelId);

                        if (m instanceof AnimatedModel) {
                            depthShaderProgram.setUniform("isAnimated", 1);
//                        Logger.log("detecting animated model");
                            AnimatedModel anim = (AnimatedModel) m;
                            for (int j = 0; j < anim.currentJointTransformations.size(); j++) {
                                var matrix = anim.currentJointTransformations.get(j);
                                depthShaderProgram.setUniform("jointMatrices[" + j + "]", matrix);
                            }
                        } else {
                            depthShaderProgram.setUniform("isAnimated", 0);
                        }

                        if (m.shouldCastShadow && m.shouldRender) {
                            Matrix modelLightViewMatrix = worldToLight.matMul(m.getObjectToWorldMatrix());
                            depthShaderProgram.setUniform("modelLightViewMatrix", modelLightViewMatrix);
                            mesh.render();
                        }
                    }
                }
                mesh.endRender();
            }
            glBindFramebuffer(GL_FRAMEBUFFER, 0);
        }

        for(int i =0;i < scene.spotLights.size();i++) {
            SpotLight light = scene.spotLights.get(i);

            Matrix projMatrix = light.shadowProjectionMatrix;
            depthShaderProgram.setUniform("projectionMatrix", projMatrix);
            glViewport(0,0, light.shadowMap.shadowMapWidth, light.shadowMap.shadowMapHeight);
            glBindFramebuffer(GL_FRAMEBUFFER, light.shadowMap.depthMapFBO);
            glClear(GL_DEPTH_BUFFER_BIT);

            Matrix worldToLight = light.getWorldToObject();
            worldToSpotLights.add(worldToLight);

            for(String meshId :scene.shaderblock_mesh_model_map.get(blockID).keySet()) {
                Mesh mesh = scene.meshID_mesh_map.get(meshId);

                if (curShouldCull != mesh.shouldCull) {
                    if(mesh.shouldCull) {
                        glEnable(GL_CULL_FACE);
                    }
                    else {
                        glDisable(GL_CULL_FACE);
                    }
                    curShouldCull = mesh.shouldCull;
                }

                if(currCull != mesh.cullmode) {
                    glCullFace(mesh.cullmode);
                    currCull = mesh.cullmode;
                }

                mesh.initRender();

                if(mesh instanceof InstancedMesh) {
                    depthShaderProgram.setUniform("isInstanced", 1);
                    var inst_mesh = (InstancedMesh) mesh;
                    var models = new ArrayList<Model>();

                    for (String modelId : scene.shaderblock_mesh_model_map.get(blockID).get(meshId).keySet()) {
                        var m = scene.modelID_model_map.get(modelId);
                        if (m.shouldRender && m.shouldCastShadow) {
                            models.add(m);
                        }
                    }
                    var chunks = inst_mesh.getRenderChunks(models);
                    for (var chunk : chunks) {
                        inst_mesh.instanceDataBuffer.clear();

                        for (Model m : chunk) {
                            Matrix objectToLight = worldToLight.matMul(m.getObjectToWorldMatrix());
                            objectToLight.setValuesToFloatBuffer(inst_mesh.instanceDataBuffer);
                            for(int counter = 0;counter < 8;counter++) {
                                inst_mesh.instanceDataBuffer.put(0f);
                            }
                        }
                        inst_mesh.instanceDataBuffer.flip();

                        glBindBuffer(GL_ARRAY_BUFFER, inst_mesh.instanceDataVBO);
                        glBufferData(GL_ARRAY_BUFFER, inst_mesh.instanceDataBuffer, GL_DYNAMIC_DRAW);

                        inst_mesh.render(chunk.size());
                    }
                }
                else{
                    depthShaderProgram.setUniform("isInstanced", 0);
                    for (String modelId : scene.shaderblock_mesh_model_map.get(blockID).get(meshId).keySet()) {
                        Model m = scene.modelID_model_map.get(modelId);

                        if (m instanceof AnimatedModel) {
                            depthShaderProgram.setUniform("isAnimated", 1);
//                        Logger.log("detecting animated model");
                            AnimatedModel anim = (AnimatedModel) m;
                            for (int j = 0; j < anim.currentJointTransformations.size(); j++) {
                                var matrix = anim.currentJointTransformations.get(j);
                                depthShaderProgram.setUniform("jointMatrices[" + j + "]", matrix);
                            }
                        } else {
                            depthShaderProgram.setUniform("isAnimated", 0);
                        }

                        if (m.shouldCastShadow) {
                            Matrix modelLightViewMatrix = worldToLight.matMul(m.getObjectToWorldMatrix());
                            depthShaderProgram.setUniform("modelLightViewMatrix", modelLightViewMatrix);
                            mesh.render();
                        }
                    }
                }
                mesh.endRender();
            }
            glBindFramebuffer(GL_FRAMEBUFFER, 0);
        }

        depthShaderProgram.unbind();
        return new ShadowDepthRenderPackage(worldToDirectionalLights,worldToSpotLights);
    }

    public class ShadowDepthRenderPackage {
        public List<Matrix> worldToDirectionalLights;
        public List<Matrix> worldToSpotLights;
        public ShadowDepthRenderPackage(List<Matrix> worldToDirectionalLights,List<Matrix> worldToSpotLights) {
            this.worldToDirectionalLights = worldToDirectionalLights;
            this.worldToSpotLights = worldToSpotLights;
        }
    }
}
