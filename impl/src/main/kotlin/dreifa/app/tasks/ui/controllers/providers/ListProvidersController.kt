package dreifa.app.tasks.ui.controllers.providers

import dreifa.app.registry.Registry
import dreifa.app.tasks.ui.controllers.BaseController
import org.http4k.core.Request
import org.http4k.core.Response

class ListProvidersController(registry: Registry) : BaseController(registry) {
    //private val service = ListProvidersService(registry)

    override fun handle(req: Request): Response {
        val model = buildBaseModel(req)
        setMenuFlags(model, "prv", "list_prv")

        // todo - update to use the ListProvidersTask
        //model["providers"] = service.exec(SimpleClientContext())

        val content = templateEngine().renderMustache("providers/list.html", model)
        return html(content)
    }
}