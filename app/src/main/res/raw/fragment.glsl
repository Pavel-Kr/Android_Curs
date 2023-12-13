precision mediump float;
varying vec2 v_TexCord;
varying vec3 v_Normal;
varying vec3 v_LightPos, v_EyePos;
uniform float u_hasTexture;
uniform sampler2D u_Texture;
uniform vec4 u_Color;
uniform float u_BrightnessFactor;
void main() {
    float specularPower=5.0;
    vec4 color;
    if (u_hasTexture > 0.5){
        color = texture2D(u_Texture, v_TexCord);
    }
    else {
        color = u_Color;
    }
    vec4 ambientColor = vec4(0.1, 0.1, 0.1, 1.0);
    vec4 diffuseColor = vec4(1.0, 1.0, 1.0, 1.0);
    vec4 specularColor = vec4(1.0, 1.0, 1.0, 1.0);
    vec3 normal = normalize(v_Normal);
    vec3 light = normalize(v_LightPos);
    vec3 reflection = reflect(-light, normal);
    vec4 diffuse = diffuseColor * max(dot(normal, light), 0.0);
    vec4 specular = specularColor * pow(max(dot(normalize(v_EyePos), reflection), 0.0), specularPower);
    gl_FragColor = (ambientColor + diffuse + specular) * color;
}
