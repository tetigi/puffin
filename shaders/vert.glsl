#version 410

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform mat4 modelMatrix;

layout(location = 0) in vec4 position;
layout(location = 1) in vec4 color;

out vec4 colorV;

void main(void){
  colorV = position;
  //colorV = color;
  gl_Position = projectionMatrix * viewMatrix * modelMatrix * position;
}
