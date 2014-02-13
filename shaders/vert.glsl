#version 410

uniform mat4 pvm;
uniform mat3 normalMatrix;

uniform vec4 diffuse;
uniform vec4 ambient;
uniform vec3 l_dir;

layout(location = 0) in vec4 position;
layout(location = 1) in vec3 normal;

out vec4 colorV;

void main(void){
  //colorV = position;
  vec3 n = normalize(normalMatrix * normal);
  float intensity = max(dot(n, l_dir), 0.0);
  colorV = (intensity * diffuse) + ambient;
  gl_Position = pvm * position;
}
