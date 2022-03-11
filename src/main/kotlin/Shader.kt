import org.joml.*
import org.lwjgl.opengl.GL13C
import org.lwjgl.opengl.GL20.*
import java.rmi.activation.UnknownObjectException


interface Shader {

    fun reload()

    fun use()

    fun setUniform(name: String, u: Any)

}

interface TextureShader : Shader {

    fun bindSamplers()
}


data class LightCounter(
    var sunIndex: Int,
    var spotIndex: Int,
    var pointIndex: Int,
    val indexMap: MutableMap<Light, Int>
)

abstract class LightShader : Shader {

    private var sunIndex: Int = 0
    private var spotIndex: Int = 0
    private var pointIndex: Int = 0
    private val indexMap = mutableMapOf<Light, Int>()

    fun updateSpotLightAngle(light: SpotLight) {
        setUniform("${getUniformName(light)}.innerAngle", light.innerAngle)
        setUniform("${getUniformName(light)}.outerAngle", light.outerAngle)
        setUniform("${getUniformName(light)}.epsilon", light.epsilon)
    }

    fun updatePointLightLinear(light: PointLight) {
        setUniform("${getUniformName(light)}.linear", light.linear)
    }

    fun updatePointLightQuadratic(light: PointLight) {
        setUniform("${getUniformName(light)}.quadratic", light.quadratic)
    }

    fun updateLightRange(light: RangedLight) {
        setUniform("${getUniformName(light)}.range", light.range)
    }

    fun updateLightPos(light: MovableLight) {
        setUniform("${getUniformName(light)}.position", light.pos)
    }

    fun updateLightDir(light: OrientableLight) {
        setUniform("${getUniformName(light)}.direction", light.dir)
    }

    fun updateLightColor(light: Light) = setUniform("${getUniformName(light)}.color", light.color)

    fun updateLightDiffuse(light: Light) {
        setUniform("${getUniformName(light)}.light.diffuse", light.diffuse)
    }

    fun updateLightAmbient(light: Light) {
        setUniform("${getUniformName(light)}.light.ambient", light.ambient)
    }

    fun updateLightSpecular(light: Light) {
        setUniform("${getUniformName(light)}.light.specular", light.specular)
    }

    fun updateLightIntensity(light: Light) {
        setUniform("${getUniformName(light)}.light.intensity", light.intensity)
    }


    fun addLight(light: SunLight) {
        val target = "sunLights[$sunIndex]"
        addBaseLight(light, target)
        setUniform("$target.direction", light.dir)
        indexMap[light] = sunIndex++
        setUniform("nbSunL", sunIndex)
    }

    fun addLight(light: SpotLight) {
        val target = "spotLights[$spotIndex]"
        addBaseLight(light, target)
        setUniform("$target.direction", light.dir)
        setUniform("$target.position", light.pos)
        setUniform("$target.innerAngle", light.innerAngle)
        setUniform("$target.outerAngle", light.outerAngle)
        setUniform("$target.epsilon", light.epsilon)
        setUniform("$target.range", light.range)
        indexMap[light] = spotIndex++
        setUniform("nbSpotL", spotIndex)
    }

    fun addLight(light: PointLight) {
        val target = "pointLights[$pointIndex]"
        addBaseLight(light, target)
        setUniform("$target.position", light.pos)
        setUniform("$target.range", light.range)
        setUniform("$target.linear", light.linear)
        setUniform("$target.quadratic", light.quadratic)
        indexMap[light] = pointIndex++
        setUniform("nbPointL", pointIndex)
    }

    fun removeLight(light: Light) {
        when (light) {
            is SpotLight -> sunIndex--
            is SunLight -> spotIndex--
            is PointLight -> pointIndex--
        }
    }

    fun removeAllLights() {
        indexMap.clear()
        sunIndex = 0
        spotIndex = 0
        pointIndex = 0
    }

    private fun indexOf(light: Light): Int {
        if (light !in indexMap)
            throw UnknownObjectException("this light don't exist in the shader")
        return indexMap[light]!!
    }

    private fun addBaseLight(light: Light, target: String) {
        val lightTarget = "$target.light"
        setUniform("$lightTarget.color", light.color)
        setUniform("$lightTarget.ambient", light.ambient)
        setUniform("$lightTarget.diffuse", light.diffuse)
        setUniform("$lightTarget.specular", light.specular)
        setUniform("$lightTarget.intensity", light.intensity)
    }

    private fun getUniformName(light: Light): String {
        val array = when (light) {
            is SpotLight -> "spotLights"
            is SunLight -> "sunLights"
            is PointLight -> "pointLights"
            else -> throw IllegalArgumentException("this type of light does not exist")
        }
        return "$array[${indexOf(light)}]"
    }
}


class ShaderGl(private val vertexSource: String, private var fragmentSource: String) : Shader {
    private var program = -1

    init {
        val vertex = createShader(GL_VERTEX_SHADER, vertexSource)
        val fragment = createShader(GL_FRAGMENT_SHADER, fragmentSource)
        program = createProgram(vertex, fragment)
        use()
    }

    override fun reload() {
        glDeleteProgram(program)
        val vertex = createShader(GL_VERTEX_SHADER, vertexSource)
        val fragment = createShader(GL_FRAGMENT_SHADER, fragmentSource)
        program = createProgram(vertex, fragment)
        use()
    }

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

    private fun createProgram(vertexShader: Int, fragmentShader: Int): Int {
        val program = glCreateProgram()
        glAttachShader(program, vertexShader)
        glAttachShader(program, fragmentShader)
        glLinkProgram(program)
        println(glGetProgramInfoLog(program))
        glValidateProgram(program)
        glDeleteShader(vertexShader)
        glDeleteShader(fragmentShader)
        return program
    }

    override fun use() {
        glUseProgram(program)
    }

    override fun setUniform(name: String, u: Any) {
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
            is Boolean -> glUniform1i(loc, if (u) 1 else 0)
            else -> throw IllegalArgumentException("type of uniform not supported")
        }
    }

    private fun locate(name: String) = glGetUniformLocation(program, name)
}


class BlinnPhongShaderGl(
    private val shader: Shader = ShaderGl(vertexShad, objectFragShad)
) : Shader by shader, LightShader(), TextureShader{

    init {
        bindSamplers()
    }

    override fun reload() {
        shader.reload()
        removeAllLights()
    }

    override fun bindSamplers() {
        this.setUniform("material.texture", 0)
        this.setUniform("material.specMap", 1)
        this.setUniform("material.emissMap", 2)
        this.setUniform("material.normalMap", 3)
    }

    companion object {
        private const val vertexShad = """#version 460
layout (location=0) in vec3 position;
layout (location=1) in vec2 texCoord;
layout (location=2) in vec3 normal;
layout (location=3) in vec3 tangent;


out vec2 fTexCoord;
out vec3 fNormal;
out vec3 fragPos;
out mat3 TBN;

uniform mat4 projView;
uniform mat4 model;
uniform mat3 worldNormals;
uniform vec3 viewPos;

void main()
{
    gl_Position = projView * model * vec4(position, 1.0);
    
    fTexCoord = texCoord;
    fNormal = normalize(worldNormals * normal);
    
    vec3 T = normalize(vec3(model * vec4(tangent,   0.0)));
    vec3 N = normalize(vec3(model * vec4(normal, 0.0)));
    // re-orthogonalize T with respect to N
    T = normalize(T - dot(T, N) * N);
    vec3 B = cross(normal, tangent);
    TBN = mat3(T, B, N);
    fragPos = vec3(model * vec4(position, 1.0));
}
"""
        private const val objectFragShad = """#version 460
in vec2 fTexCoord;
in vec3 fNormal;
in vec3 fragPos;
in mat3 TBN;

struct Material {
    sampler2D texture;
    sampler2D specMap;
    sampler2D emissMap;
    sampler2D normalMap;
    float shininess;
    int hasSpec;
    int hasEmiss;
    int hasNormal;
};

struct Light {
    vec3 color;
    float ambient;
    float diffuse;
    float specular;
    float intensity;
};

struct SunLight {
    Light light;
    vec3 direction;
};

struct PointLight {
    Light light;
    vec3 position;
    float range;
    float linear;
    float quadratic;
};

struct SpotLight{
    Light light;
    vec3 position;
    vec3 direction;
    float innerAngle;
    float outerAngle;
    float epsilon;
    float range;
};

uniform vec3 viewPos;
uniform Material material;
uniform PointLight pointLights[10];
uniform int nbPointL = 0;

uniform SpotLight spotLights[10];
uniform int nbSpotL = 0;

uniform SunLight sunLights[10];
uniform int nbSunL = 0;


out vec4 FragColor;

const vec3 ZERO = vec3(0.0);

float DiffStr(vec3 lightToFragDir, vec3 normal)
{
    return max(dot(normal, -lightToFragDir), 0.0);
}

float SpecStr(vec3 lightToFragDir, vec3 fragToViewDir, Material mat, vec3 normal)
{
    vec3 halfwayDir = normalize(-lightToFragDir + fragToViewDir);
    return pow(max(dot(normal, halfwayDir), 0.0), mat.shininess);
}

// compute the base ambient + diffuse + specular shading of a light.
// the base is common to all light types.
vec3 ambDiffSpec(
    Light light, vec3 lightToFragDir,vec3 fragToViewDir,
    Material mat, vec3 colorSample, vec3 specularSample,  vec3 normal
){
    float diffStr = DiffStr(lightToFragDir, normal);
    vec3 ambDif = (light.ambient + light.diffuse * diffStr) * colorSample;

    vec3 specular = vec3(0);
    if (mat.hasSpec == 1){
        float specStr = SpecStr(lightToFragDir, fragToViewDir, mat, normal);
        specular.xyz = light.specular * specStr * specularSample;
    }
 
    return (ambDif + specular) * light.color * light.intensity;
}

vec3 CalcSunLight(
SunLight l, vec3 fragToViewDir, Material mat, vec3 colorSample, vec3 specularSample, vec3 normal)
{
    return ambDiffSpec(l.light, l.direction,fragToViewDir, mat, colorSample, specularSample, normal);
}

vec3 CalcPointLight(
PointLight l, vec3 fragToViewDir, Material mat, vec3 colorSample, vec3 specularSample, vec3 normal)
{
    float d = length(l.position - fragPos);
    if(d > l.range) {
        return ZERO;
    }
    float attenuation = 1.0/(l.linear * d  + l.quadratic * d * d);
    vec3 lightToFragDir = normalize(fragPos - l.position);
    return ambDiffSpec(
        l.light, lightToFragDir,fragToViewDir, mat, colorSample, specularSample, normal
    ) * attenuation;
}

vec3 CalcSpotLight(
SpotLight l, vec3 fragToViewDir, Material mat, vec3 colorSample, vec3 specularSample, vec3 normal)
{

    float d = length(l.position - fragPos);
    if(d > l.range) {
        return ZERO;
    }
    float attenu = 1/(0.5 +(d * d * 0.001));
    vec3 spotToFragDir = normalize(fragPos - l.position);
    float spotFragAngle = dot(spotToFragDir, l.direction);
    float intensity = clamp((spotFragAngle - l.outerAngle) / l.epsilon, 0.0, 1.0) ;
    if(intensity < 0.01){
        return ZERO;
    }
    return ambDiffSpec(
        l.light, spotToFragDir, fragToViewDir, mat, colorSample, specularSample, normal
    ) * intensity * attenu;
}



void main()
{
    FragColor = vec4(0.0);

    vec3 colorSample = pow(texture(material.texture, fTexCoord).xyz, vec3(2.2));
    vec3 specularSample = texture(material.specMap, fTexCoord).xyz;
    vec3 normal = fNormal;
    
    if( material.hasEmiss == 1) {
        FragColor.xyz += texture(material.emissMap, fTexCoord).xyz;
    }
    
    if(material.hasNormal == 1) {
        normal = texture(material.normalMap, fTexCoord).xyz;
        normal = normal * 2.0 - 1.0;
        normal = normalize(TBN * normal);
    }
    
    vec3 fragToViewDir = normalize(viewPos - fragPos);
    for (int i = 0; i < nbPointL; i++){
        FragColor.xyz += CalcPointLight(
        pointLights[i], fragToViewDir, material, colorSample, specularSample, normal
        );
    }

    for (int i = 0; i < nbSpotL; i++){
        FragColor.xyz += CalcSpotLight(
        spotLights[i], fragToViewDir, material, colorSample, specularSample, normal
        );
    }

    for (int i = 0; i < nbSunL; i++){
        FragColor.xyz += CalcSunLight(
        sunLights[i], fragToViewDir, material, colorSample, specularSample, normal
        );
    }

    //FragColor.x = 0;
}
"""
    }
}

//
//class ScreenShader : Shader(vertexShader, fragmentShader) {
//
//    companion object {
//        private val vertexShader = """
//            #version 460
//            layout (location=0) in vec3 position;
//            layout (location=1) in vec2 texCoord;
//
//            out vec2 fTexCoord;
//
//            void main()
//            {
//                fTexCoord = texCoord;
//            }
//        """.trimIndent().trim()
//
//        private val fragmentShader = """
//            in vec2 fTexCoord;
//
//            uniform sampler2D screenTexture;
//
//            void main()
//            {
//                FragColor.xyz = texture
//            }
//        """.trimIndent().trim()
//    }
//
//}




