#version 410

uniform mat4 pvm;
uniform mat3 normalMatrix;

uniform vec4 diffuse;
uniform vec4 ambient;
uniform vec3 l_dir;

layout(location = 0) in vec4 position;
layout(location = 1) in vec3 normal;
layout(location = 2) in float occlusion;

out float intensity;
out vec3 normalV;
out float occlusionV;

void main(void){
  //colorV = position;
  vec3 n = normalize(normalMatrix * normal);
  intensity = max(dot(n, l_dir), 0.0);
  normalV = n;
  occlusionV = occlusion;
  gl_Position = pvm * position;
}
