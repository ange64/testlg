import org.joml.*
import org.lwjgl.opengl.GL20.*
import java.lang.IllegalArgumentException
import java.rmi.activation.UnknownObjectException

open class Shader(vertexSource: String, fragmentSource: String) {
    private val vertex = createShader(GL_VERTEX_SHADER, vertexSource)
    private val fragment = createShader(GL_FRAGMENT_SHADER, fragmentSource)
    private val program = createProgram()

    private fun createShader(shaderType: Int, source: String): Int {
        val shaderId = glCreateShader(shaderType)
        glShaderSource(shaderId, source)
        glCompileShader(shaderId)
        if (glGetShaderi(shaderId, GL_COMPILE_STATUS) == 0) {
            println(glGetShaderInfoLog(shaderId))
            throw Exception("failed to compile shader")
        }
        return shaderId
    }

    private fun createProgram(): Int {
        val program = glCreateProgram()
        glAttachShader(program, vertex)
        glAttachShader(program, fragment)
        glLinkProgram(program)
        println(glGetProgramInfoLog(program))
        glValidateProgram(program)
        glDeleteShader(vertex)
        glDeleteShader(fragment)
        return program
    }

    fun use() {
        glUseProgram(program)
    }

    fun setUniform(name: String, u: Any) {
        val loc = locate(name)
        when (u) {
            is Matrix4fc -> glUniformMatrix4fv(
                loc, false, useStack { u.get(it.mallocFloat(16)) })
            is Matrix3fc -> glUniformMatrix3fv(
                loc, false, useStack { u.get(it.mallocFloat(9)) })
            is Vector4fc -> glUniform4f(loc, u.x(), u.y(), u.z(), u.w())
            is Vector4ic -> glUniform4i(loc, u.x(), u.y(), u.z(), u.w())
            is Vector3fc -> glUniform3f(loc, u.x(), u.y(), u.z())
            is Vector3ic -> glUniform3i(loc, u.x(), u.y(), u.z())
            is Vector2fc -> glUniform2f(loc, u.x(), u.y())
            is Float -> glUniform1f(loc, u)
            is Int -> glUniform1i(loc, u)
            else -> throw IllegalArgumentException("type of uniform not supported")
        }
    }

    private fun locate(name: String) = glGetUniformLocation(program, name)
}

class PhongShader : Shader(vertexShad, objectFragShad) {

    private val sunLights = "sunLights"
    private val spotLights = "spotLights"
    private val pointLights = "pointLights"

    private var sunIndex = 0
    private var spotIndex = 0
    private var pointIndex = 0

    private val indexMap = mutableMapOf<Light, Int>()

    fun addLight(light: SunLight) {
        val target = "$sunLights[$sunIndex]"
        addBaseLight(light, target)
        setUniform("$target.direction", light.dir)
        indexMap[light] = sunIndex++
        setUniform("nbSunL", sunIndex)
    }

    fun addLight(light: SpotLight) {
        val target = "$spotLights[$spotIndex]"
        addBaseLight(light, target)
        setUniform("$target.direction", light.dir)
        setUniform("$target.position", light.pos)
        setUniform("$target.innerAngle", light.innerAngle)
        setUniform("$target.outerAngle", light.outerAngle)
        setUniform("$target.epsilon", light.epsilon)
        indexMap[light] = spotIndex++
        setUniform("nbSpotL", spotIndex)
    }

    fun addLight(light: PointLight) {
        val target = "$pointLights[$pointIndex]"
        addBaseLight(light, target)
        setUniform("$target.position", light.pos)
        setUniform("$target.range", light.range)
        setUniform("$target.linear", light.linear)
        setUniform("$target.quadratic", light.quadratic)
        indexMap[light] = pointIndex++
        setUniform("nbPointL", pointIndex)

    }

    private fun addBaseLight(light: Light, target: String) {
        setUniform("$target.color", light.color)
        setUniform("$target.ambient", light.ambient)
        setUniform("$target.diffuse", light.diffuse)
        setUniform("$target.specular", light.specular)
    }

    fun updateLightPos(light: MovableLight) {
        when (light) {
            is SpotLight -> {
                setUniform("$spotLights[${indexOf(light)}].position", light.pos)
            }
            is PointLight -> {
                setUniform("$pointLights[${indexOf(light)}].position", light.pos)
            }
        }
    }

    fun updateLightDir(light: OrientableLight) {
        when (light) {
            is SpotLight -> {
                setUniform("$spotLights[${indexOf(light)}].direction", light.dir)
            }
            is SunLight -> {
                setUniform("$sunLights[${indexOf(light)}].direction", light.dir)
            }
        }
    }

    fun updateLightColor(light: Light) {
        var target = ""
        when (light) {
            is SunLight -> target = "$sunLights[$sunIndex].color"
            is SpotLight -> target = "$spotLights[$spotIndex].color"
            is PointLight -> target = "$pointLights[$pointIndex].color"
        }
        setUniform(target, light.color)
    }

    private fun indexOf(light: Light): Int {
        if (light !in indexMap)
            throw UnknownObjectException("this light don't exist in the shader")
        return indexMap[light]!!
    }
}

const val objectFragShad = """#version 330
in vec4 fColor;
in vec2 fTexCoord;
in vec3 fNormal;
in vec3 fragPos;

struct Material {
    sampler2D texture;
    sampler2D specMap;
    sampler2D emissMap;
    vec3 emissColor;
    float shininess;
    int hasTexture;
    int hasSpec;
    int hasEmiss;
};

struct SunLight {
    vec3 color;
    float ambient;
    float diffuse;
    float specular;
    vec3 direction;
};

struct PointLight {
    vec3 color;
    float ambient;
    float diffuse;
    float specular;
    vec3 position;
    float range;
    float linear;
    float quadratic;
};

struct SpotLight{
    vec3 color;
    float ambient;
    float diffuse;
    float specular;
    vec3 position;
    vec3 direction;
    float innerAngle;
    float outerAngle;
    float epsilon;
};

uniform Material material;
uniform PointLight pointLights[10];
uniform int nbPointL = 0;

uniform SpotLight spotLights[10];
uniform int nbSpotL = 0;

uniform SunLight sunLights[10];
uniform int nbSunL = 0;

uniform vec3 viewPos;

out vec4 FragColor;

const vec3 ZERO = vec3(0.0);

float DiffStr(vec3 lightToFragDir)
{
    return max(dot(fNormal, -lightToFragDir), 0.0);
}

float SpecStr(vec3 lightToFragDir, vec3 fragToViewDir, Material mat)
{
    vec3 reflect = reflect(lightToFragDir, fNormal);
    return pow(max(dot(fragToViewDir, reflect), 0.0), mat.shininess);
}

// compute the base ambient + diffuse + specular shading of a light.
// the base is common to all light types.
vec3 ambDiffSpec(
float amb, float diff, float spec, vec3 color, vec3 lightToFragDir,
vec3 fragToViewDir, Material mat, vec3 colorSample, vec3 specularSample)
{
    vec3 ambDif = vec3(0.0);
    if (mat.hasTexture == 1)
    {
        float diffStr = DiffStr(lightToFragDir);
        ambDif = (amb + diff * diffStr) * colorSample;
    }

    vec3 specular = vec3(0.0);
    if (mat.hasSpec == 1)
    {
        float specStr = SpecStr(lightToFragDir, fragToViewDir, mat);
        specular = spec * specStr * specularSample;
    }

    return (ambDif + specular) * color;
}

vec3 CalcSunLight(
SunLight l, vec3 fragToViewDir, Material mat, vec3 colorSample, vec3 specularSample)
{
    return ambDiffSpec(
    l.ambient, l.diffuse, l.specular, l.color, l.direction,
    fragToViewDir, mat, colorSample, specularSample
    );
}

vec3 CalcPointLight(
PointLight l, vec3 fragToViewDir, Material mat, vec3 colorSample, vec3 specularSample)
{
    float d = length(l.position - fragPos);
    if(d > l.range) {
        return ZERO;
    }
    float attenuation = 1.0/(1.0 + l.linear * d + l.quadratic * d * d);
    vec3 lightToFragDir = normalize(fragPos - l.position);
    return ambDiffSpec(
    l.ambient, l.diffuse, l.specular, l.color, lightToFragDir,
    fragToViewDir, mat, colorSample, specularSample
    ) * attenuation;
}

vec3 CalcSpotLight(
SpotLight l, vec3 fragToViewDir, Material mat, vec3 colorSample, vec3 specularSample)
{

    float d = length(l.position - fragPos);
    float attenu = min(1/(d * 0.05),2);
    vec3 spotToFragDir = normalize(fragPos - l.position);
    float spotFragAngle = dot(spotToFragDir, l.direction);
    float intensity = clamp((spotFragAngle - l.outerAngle) / l.epsilon, 0.0, 1.0) ;
    if(intensity < 0.01){
        return ZERO;
    }
    return ambDiffSpec(
    l.ambient, l.diffuse, l.specular, l.color, spotToFragDir,
    fragToViewDir, mat, colorSample, specularSample
    ) * intensity * attenu;
}

void main()
{
    FragColor = vec4(0.0);
    
    if (material.hasEmiss == 1){
        FragColor.xyz += texture(material.emissMap, fTexCoord).xyz * material.emissColor;
    }

    vec3 fragToViewDir = normalize(viewPos - fragPos);
    vec3 colorSample = texture(material.texture, fTexCoord).xyz;
    vec3 specularSample = texture(material.specMap, fTexCoord).xyz;

    for (int i = 0; i < nbPointL; i++){
        FragColor.xyz += CalcPointLight(
        pointLights[i], fragToViewDir, material, colorSample, specularSample
        );
    }

    for (int i = 0; i < nbSpotL; i++){
        FragColor.xyz += CalcSpotLight(
       spotLights[i], fragToViewDir, material, colorSample, specularSample
       );
    }

    for (int i = 0; i < nbSunL; i++){
        FragColor.xyz += CalcSunLight(
       sunLights[i], fragToViewDir, material, colorSample, specularSample
       );
    }
   
    FragColor.xyz += CalcPointLight(
        pointLights[0], fragToViewDir, material, colorSample, specularSample
    );
}
"""

const val vertexShad = """#version 330
layout (location=0) in vec3 position;
layout (location=1) in vec4 color;
layout (location=2) in vec2 texCoord;
layout (location=3) in vec3 normal;

out vec4 fColor;
out vec2 fTexCoord;
out vec3 fNormal;
out vec3 fragPos;

uniform mat4 projView;
uniform mat4 model;
uniform mat3 worldNormals;

void main()
{
    gl_Position = projView * model * vec4(position, 1.0);
    fColor = color;
    fTexCoord = texCoord;
    fNormal = normalize(worldNormals * normal);
    fragPos = vec3(model * vec4(position, 1.0));
}
"""




