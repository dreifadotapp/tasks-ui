package dreifa.app.tasks.ui.controllers

import dreifa.app.tasks.ui.TemplateProcessor
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status

abstract class BaseController {
    protected fun buildBaseModel(req: Request): MutableMap<String, Any> {
        return mutableMapOf("foo" to "foo")
    }

    abstract fun handle(req: Request): Response

    fun html(content: String): Response {
        return Response(Status.OK).body(content)
    }

    fun templateEngine(): TemplateProcessor = templateEngine

    companion object {
        private val templateEngine = TemplateProcessor()
    }
}