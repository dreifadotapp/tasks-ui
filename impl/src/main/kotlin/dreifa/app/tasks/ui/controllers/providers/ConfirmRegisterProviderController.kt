package dreifa.app.tasks.ui.controllers.providers

import dreifa.app.tasks.ui.controllers.BaseController
import dreifa.app.types.UniqueId
import org.http4k.core.*
import org.http4k.routing.path

class ConfirmRegisterProviderController() : BaseController() {

    override fun handle(req: Request): Response {
        val model = buildBaseModel(req)
        setMenuFlags(model, "prv", "reg_prv")

        val bundleId = req.path("bundleId")!!
        val providerClass = req.path("providerClass")!!
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