package engine.scene;

import engine.DataStructure.Mesh.Mesh;
import engine.DataStructure.Texture;
import engine.Effects.Material;
import engine.model.Model;
import engine.utils.Logger;
import org.lwjgl.system.CallbackI;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

public class SceneUtils {


//    Sorting function from https://www.geeksforgeeks.org/sorting-a-hashmap-according-to-values/
    public static <T> Map<T, Integer> sortByValue(Map<T, Integer> hm) {

        // Create a list from elements of HashMap
        List<Map.Entry<T, Integer> > list =
                new LinkedList<>(hm.entrySet());

        // Sort the list
        Collections.sort(list, (o1, o2) -> (o2.getValue()).compareTo(o1.getValue()));

        // put data from sorted list to hashmap
        HashMap<T, Integer> temp = new LinkedHashMap<>();
        for (Map.Entry<T, Integer> aa : list) {
            temp.put(aa.getKey(), aa.getValue());
        }
        return temp;
    }

    public static void writeSceneToKE(Scene scene, String directory, String filePrefix, String engineVersion) {

//      Create Save folder
        File folder = new File(directory+"/"+filePrefix);
        boolean folderCreationSuccess = folder.mkdir();
        if(folderCreationSuccess){
            Logger.log("Directory created successfully");
        }else{
            Logger.logError("Sorry couldn’t create save folder");
            Logger.logError("Save failed...");
            return;
        }

//      Write Material File
        writeMaterialFile(scene.meshID_mesh_map, directory, filePrefix, engineVersion);

//      Write .KE file

    }

    public static void writeMaterialFile(Map<String, Mesh> meshes, String directory, String filePrefix,
                                         String engineVersion) {

//                                           Create new material file

        File materialFile = new File(directory+"/"+filePrefix+"/"+"matLibrary.mtl");
        try {
            materialFile.createNewFile();
        }catch(IOException e) {
            Logger.log("Could not create material file");
        }
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//                                      Create folder for storing textures

        File textureFolder = new File(directory+"/"+filePrefix+"/"+"textures");
        boolean folderCreationSuccess = textureFolder.mkdir();
        if(folderCreationSuccess){
            Logger.log("Directory created successfully");
        }else{
            Logger.log("Sorry couldn’t create textures directory");
        }


        Map<String, Integer> matNamesSoFar = new HashMap<>();
        Map<String, String> texturesStoredSoFar = new HashMap<>();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(materialFile))) {

            int matCount = 0;
            for (Mesh m: meshes.values()) {
                matCount += m.materials.size();
            }

            writer.write("# Created by "+engineVersion+" on "+java.time.LocalDateTime.now()+"\n");
            writer.write("# Material Count: "+matCount+"\n\n");

            for (String meshID: meshes.keySet()) {
                for (Material mat : meshes.get(meshID).materials) {

//                Write material name
                    String matName = meshID+"|"+mat.matName;
//                    Integer times = matNamesSoFar.get(matName);
//                    if (times == null) {
//                        matNamesSoFar.put(matName, 1);
//                    } else {
//                        matName += times;
//                        matNamesSoFar.put(matName, times + 1);
//                    }

                    writer.write("newmtl " + matName + "\n");

//                Write ambient Color
                    writer.write("ka " + mat.ambientColor + '\n');

//                Write diffuse color
                    writer.write("kd " + mat.diffuseColor + "\n");

//                Write specular color
                    writer.write("ks " + mat.specularColor + "\n");

//                Write specular power
                    writer.write("ns " + mat.specularPower + "\n");

//                Write reflectance
                    writer.write("reflectance " + mat.reflectance + "\n");

//                Write texture
                    Texture curTex = mat.texture;
                    if (curTex != null && curTex.fileName != null) {
                        String newTextLoc = texturesStoredSoFar.get(curTex.fileName);

//                    If this texture hasn't already been copied
                        if (newTextLoc == null) {
                            String[] splits = curTex.fileName.split("/");
                            String saveTexName = directory + "/" + filePrefix + "/" + "textures" + "/" + splits[splits.length - 1];

//                        Create copy of texture in current save directory
                            File source = new File(curTex.fileName);
                            File dest = new File(saveTexName);
                            try {
                                Files.copy(source.toPath(), dest.toPath());
                            }
                            catch(Exception e) {
                                Logger.logError("curTex: "+curTex.fileName + " MeshID: "+meshID);
                                e.printStackTrace();
                                System.exit(1);
                            }
                            texturesStoredSoFar.put(curTex.fileName, splits[splits.length - 1]);
                        }
                        writer.write("map_ka " + texturesStoredSoFar.get(curTex.fileName) + "\n");
                    }

//                Write diffuseMap
                    curTex = mat.diffuseMap;
                    if (curTex != null && curTex.fileName != null) {
                        String newTextLoc = texturesStoredSoFar.get(curTex.fileName);

//                    If this texture hasn't already been copied
                        if (newTextLoc == null) {
                            String[] splits = curTex.fileName.split("/");
                            String saveTexName = directory + "/" + filePrefix + "/" + "textures" + "/" + splits[splits.length - 1];

//                        Create copy of texture in current save directory
                            Files.copy(new File(curTex.fileName).toPath(), new File(saveTexName).toPath());

                            texturesStoredSoFar.put(curTex.fileName, splits[splits.length - 1]);
                        }
                        writer.write("map_kd " + texturesStoredSoFar.get(curTex.fileName) + "\n");
                    }

//                Write specular Map
                    curTex = mat.specularMap;
                    if (curTex != null && curTex.fileName != null) {
                        String newTextLoc = texturesStoredSoFar.get(curTex.fileName);

//                    If this texture hasn't already been copied
                        if (newTextLoc == null) {
                            String[] splits = curTex.fileName.split("/");
                            String saveTexName = directory + "/" + filePrefix + "/" + "textures" + "/" + splits[splits.length - 1];

//                        Create copy of texture in current save directory
                            Files.copy(new File(curTex.fileName).toPath(), new File(saveTexName).toPath());

                            texturesStoredSoFar.put(curTex.fileName, splits[splits.length - 1]);
                        }
                        writer.write("map_ks " + texturesStoredSoFar.get(curTex.fileName) + "\n");
                    }

//                Write bump map
                    curTex = mat.normalMap;
                    if (curTex != null && curTex.fileName != null) {
                        String newTextLoc = texturesStoredSoFar.get(curTex.fileName);

//                    If this texture hasn't already been copied
                        if (newTextLoc == null) {
                            String[] splits = curTex.fileName.split("/");
                            String saveTexName = directory + "/" + filePrefix + "/" + "textures" + "/" + splits[splits.length - 1];

//                        Create copy of texture in current save directory
                            Files.copy(new File(curTex.fileName).toPath(), new File(saveTexName).toPath());

                            texturesStoredSoFar.put(curTex.fileName, splits[splits.length - 1]);
                        }
                        writer.write("map_bump " + texturesStoredSoFar.get(curTex.fileName) + "\n");
                    }

                    writer.newLine();
                }
            }

            writer.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
