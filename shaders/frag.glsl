#version 410

in Data{
  vec3 color;
} gdata;

out vec3 fragment;

void main(){
  fragment = gdata.color;
}
