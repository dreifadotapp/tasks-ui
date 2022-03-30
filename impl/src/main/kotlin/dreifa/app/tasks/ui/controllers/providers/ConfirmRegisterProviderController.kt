package dreifa.app.tasks.ui.controllers.providers

import dreifa.app.registry.Registry
import dreifa.app.tasks.ui.controllers.BaseController
import dreifa.app.types.UniqueId
import org.http4k.core.*
import org.http4k.routing.path

class ConfirmRegisterProviderController(registry: Registry) : BaseController() {

    override fun handle(request: Request): Response {
        val model = buildBaseModel(request)
        setMenuFlags(model, "prv", "reg_prv")

        val bundleId = request.path("bundleId")!!
        val providerClass = request.path("providerClass")!!
        val providerId = UniqueId.alphanumeric()

        // build the view
        model["bundleId"] = bundleId
        model["providerClass"] = providerClass
        model["providerId"] = providerId
        model["providerName"] = providerClass.split(".").last()
        val content = templateEngine().renderMustache(
            "providers/confirmRegistration.html",
            model
        )
        return html(content)
    }
}