varying vec4 vertColor;

void main(){
  gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
  vertColor = vec4(0.2, 0.2, 0.2, 1.0);
}
