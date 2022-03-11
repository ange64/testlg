import org.lwjgl.opengl.GL30.*
import org.lwjgl.opengl.GL32.*
import org.lwjgl.opengl.GL11.*


class FrameBuffer{
    private var bufferId = -1

    init {
        init()
    }

    fun init() {
        bufferId = glGenFramebuffers()
    }

    fun bind() {
        glBindFramebuffer(GL_FRAMEBUFFER, bufferId)
    }

    fun unbind() {
        glBindFramebuffer(GL_FRAMEBUFFER, 0)
    }

    fun checkStatus() : Boolean {
        return glCheckFramebufferStatus(GL_FRAMEBUFFER) == GL_FRAMEBUFFER_COMPLETE
    }

    fun delete() {
        glDeleteFramebuffers(bufferId)
        glBindFramebuffer(GL_FRAMEBUFFER, 0)
    }

    fun attachColorTexture(texture: Texture) {
       glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D,texture.textureId,0)
    }

    fun attachDepthTexture(texture: Texture) {
       glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D,texture.textureId,0)
    }

    fun attachTexture(texture: Texture, attachment : Int){
        glFramebufferTexture2D(GL_FRAMEBUFFER, attachment, GL_TEXTURE_2D,texture.textureId,0)
    }


}


