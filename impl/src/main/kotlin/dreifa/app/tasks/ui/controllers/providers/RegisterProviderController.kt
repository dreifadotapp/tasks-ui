package dreifa.app.tasks.ui.controllers.providers

import dreifa.app.tasks.ui.controllers.BaseController
import org.http4k.core.Request
import org.http4k.core.Response

class RegisterProviderController() : BaseController() {
    override fun handle(req: Request): Response {
        val model = buildBaseModel(req)
        setMenuFlags(model, "prv","reg_prv")

        val content = templateEngine().renderMustache("providers/scanJar.html", model)
        return html(content)
    }
}