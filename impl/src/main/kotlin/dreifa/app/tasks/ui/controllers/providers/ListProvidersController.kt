package dreifa.app.tasks.ui.controllers.providers

import dreifa.app.registry.Registry
import dreifa.app.tasks.ui.controllers.BaseController
import org.http4k.core.Request
import org.http4k.core.Response

class ListProvidersController(val registry: Registry) : BaseController() {
    override fun handle(request: Request): Response {
        val model = buildBaseModel(request)

        return html("todo")
    }
}