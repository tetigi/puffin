#version 410

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform mat4 modelMatrix;

layout(location = 0) in vec4 position;
in vec2 texCoord;

void main(void){
  gl_Position = position;
  gl_Position = projectionMatrix * viewMatrix * modelMatrix * position;
}
