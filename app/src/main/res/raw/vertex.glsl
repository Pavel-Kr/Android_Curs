precision mediump float;
varying vec2 v_TexCord;
varying vec3 v_Normal;
varying vec3 v_LightPos, v_EyePos;
uniform mat4 u_VPMatrix;
uniform mat4 u_ModelMatrix;
uniform vec3 u_LightPos, u_EyePos;
attribute vec3 a_Position;
attribute vec2 a_TexCord;
attribute vec3 a_Normal;
void main(){
    mat4 MVPMatrix = u_VPMatrix * u_ModelMatrix;
    v_TexCord = a_TexCord;
    v_Normal = vec3(u_ModelMatrix * vec4(a_Normal, 0.0));
    v_LightPos = u_LightPos - a_Position;
    v_EyePos = u_EyePos - a_Position;
    gl_Position = MVPMatrix * vec4(a_Position, 1.0);
}
