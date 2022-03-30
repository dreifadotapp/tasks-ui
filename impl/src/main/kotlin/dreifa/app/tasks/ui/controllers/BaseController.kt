package dreifa.app.tasks.ui.controllers

import dreifa.app.tasks.ui.TemplateProcessor
import dreifa.app.types.UniqueId
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status

abstract class BaseController {
    protected fun buildBaseModel(req: Request): MutableMap<String, Any> {
        return mutableMapOf("currentViewTask" to "view")
    }

    fun setMenuFlags(model: MutableMap<String, Any>, vararg flags: String) {
        flags.asList().forEach {
            model["menuFlag_$it"] = true
        }
    }

    fun setActiveTask(model: MutableMap<String, Any>, providerId: String, taskClazz: String) {
        model["currentViewTask"] = "/$providerId/$taskClazz/view"
    }

    fun setActiveTask(model: MutableMap<String, Any>, providerId: UniqueId, taskClazz: String) {
        setActiveTask(model, providerId.toString(), taskClazz)
    }

    abstract fun handle(req: Request): Response

    fun html(content: String): Response {
        return Response(Status.OK)
            .header("content-type", "text/html")
            .body(content)
    }

    fun json(content: String): Response {
        return Response(Status.OK)
            .header("content-type", "application/json")
            .body(content)
    }

    fun templateEngine(): TemplateProcessor = templateEngine

    companion object {
        private val templateEngine = TemplateProcessor()
    }
}