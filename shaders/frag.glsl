#version 410

uniform vec4 color = vec4(0.2, 0.2, 0.2, 1.0);
in vec4 colorV;
out vec4 outputColor;

void main(){
  outputColor = colorV;
  //outputColor = vec4(0.2f, 0.2f, 0.2f, 0.2f);
}
