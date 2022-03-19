package dreifa.app.tasks.ui.controllers.providers

import dreifa.app.registry.Registry
import dreifa.app.tasks.ui.TemplateProcessor
import dreifa.app.tasks.ui.controllers.BaseController
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status

class RegisterProviderController(registry: Registry) : BaseController() {
    override fun handle(request: Request): Response {
        val model = buildBaseModel(request)

        setMenuFlags(model, "prv","reg_prv")

        val html = TemplateProcessor().renderMustache("providers/scanJar.html", model)
        return Response(Status.OK).body(html)
    }
}