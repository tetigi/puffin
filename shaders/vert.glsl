#version 410

uniform mat4 vm;
uniform mat4 pvm;
uniform mat3 normalMatrix;

uniform vec4 diffuse;
uniform vec4 ambient;
uniform vec3 l_dir;

layout(location = 0) in vec4 positionIn;
layout(location = 1) in vec3 normalIn;
layout(location = 2) in float occlusionIn;

out float intensity;
out vec3 normal;
out float occlusion;
out float depth;

void main(void){
  vec3 n = normalize(normalMatrix * normalIn);
  intensity = max(dot(n, l_dir), 0.0);
  normal = n;
  occlusion = occlusionIn;
  depth = length((vm * positionIn).xyz);
  gl_Position = pvm * positionIn;
}
