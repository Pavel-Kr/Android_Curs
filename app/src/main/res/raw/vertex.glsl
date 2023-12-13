precision mediump float;
varying vec2 v_TexCord;
varying vec3 v_Normal;
varying vec3 FragPos;
uniform mat4 u_MVPMatrix;
uniform mat4 u_ModelMatrix;
uniform mat4 u_NormalMatrix;
attribute vec3 a_Position;
attribute vec2 a_TexCord;
attribute vec3 a_Normal;
void main(){
    v_TexCord = a_TexCord;
    FragPos = vec3(u_ModelMatrix * vec4(a_Position, 1.0));
    gl_Position = u_MVPMatrix * vec4(a_Position, 1.0);
    v_Normal = vec3(u_NormalMatrix * vec4(a_Normal, 1.0));
}
