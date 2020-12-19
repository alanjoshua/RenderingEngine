#version 460

layout (location = 0) in vec3 position;
layout (location = 3) in vec4 color;
layout (location = 1) in vec2 texCoord;
layout (location = 2) in vec3 normal;
layout (location = 4) in vec3 tangent;
layout (location = 5) in vec3 biTangent;
layout (location = 6) in float materialIndex;
layout (location = 7) in vec4 biases;
layout (location = 8) in vec4 jointIndices;

const int MAX_DIRECTIONAL_LIGHTS = 5;
const int MAX_SPOT_LIGHTS = 10;

const int MAX_WEIGHTS = 4;
const int MAX_JOINTS = 150;

uniform mat4 modelViewMatrix;
uniform mat4 projectionMatrix;
uniform mat4 modelLightViewMatrix[MAX_DIRECTIONAL_LIGHTS];
uniform mat4 modelSpotLightViewMatrix[MAX_SPOT_LIGHTS];
uniform mat4 directionalLightOrthoMatrix[MAX_DIRECTIONAL_LIGHTS];
uniform mat4 spotlightPerspMatrix[MAX_SPOT_LIGHTS];
uniform int numDirectionalLights;
uniform int numberOfSpotLights;
uniform mat4 jointMatrices[MAX_JOINTS];
uniform int isAnimated = 0;

out vec4 exColor;
out vec2 outTex;
out vec3 vertNormal;
out vec3 vertPos;
out mat4 outModelViewMatrix;
out mat3 TBN;
out vec4 mLightViewVertexPos[MAX_DIRECTIONAL_LIGHTS];
out vec4 mSpotLightViewVertexPos[MAX_SPOT_LIGHTS];
flat out float materialInd;
flat out int numDirLight;
flat out int numSpotLights;

void main() {
    vec4 initPos = vec4(0, 0, 0, 0);
    vec4 initNormal = vec4(0, 0, 0, 0);
    vec4 initTangent = vec4(0, 0, 0, 0);
    vec4 initBitangent = vec4(0, 0, 0, 0);

    int count = 0;
    if (isAnimated != 0) {
        for (int i = 0;i < MAX_WEIGHTS;i++) {
            float weight = biases[i];
            if (weight > 0f) {
                count++;
                int jointIndex = int(jointIndices[i]);
                vec4 temp = jointMatrices[jointIndex] * vec4(position, 1.0);
                initPos += weight * temp;

                vec4 tempNormal = jointMatrices[jointIndex] * vec4(normal, 0.0);
                initNormal += weight * tempNormal;

                vec4 tempTangent = jointMatrices[jointIndex] * vec4(tangent, 0.0);
                initTangent += weight * tempTangent;

                vec4 tempBitangent = jointMatrices[jointIndex] * vec4(biTangent, 0.0);
                initBitangent += weight * tempBitangent;
            }
        }
    }

    if (count == 0) {
        initPos = vec4(position, 1.0);
        initNormal = vec4(normal, 0.0);
        initTangent = vec4(tangent, 0.0);
        initBitangent = vec4(biTangent, 0.0);
    }


    vec4 tempPos = modelViewMatrix * initPos;
    gl_Position = projectionMatrix * tempPos;

    exColor = color;
    outTex = texCoord;

    vertNormal = normalize(modelViewMatrix * initNormal).xyz;
    vertPos = tempPos.xyz;
    outModelViewMatrix = modelViewMatrix;

    vec3 T = normalize(vec3(modelViewMatrix * initTangent));
    vec3 B = normalize(vec3(modelViewMatrix * initBitangent));
    vec3 N = normalize(vec3(modelViewMatrix * initNormal));
    TBN = mat3(T, B, N);

    for(int i = 0;i < numDirectionalLights;i++) {
        mLightViewVertexPos[i] = directionalLightOrthoMatrix[i] * modelLightViewMatrix[i] * vec4(position, 1.0);
    }
    for(int i = 0;i < numberOfSpotLights;i++) {
        mSpotLightViewVertexPos[i] = spotlightPerspMatrix[i] * modelSpotLightViewMatrix[i] * vec4(position, 1.0);
        mSpotLightViewVertexPos[i] = mSpotLightViewVertexPos[i]/mSpotLightViewVertexPos[i].w;
    }
    materialInd = materialIndex;
    numDirLight = numDirectionalLights;
    numSpotLights = numberOfSpotLights;
}