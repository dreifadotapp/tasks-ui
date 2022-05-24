package dreifa.app.tasks.ui.controllers.providers

import dreifa.app.registry.Registry
import dreifa.app.tasks.ui.controllers.BaseController
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.routing.path

class ViewProviderController(registry: Registry) : BaseController(registry) {

    override fun handle(req: Request): Response {
        val trc = TelemetryRequestContext(req, "/providers/{providerId}")
        return runWithTelemetry(trc) { span ->
            val model = buildBaseModel(req)
            setMenuFlags(model, "prv", "view_prv")

            val providerId = req.path("providerId")!!
            model["providerId"] = providerId

            val html = templateEngine().renderMustache("providers/view.html", model)
            Response(Status.OK).body(html)
        }
    }
}