precision mediump float;
varying vec2 v_TexCord;
varying vec3 v_Normal;
varying vec3 FragPos;
uniform float u_hasTexture;
uniform sampler2D u_Texture;
uniform vec4 u_Color;
uniform vec3 u_LightPos, u_EyePos;
void main() {
    float specularPower=5.0;
    float specularStrength = 0.5;
    vec4 color;
    if (u_hasTexture > 0.5){
        color = texture2D(u_Texture, v_TexCord);
    }
    else {
        color = u_Color;
    }
    vec4 ambientColor = vec4(0.1, 0.1, 0.1, 1.0);
    vec3 diffuseColor = vec3(1.0, 1.0, 1.0);
    vec3 specularColor = vec3(1.0, 1.0, 1.0);
    vec3 normal = normalize(v_Normal);
    vec3 light = normalize(u_LightPos - FragPos);
    vec4 diffuse = vec4(diffuseColor * max(dot(normal, light), 0.0), 1.0);

    vec3 eyeDir = normalize(u_EyePos - FragPos);
    vec3 reflection = reflect(-light, normal);
    vec4 specular = vec4(specularColor * specularStrength * pow(max(dot(eyeDir, reflection), 0.0), specularPower), 1.0);
    gl_FragColor = (ambientColor + diffuse + specular) * color;
}
