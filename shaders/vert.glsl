#version 410

in vec3 normal;
in vec4 position;

out Data{
  vec3 normal;
  vec4 position;
} vdata;

void main(void){
  vdata.position = position;
  vdata.normal = normal;
}
