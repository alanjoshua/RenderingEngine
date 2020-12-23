package engine.scene;

import engine.Effects.Fog;
import engine.Effects.Material;
import engine.Math.Matrix;
import engine.Math.Vector;
import engine.Mesh.Mesh;
import engine.camera.Camera;
import engine.game.Game;
import engine.geometry.MD5.AnimationFrame;
import engine.geometry.MeshBuilder;
import engine.geometry.MeshBuilderHints;
import engine.lighting.DirectionalLight;
import engine.lighting.PointLight;
import engine.lighting.SpotLight;
import engine.model.AnimatedModel;
import engine.model.HUD;
import engine.model.Model;
import engine.particle.ParticleGenerator;
import engine.renderingEngine.RenderPipeline;
import engine.utils.Logger;
import engine.utils.Utils;

import java.util.*;

import static engine.utils.Logger.log;
import static engine.utils.Logger.logError;

public class Scene {

    public Map<String, Mesh> meshID_mesh_map = new HashMap<>();
    public Map<String, HashMap<String, HashMap<String, Model>>> shaderblock_mesh_model_map = new HashMap<>();
    public Map<String, Model> modelID_model_map = new HashMap<>();
    public Map<String, List<String>> modelID_shaderID_map = new HashMap<>();
//    public Map<String, ShaderProgram> shaderID_shader_map = new HashMap<>();

    public Map<String, ParticleGenerator> particleGenID_generator_map = new HashMap<>();
    public Map<String, List<String>> particleGenID_shaderID_map = new HashMap<>();
    public Map<String, List<String>> shaderBlockID_particelGenID_map = new HashMap<>();

    public List<ParticleGenerator> particleGenerators = new ArrayList<>();
    public List<PointLight> pointLights = new ArrayList<>();
    public List<DirectionalLight> directionalLights = new ArrayList<>();
    public List<SpotLight> spotLights = new ArrayList<>();
    public Vector ambientLight = new Vector(new float[]{0.3f,0.3f,0.3f});

    public Model skybox = null;
    public Fog fog = Fog.NOFOG;

    public HUD hud;
    public Camera camera;
    public RenderPipeline renderPipeline;
    public List<Material> materialLibrary = new ArrayList<>();
    public HashMap<String, Material> materialInd_mat = new HashMap<>();
    public boolean hasMatLibraryUpdated = false;
    private Game game;

    public Scene(Game game) {
        this.game = game;
    }

    public void addPointlight(PointLight pl) {
        pointLights.add(pl);
    }

    public void addSplotLight(SpotLight sl, List<String> shaderID) {
        addModel(sl, shaderID);
        spotLights.add(sl);
    }

    public void addDirectionalLight(DirectionalLight dl, List<String> shaderID) {
        addModel(dl, shaderID);
        directionalLights.add(dl);
    }

    public boolean isIDUniqueMeshID(String id) {
        if (id == null) {
            return false;
        }

        boolean idPresent = meshID_mesh_map.containsKey(id);
        if (idPresent) {
            return false;
        }
        else {
            return true;
        }
    }

    public void setUniqueMeshID(Mesh mesh) {
        if (!isIDUniqueMeshID(mesh.meshIdentifier)) {
            mesh.meshIdentifier = Utils.getUniqueID();
        }
    }

    public void addParticleGenerator(ParticleGenerator generator, List<String> shaderID) {
        var id = generator.ID;

        if (id == null) {
            id = Utils.getUniqueID();
        }

        if (particleGenID_generator_map.containsKey(id)) {
            id = Utils.getUniqueID();
        }

        generator.ID = id;
        particleGenID_generator_map.put(id, generator);
        particleGenerators.add(generator);
        particleGenID_shaderID_map.put(id, shaderID);
        for(var shaID: shaderID) {
            shaderBlockID_particelGenID_map.putIfAbsent(shaID, new ArrayList<>());
            shaderBlockID_particelGenID_map.get(shaID).add(generator.ID);
        }

    }

    public Mesh loadMesh(String location, String meshID, MeshBuilderHints hints) {

        log("Loading mesh "+meshID + " ...");
        Mesh newMesh = MeshBuilder.buildMesh(location, hints);
        log("Finished loading mesh");

        log("Checking whether input meshID is unique...");
        boolean idPresent = meshID_mesh_map.containsKey(meshID);
        String id;

        if (idPresent) {
            log("ID not unique. Checking whether location already exists as an ID...");

            String[] splits = location.split("/");
            String fileName = splits[splits.length - 1].split(".")[0];
            boolean locPresent = meshID_mesh_map.containsKey(fileName);

            if (locPresent) {
                log("Location already being used as ID. Asigning random ID...");
                id = Utils.getUniqueID();
            }
            else {
                id = fileName;
            }
        }
        else {
             id = meshID;
        }

        log("Assigned id: "+id);
        newMesh.meshIdentifier = id;
        meshID_mesh_map.put(id, newMesh);
//        mesh_model_map.put(id, new HashMap<>());
        return newMesh;
    }

    public void removeModel(Model model) {
        var id = model.identifier;
        List<String> shaders = null;

        if(modelID_model_map.containsKey(id)) {
            modelID_model_map.remove(id);
            shaders = modelID_shaderID_map.remove(id);
        }
        else {
            Logger.logError("Error while removing model. Model does not exist");
        }

        for(var shader: shaders) {
            var shaderBlock = shaderblock_mesh_model_map.get(shader);

            for(var mesh: model.meshes) {
                var mesh_model = shaderBlock.get(mesh.meshIdentifier);
                mesh_model.remove(id);
                if(mesh_model.size() == 0) {
                    shaderBlock.remove(mesh.meshIdentifier);
                }
            }

            if(shaderBlock.size() == 0) {
                shaderblock_mesh_model_map.remove(shader);
            }

        }

    }

//    This will add materials to scene mat library only if the matname does not already exist
    public void addMaterialsToLibrary(List<Material> mats) {
        for(var mat: mats) {
            addMaterialToLibrary(mat);
        }
    }

    public Integer addMaterialToLibrary(Material mat) {
        if(!materialInd_mat.containsKey(mat.matName)) {
            materialInd_mat.put(mat.matName, mat);
            int id = materialLibrary.size();
            mat.globalSceneID = id;
            materialLibrary.add(mat);
            hasMatLibraryUpdated = true;
            return id;
        }
        else {
            mat.globalSceneID = materialInd_mat.get(mat.matName).globalSceneID;
            materialInd_mat.put(mat.matName, mat);
            materialLibrary.set(mat.globalSceneID, mat);
        }
        return null;
    }

    public void addMesh(Mesh mesh) {
        log("Checking whether input meshID is unique...");
        boolean idPresent = meshID_mesh_map.containsKey(mesh.meshIdentifier);

        if (idPresent) {
            log("ID not unique. Assigning random ID...");
            mesh.meshIdentifier = Utils.getUniqueID();
        }

        log("Assigned id: "+mesh.meshIdentifier);
        meshID_mesh_map.put(mesh.meshIdentifier, mesh);
    }

    public Model createModel(Mesh mesh, String modelID, List<String> shaderID) {
        addMaterialsToLibrary(mesh.materials);
        Model newModel = new Model(game, Arrays.asList(new Mesh[]{mesh}), modelID);
        addModel(newModel, shaderID);
        return newModel;
    }

    public Model createModel(List<Mesh> meshes, String modelID, List<String> shaderID) {
        for(var mesh: meshes) {
            addMaterialsToLibrary(mesh.materials);
        }
        Model newModel = new Model(game, meshes, modelID);
        addModel(newModel, shaderID);
        return newModel;
    }

    public Model createAnimatedModel(Mesh mesh, List<AnimationFrame> frames, List<Matrix> invMatrices, float frameRate, String modelID, List<String> shaderID) {
        Model newModel = new AnimatedModel(game, Arrays.asList(new Mesh[]{mesh}), frames, invMatrices, frameRate, modelID);
        addModel(newModel, shaderID);
        return newModel;
    }

    public Model createAnimatedModel(List<Mesh> meshes, List<AnimationFrame> frames, List<Matrix> invMatrices, float frameRate, String modelID, List<String> shaderID) {
        Model newModel = new AnimatedModel(game, meshes, frames, invMatrices, frameRate, modelID);
        addModel(newModel, shaderID);
        return newModel;
    }

    public void addModel(Model newModel, List<String> shaderIDs) {

        for(var mesh: newModel.meshes) {
            addMaterialsToLibrary(mesh.materials);
        }

//        Check whether modelID is unique. If not, assign a random ID
        if (modelID_model_map.containsKey(newModel.identifier)) {
            logError("Model ID "+ newModel.identifier + " not unique. Assigning random id...");
            newModel.identifier = Utils.getUniqueID();
        }

        modelID_model_map.put(newModel.identifier, newModel);
        modelID_shaderID_map.put(newModel.identifier, shaderIDs);

//        If model does not have a mesh or shaderIds = Null, return immediately as there is no need to render Model
        if (newModel.meshes == null || (newModel.meshes.size() == 0) || shaderIDs == null) {
            return;
        }

//        Mesh does not exists in scene's database
        for(Mesh mesh: newModel.meshes) {
            if(mesh == null) {
                continue;
            }

            if (!meshID_mesh_map.containsKey(mesh.meshIdentifier)) {
                meshID_mesh_map.put(mesh.meshIdentifier, mesh);
            }

//      Loop through all shaderIds
            for (String shaderID : shaderIDs) {
                shaderblock_mesh_model_map.putIfAbsent(shaderID, new HashMap<>());  // Enter a new hashmap if shaderID does not exist yet

//        Shader is not associated with model mesh, so add new mesh entry to shader ID
                if (!shaderblock_mesh_model_map.get(shaderID).containsKey(mesh.meshIdentifier)) {
                    shaderblock_mesh_model_map.get(shaderID).put(mesh.meshIdentifier, new HashMap<>());
                }

//            Link model with shader
                shaderblock_mesh_model_map.get(shaderID).
                        get(mesh.meshIdentifier).
                        put(newModel.identifier, newModel);  // Insert new model into mesh_model map

            }
        }
    }

    public void addSkyBlock(Model skyblock, List<String> shaderID) {
//        Check whether modelID is unique. If not, assign a random ID
        if (modelID_model_map.containsKey(skyblock.identifier)) {
            logError("Model ID "+ skyblock.identifier + " not unique. Assigning random id...");
            skyblock.identifier = Utils.getUniqueID();
        }

        setUniqueMeshID(skyblock.meshes.get(0));  //Assumes skyblock must only have one mesh
        addModel(skyblock, shaderID);
        this.skybox = skyblock;
    }

    public void cleanUp() {
        for(Mesh m: meshID_mesh_map.values()) {
            m.cleanUp();
        }
    }

    public Collection<Model> getModels() {
        return modelID_model_map.values();
    }

    public Collection<Mesh> getMeshes() {
        return meshID_mesh_map.values();
    }

}
